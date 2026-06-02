package com.gallatinapps.syntaxmp.spans

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenSpanNormalizationTest {
    @Test
    fun sortedNonOverlappingSpansAreReturned() {
        val input = listOf(
            SyntaxTokenSpan(0, 3, SyntaxRole.Keyword, LanguageId.Kotlin),
            SyntaxTokenSpan(5, 8, SyntaxRole.String, LanguageId.Kotlin),
        )

        val spans = normalizeTokenSpans(
            spans = input,
            codeLength = 10,
        )

        assertEquals(input, spans)
    }

    @Test
    fun adjacentSpansWithSameRoleAndLanguageMerge() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(3, 6, SyntaxRole.String, LanguageId.Kotlin),
            ),
            codeLength = 6,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 6, SyntaxRole.String, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun adjacentSpansWithDifferentRolesDoNotMerge() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(3, 6, SyntaxRole.Number, LanguageId.Kotlin),
            ),
            codeLength = 6,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(3, 6, SyntaxRole.Number, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun overlappingTokenSpansAreSplitDeterministically() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 10, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(4, 6, SyntaxRole.Escape, LanguageId.Kotlin),
            ),
            codeLength = 10,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 4, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(4, 6, SyntaxRole.Escape, LanguageId.Kotlin),
                SyntaxTokenSpan(6, 10, SyntaxRole.String, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun deeperRoleWinsWhenOverlappingSpansHaveTheSameRange() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 8, SyntaxRole.Keyword, LanguageId.Kotlin),
                SyntaxTokenSpan(0, 8, SyntaxRole.Keyword.Declaration, LanguageId.Kotlin),
            ),
            codeLength = 8,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 8, SyntaxRole.Keyword.Declaration, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun earlierSpanWinsWhenOverlappingSpansHaveTheSameRangeAndDepth() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 8, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(0, 8, SyntaxRole.Number, LanguageId.Kotlin),
            ),
            codeLength = 8,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 8, SyntaxRole.String, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun sweepLineNormalizerMatchesLegacyBoundaryScanOnGeneratedSpanSets() {
        val roles = listOf(
            SyntaxRole.Keyword,
            SyntaxRole.Keyword.Declaration,
            SyntaxRole.String,
            SyntaxRole.Number,
            SyntaxRole.Markup,
            SyntaxRole.Markup.Expression,
        )
        val languages = listOf(
            LanguageId.Kotlin,
            LanguageId.JavaScript,
            LanguageId.Markdown,
        )
        val random = Random(seed = 30)

        repeat(120) { caseIndex ->
            val codeLength = 1 + random.nextInt(96)
            val spanCount = random.nextInt(1, 48)
            val spans = List(spanCount) { spanIndex ->
                val start = random.nextInt(-12, codeLength + 12)
                val width = random.nextInt(-4, 28)
                SyntaxTokenSpan(
                    start = start,
                    endExclusive = start + width,
                    role = roles[(caseIndex + spanIndex) % roles.size],
                    languageId = languages[(caseIndex + spanIndex * 2) % languages.size],
                )
            }

            assertEquals(
                legacyNormalizeTokenSpans(spans = spans, codeLength = codeLength),
                normalizeTokenSpans(spans = spans, codeLength = codeLength),
                "caseIndex=$caseIndex codeLength=$codeLength spans=$spans",
            )
        }
    }

    @Test
    fun normalizerUsesUtf16CodeUnitOffsets() {
        val code = "let rocket = \"🚀\""
        val rocketStart = code.indexOf("🚀")
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(
                    start = rocketStart,
                    endExclusive = rocketStart + "🚀".length,
                    role = SyntaxRole.String,
                    languageId = LanguageId.Kotlin,
                ),
            ),
            codeLength = code.length,
        )

        assertEquals("🚀", code.substring(spans.single().start, spans.single().endExclusive))
    }

    @Test
    fun outOfBoundsAndEmptySpansNormalizeDeterministically() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(-2, 2, SyntaxRole.Comment, LanguageId.Kotlin),
                SyntaxTokenSpan(2, 2, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(4, 12, SyntaxRole.Keyword, LanguageId.Kotlin),
                SyntaxTokenSpan(12, 14, SyntaxRole.Number, LanguageId.Kotlin),
            ),
            codeLength = 10,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 2, SyntaxRole.Comment, LanguageId.Kotlin),
                SyntaxTokenSpan(4, 10, SyntaxRole.Keyword, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun outOfOrderSpansNormalizeDeterministically() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(4, 8, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(0, 4, SyntaxRole.Keyword, LanguageId.Kotlin),
            ),
            codeLength = 8,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 4, SyntaxRole.Keyword, LanguageId.Kotlin),
                SyntaxTokenSpan(4, 8, SyntaxRole.String, LanguageId.Kotlin),
            ),
            spans,
        )
    }

    @Test
    fun adjacentSpansWithDifferentLanguagesDoNotMerge() {
        val spans = normalizeTokenSpans(
            spans = listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(3, 6, SyntaxRole.String, LanguageId.JavaScript),
            ),
            codeLength = 6,
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.String, LanguageId.Kotlin),
                SyntaxTokenSpan(3, 6, SyntaxRole.String, LanguageId.JavaScript),
            ),
            spans,
        )
    }
}

private fun legacyNormalizeTokenSpans(
    spans: List<SyntaxTokenSpan>,
    codeLength: Int,
): List<SyntaxTokenSpan> {
    val normalized = spans.mapIndexedNotNull { index, span ->
        val start = span.start.coerceIn(0, codeLength)
        val endExclusive = span.endExclusive.coerceIn(start, codeLength)
        if (endExclusive <= start) {
            null
        } else {
            LegacyIndexedTokenSpan(
                index = index,
                span = span.copy(
                    start = start,
                    endExclusive = endExclusive,
                ),
                roleDepth = span.role.value.count { it == '.' } + 1,
            )
        }
    }
    if (normalized.isEmpty()) {
        return emptyList()
    }

    val boundaries = normalized
        .flatMap { listOf(it.span.start, it.span.endExclusive) }
        .distinct()
        .sorted()
    return buildList {
        for (boundaryIndex in 0 until boundaries.lastIndex) {
            val start = boundaries[boundaryIndex]
            val endExclusive = boundaries[boundaryIndex + 1]
            val winner = normalized
                .filter { it.span.start <= start && it.span.endExclusive >= endExclusive }
                .minWithOrNull(legacyIndexedTokenSpanComparator)
                ?: continue
            val candidate = winner.span.copy(
                start = start,
                endExclusive = endExclusive,
            )
            val previous = lastOrNull()
            if (
                previous != null &&
                previous.endExclusive == candidate.start &&
                previous.role == candidate.role &&
                previous.languageId == candidate.languageId
            ) {
                removeAt(lastIndex)
                add(previous.copy(endExclusive = candidate.endExclusive))
            } else {
                add(candidate)
            }
        }
    }
}

private data class LegacyIndexedTokenSpan(
    val index: Int,
    val span: SyntaxTokenSpan,
    val roleDepth: Int,
)

private val legacyIndexedTokenSpanComparator = compareBy<LegacyIndexedTokenSpan> {
    it.span.endExclusive - it.span.start
}.thenByDescending {
    it.roleDepth
}.thenBy {
    it.index
}
