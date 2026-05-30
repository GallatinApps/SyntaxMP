package com.gallatinapps.syntaxmp.engine.spans

import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal fun normalizeTokenSpans(
    spans: List<SyntaxTokenSpan>,
    codeLength: Int,
): List<SyntaxTokenSpan> =
    tryNormalizeTokenSpansFastPath(spans = spans, codeLength = codeLength)
        ?: normalizeTokenSpansGeneral(spans = spans, codeLength = codeLength)

private fun tryNormalizeTokenSpansFastPath(
    spans: List<SyntaxTokenSpan>,
    codeLength: Int,
): List<SyntaxTokenSpan>? {
    var normalized: MutableList<SyntaxTokenSpan>? = null
    var previous: SyntaxTokenSpan? = null
    var previousInputStart = Int.MIN_VALUE
    for (span in spans) {
        if (
            span.start < 0 ||
            span.endExclusive > codeLength ||
            span.endExclusive <= span.start ||
            span.start < previousInputStart
        ) {
            return null
        }

        val previousSpan = previous
        if (previousSpan != null && span.start < previousSpan.endExclusive) {
            return null
        }

        previous = if (
            previousSpan != null &&
            previousSpan.endExclusive == span.start &&
            previousSpan.role == span.role &&
            previousSpan.languageId == span.languageId
        ) {
            previousSpan.copy(endExclusive = span.endExclusive)
        } else {
            if (previousSpan != null) {
                val output = normalized ?: mutableListOf<SyntaxTokenSpan>().also { normalized = it }
                output.add(previousSpan)
            }
            span
        }
        previousInputStart = span.start
    }

    val finalSpan = previous ?: return emptyList()
    val output = normalized ?: return listOf(finalSpan)
    output.add(finalSpan)
    return output
}

private fun normalizeTokenSpansGeneral(
    spans: List<SyntaxTokenSpan>,
    codeLength: Int,
): List<SyntaxTokenSpan> {
    val normalized = spans.mapIndexedNotNull { index, span ->
        val start = span.start.coerceIn(0, codeLength)
        val endExclusive = span.endExclusive.coerceIn(start, codeLength)
        if (endExclusive <= start) {
            null
        } else {
            IndexedTokenSpan(
                index = index,
                span = span.copy(
                    start = start,
                    endExclusive = endExclusive,
                ),
                roleDepth = span.role.value.roleDepth(),
            )
        }
    }
    if (normalized.isEmpty()) {
        return emptyList()
    }

    val events = normalized.flatMap { indexedSpan ->
        listOf(
            SpanBoundaryEvent(indexedSpan.span.start, indexedSpan, SpanBoundaryEventKind.Start),
            SpanBoundaryEvent(indexedSpan.span.endExclusive, indexedSpan, SpanBoundaryEventKind.End),
        )
    }.sortedBy { it.offset }
    val activeSpans = mutableListOf<IndexedTokenSpan>()
    return buildList {
        var eventIndex = 0
        while (eventIndex < events.size) {
            val offset = events[eventIndex].offset
            while (eventIndex < events.size && events[eventIndex].offset == offset) {
                val event = events[eventIndex]
                when (event.kind) {
                    SpanBoundaryEventKind.Start -> activeSpans.add(event.span)
                    SpanBoundaryEventKind.End -> activeSpans.remove(event.span)
                }
                eventIndex++
            }

            val nextOffset = events.getOrNull(eventIndex)?.offset ?: break
            val winner = activeSpans
                .minWithOrNull(indexedTokenSpanComparator)
                ?: continue
            val candidate = winner.span.copy(
                start = offset,
                endExclusive = nextOffset,
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

private data class SpanBoundaryEvent(
    val offset: Int,
    val span: IndexedTokenSpan,
    val kind: SpanBoundaryEventKind,
)

private enum class SpanBoundaryEventKind {
    Start,
    End,
}

private data class IndexedTokenSpan(
    val index: Int,
    val span: SyntaxTokenSpan,
    val roleDepth: Int,
)

private val indexedTokenSpanComparator = compareBy<IndexedTokenSpan> {
    it.span.endExclusive - it.span.start
}.thenByDescending {
    it.roleDepth
}.thenBy {
    it.index
}

private fun String.roleDepth(): Int =
    count { it == '.' } + 1
