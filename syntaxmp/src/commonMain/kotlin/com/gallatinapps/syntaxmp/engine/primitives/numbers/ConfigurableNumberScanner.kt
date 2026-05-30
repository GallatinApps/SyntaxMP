package com.gallatinapps.syntaxmp.engine.primitives.numbers

/**
 * Configurable numeric literal scanner for SyntaxMP's built-in scanner implementations.
 *
 * Digit separators are consumed only between digits. This matters most for C++ apostrophes:
 * `1'000` is one numeric literal, while `1'a'` stops after `1` so the quote can be handled by the
 * scanner's string path.
 */
internal class ConfigurableNumberScanner(
    private val allowHex: Boolean = true,
    private val allowBinary: Boolean = false,
    private val allowOctal: OctalMode? = null,
    private val digitSeparators: Set<Char> = emptySet(),
    private val typeSuffixes: Set<String> = emptySet(),
    private val allowLeadingDot: Boolean = true,
    private val exponentChars: Set<Char> = setOf('e', 'E'),
    private val hexExponentChars: Set<Char> = setOf('p', 'P'),
    private val supportsBigInt: Boolean = false,
    private val supportsImaginary: Boolean = false,
) : NumericLiteralScanner {
    private val sortedSuffixes = typeSuffixes.sortedByDescending { it.length }

    companion object {
        val Default: NumericLiteralScanner = ConfigurableNumberScanner()
    }

    override fun scan(code: String, start: Int): Int? {
        if (start !in code.indices) return null
        val first = code[start]
        return when {
            first == '.' -> scanLeadingDot(code, start)
            first.isDigit() -> scanDigitStart(code, start)
            else -> null
        }
    }

    private fun scanLeadingDot(code: String, start: Int): Int? {
        if (
            !allowLeadingDot ||
            code.getOrNull(start - 1) == '.' ||
            code.getOrNull(start + 1)?.isDigit() != true
        ) {
            return null
        }
        var index = start + 1
        index = consumeDigitsAndSeparators(code, index, radix = 10)
        index = consumeExponent(code, index, exponentChars)
        index = consumeTail(code, index)
        return index
    }

    private fun scanDigitStart(code: String, start: Int): Int {
        var index = start
        var isHex = false

        if (code[start] == '0') {
            val next = code.getOrNull(start + 1)
            when {
                allowHex && (next == 'x' || next == 'X') -> {
                    val digitsStart = start + 2
                    val digitsEnd = consumeDigitsAndSeparators(code, digitsStart, radix = 16)
                    index = if (digitsEnd > digitsStart) digitsEnd else start + 1
                    isHex = digitsEnd > digitsStart
                }
                allowBinary && (next == 'b' || next == 'B') -> {
                    val digitsStart = start + 2
                    val digitsEnd = consumeDigitsAndSeparators(code, digitsStart, radix = 2)
                    index = if (digitsEnd > digitsStart) digitsEnd else start + 1
                }
                allowOctal == OctalMode.ZeroOhPrefix && (next == 'o' || next == 'O') -> {
                    val digitsStart = start + 2
                    val digitsEnd = consumeDigitsAndSeparators(code, digitsStart, radix = 8)
                    index = if (digitsEnd > digitsStart) digitsEnd else start + 1
                }
                allowOctal == OctalMode.LeadingZero -> {
                    index = consumeDigitsAndSeparators(code, start, radix = 8)
                    if (index == start) index = start + 1
                }
                else -> {
                    index = consumeDigitsAndSeparators(code, start, radix = 10)
                }
            }
        } else {
            index = consumeDigitsAndSeparators(code, start, radix = 10)
        }

        index = consumeFraction(code, index, isHex = isHex)
        index = consumeExponent(
            code = code,
            start = index,
            markers = if (isHex) hexExponentChars else exponentChars,
        )
        index = consumeTail(code, index)
        return index
    }

    private fun consumeFraction(
        code: String,
        start: Int,
        isHex: Boolean,
    ): Int {
        if (code.getOrNull(start) != '.') return start
        if (code.getOrNull(start + 1) == '.') return start

        val radix = if (isHex) 16 else 10
        return consumeDigitsAndSeparators(code, start + 1, radix)
    }

    private fun consumeExponent(
        code: String,
        start: Int,
        markers: Set<Char>,
    ): Int {
        if (code.getOrNull(start) !in markers) return start
        var cursor = start + 1
        if (code.getOrNull(cursor) == '+' || code.getOrNull(cursor) == '-') cursor++
        val digitsStart = cursor
        val digitsEnd = consumeDigitsAndSeparators(code, digitsStart, radix = 10)
        return if (digitsEnd > digitsStart) digitsEnd else start
    }

    private fun consumeTail(code: String, start: Int): Int {
        if (supportsBigInt && code.getOrNull(start) == 'n') {
            return start + 1
        }
        if (supportsImaginary && code.getOrNull(start) in setOf('i', 'j')) {
            return start + 1
        }
        val suffix = sortedSuffixes.firstOrNull { suffix ->
            code.regionMatches(start, suffix, 0, suffix.length)
        }
        return if (suffix != null) start + suffix.length else start
    }

    private fun consumeDigitsAndSeparators(
        code: String,
        start: Int,
        radix: Int,
    ): Int {
        var cursor = start
        while (cursor < code.length) {
            val char = code[cursor]
            when {
                char.isDigitForRadix(radix) -> cursor++
                char in digitSeparators &&
                    cursor > start &&
                    code[cursor - 1].isDigitForRadix(radix) &&
                    code.getOrNull(cursor + 1)?.isDigitForRadix(radix) == true -> cursor++
                else -> return cursor
            }
        }
        return cursor
    }
}

private fun Char.isDigitForRadix(radix: Int): Boolean =
    when (radix) {
        2 -> this == '0' || this == '1'
        8 -> this in '0'..'7'
        10 -> isDigit()
        16 -> isDigit() || this in 'a'..'f' || this in 'A'..'F'
        else -> false
    }
