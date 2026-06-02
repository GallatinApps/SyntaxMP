package com.gallatinapps.syntaxmp.builtins.fixtures

import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal data class TokenAssertion(
    val start: Int,
    val endExclusive: Int,
    val category: String,
    val scope: String?,
) {
    override fun toString(): String =
        "T($start..$endExclusive, $category${scope?.let { "/$it" } ?: ""})"
}

internal fun tk(start: Int, end: Int, category: String, scope: String? = null): TokenAssertion =
    TokenAssertion(start = start, endExclusive = end, category = category, scope = scope)

/**
 * Strictest: emitted token list must equal [expected] exactly after optional category filtering.
 * Use when you want to lock in the complete tokenization for a snippet.
 */
internal fun assertTokens(
    language: String?,
    code: String,
    expected: List<TokenAssertion>,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
    onlyAssertedCategories: Set<String>? = null,
) {
    val actual = engine.tokenize(code = code, languageLabel = language).toAssertions()
    val filteredActual = actual.filterByCategory(onlyAssertedCategories)
    val filteredExpected = expected.filterByCategory(onlyAssertedCategories)

    assertEquals(
        expected = filteredExpected,
        actual = filteredActual,
        message = buildFailureMessage(
            language = language,
            code = code,
            expected = filteredExpected,
            actual = filteredActual,
            heading = "Token assertion failed.",
        ),
    )
}

/**
 * Looser: every entry in [expected] must appear in the emitted token list. Extras are allowed.
 * Use for targeted checks where unrelated scanner output is uninteresting.
 */
internal fun assertContainsTokens(
    language: String?,
    code: String,
    expected: List<TokenAssertion>,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
) {
    val actual = engine.tokenize(code = code, languageLabel = language).toAssertions()
    val missing = expected.filterNot { it in actual }

    assertTrue(
        actual = missing.isEmpty(),
        message = buildFailureMessage(
            language = language,
            code = code,
            expected = expected,
            actual = actual,
            heading = "Missing expected token(s): ${missing.joinToString()}",
        ),
    )
}

/**
 * Returns the tokenizer output using the same span/category/scope/lexeme format as assertion failures.
 */
internal fun dumpTokens(
    language: String?,
    code: String,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
): String =
    engine.tokenize(code = code, languageLabel = language).toAssertions().describe(code)

/**
 * Negative: no emitted token should satisfy [predicate].
 * Use for false-positive tests where the rejected shape is broader than one exact substring.
 */
internal fun assertNoToken(
    language: String?,
    code: String,
    predicate: (TokenAssertion) -> Boolean,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
) {
    val actual = engine.tokenize(code = code, languageLabel = language).toAssertions()
    val unexpected = actual.filter(predicate)

    assertTrue(
        actual = unexpected.isEmpty(),
        message = buildFailureMessage(
            language = language,
            code = code,
            expected = emptyList(),
            actual = actual,
            heading = "Unexpected token(s): ${unexpected.joinToString()}",
        ),
    )
}

/**
 * Locate [substring] in [code] and assert that no emitted token at that exact range matches the
 * optional [category] and [scope] filters.
 */
internal fun assertNoTokenAt(
    language: String?,
    code: String,
    substring: String,
    category: String? = null,
    scope: String? = null,
    occurrence: Int = 0,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
) {
    val start = code.indexOfOccurrence(substring = substring, occurrence = occurrence)
    val end = start + substring.length
    val actual = engine.tokenize(code = code, languageLabel = language).toAssertions()
    val unexpected = actual.filter { token ->
        token.start == start &&
            token.endExclusive == end &&
            (category == null || token.category == category) &&
            (scope == null || token.scope == scope)
    }

    assertTrue(
        actual = unexpected.isEmpty(),
        message = buildFailureMessage(
            language = language,
            code = code,
            expected = emptyList(),
            actual = actual,
            heading = "Unexpected token at $start..$end for \"$substring\": ${unexpected.joinToString()}",
        ),
    )
}

/**
 * Locate [substring] in [code] and assert the emitted span at that exact range matches [category]
 * and optional [scope].
 */
internal fun assertTokenAt(
    language: String?,
    code: String,
    substring: String,
    category: String,
    scope: String? = null,
    occurrence: Int = 0,
    engine: SyntaxTokenizer = SyntaxTokenizer(),
) {
    val start = code.indexOfOccurrence(substring = substring, occurrence = occurrence)
    val expected = tk(start = start, end = start + substring.length, category = category, scope = scope)
    val actual = engine.tokenize(code = code, languageLabel = language).toAssertions()
    val matches = actual.any { token ->
        token.start == expected.start &&
            token.endExclusive == expected.endExclusive &&
            token.category == expected.category &&
            (scope == null || token.scope == scope)
    }

    assertTrue(
        actual = matches,
        message = buildFailureMessage(
            language = language,
            code = code,
            expected = listOf(expected),
            actual = actual,
            heading = "Expected token at ${expected.start}..${expected.endExclusive} for \"$substring\" was not emitted.",
        ),
    )
}

private fun List<SyntaxTokenSpan>.toAssertions(): List<TokenAssertion> =
    map { it.toAssertion() }

private fun SyntaxTokenSpan.toAssertion(): TokenAssertion =
    TokenAssertion(
        start = start,
        endExclusive = endExclusive,
        category = role.value.substringBefore('.').categoryName(),
        scope = role.value,
    )

private fun List<TokenAssertion>.filterByCategory(categories: Set<String>?): List<TokenAssertion> =
    if (categories == null) {
        this
    } else {
        filter { it.category in categories }
    }

private fun String.indexOfOccurrence(substring: String, occurrence: Int): Int {
    if (substring.isEmpty()) {
        fail("Cannot assert against an empty substring.")
    }
    if (occurrence < 0) {
        fail("Occurrence must be >= 0, was $occurrence.")
    }

    var searchFrom = 0
    repeat(occurrence + 1) { count ->
        val index = indexOf(substring, startIndex = searchFrom)
        if (index == -1) {
            fail("Could not find occurrence #$occurrence of \"$substring\" in code; found $count occurrence(s).")
        }
        if (count == occurrence) {
            return index
        }
        searchFrom = index + substring.length
    }

    fail("Could not find occurrence #$occurrence of \"$substring\" in code.")
}

private fun buildFailureMessage(
    language: String?,
    code: String,
    expected: List<TokenAssertion>,
    actual: List<TokenAssertion>,
    heading: String,
): String =
    buildString {
        appendLine(heading)
        appendLine("Language: ${language ?: "<none>"}")
        appendLine("Code:")
        appendLine(code)
        appendLine("Expected tokens:")
        appendLine(expected.describe(code))
        appendLine("Actual tokens:")
        appendLine(actual.describe(code))
    }

private fun List<TokenAssertion>.describe(code: String): String =
    if (isEmpty()) {
        "<none>"
    } else {
        joinToString(separator = "\n") { token ->
            "${token}=${code.lexemeFor(token)}"
        }
    }

private fun String.lexemeFor(token: TokenAssertion): String {
    val start = token.start.coerceIn(0, length)
    val end = token.endExclusive.coerceIn(start, length)
    return substring(start, end).quoteForMessage()
}

private fun String.quoteForMessage(): String =
    "\"${replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"")}\""

private fun String.categoryName(): String =
    replaceFirstChar { first -> first.uppercase() }
