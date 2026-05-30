package com.gallatinapps.syntaxmp.engine.primitives.strings

internal sealed interface StringPrefixRule {
    val leadingChars: Set<Char>
    fun tryMatch(code: String, start: Int): PrefixResult?
}

internal data class PrefixResult(
    val text: String,
    val endExclusive: Int,
)

internal data class ExactStringPrefixes(
    val values: Set<String>,
) : StringPrefixRule {
    private val sortedValues = values.sortedByDescending { it.length }

    override val leadingChars: Set<Char> =
        values.mapNotNullTo(mutableSetOf()) { it.firstOrNull() }

    override fun tryMatch(code: String, start: Int): PrefixResult? =
        sortedValues.firstNotNullOfOrNull { value ->
            if (code.regionMatches(start, value, 0, value.length)) {
                PrefixResult(text = value, endExclusive = start + value.length)
            } else {
                null
            }
        }
}

internal data class CharClassStringPrefixes(
    val allowedChars: Set<Char>,
    val maxLength: Int,
) : StringPrefixRule {
    override val leadingChars: Set<Char> = allowedChars

    override fun tryMatch(code: String, start: Int): PrefixResult? {
        var end = start
        while (end < code.length && end - start < maxLength && code[end] in allowedChars) end++
        return if (end > start) {
            PrefixResult(text = code.substring(start, end), endExclusive = end)
        } else {
            null
        }
    }
}

internal data class PrefixRun(
    val char: Char,
)
