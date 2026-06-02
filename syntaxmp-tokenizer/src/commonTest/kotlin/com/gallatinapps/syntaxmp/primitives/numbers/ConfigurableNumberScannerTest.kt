package com.gallatinapps.syntaxmp.primitives.numbers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigurableNumberScannerTest {
    @Test
    fun defaultScannerScansDecimalHexFractionExponentAndLeadingDot() {
        val scanner = ConfigurableNumberScanner()

        assertEquals("123", scanner.lexeme("123"))
        assertEquals("0x2A", scanner.lexeme("0x2A"))
        assertEquals("1.5", scanner.lexeme("1.5"))
        assertEquals("1e-3", scanner.lexeme("1e-3"))
        assertEquals(".5", scanner.lexeme(".5"))
        assertEquals("0x1.8p3", scanner.lexeme("0x1.8p3"))
    }

    @Test
    fun rangeOperatorsStopBeforeDoubleDot() {
        val scanner = ConfigurableNumberScanner()

        assertEquals("5", scanner.lexeme("5..10"))
        assertEquals("1", scanner.lexeme("1..2"))
        assertNull(scanner.lexeme("5..10", start = 2))
        assertNull(scanner.lexeme("5...10", start = 3))
    }

    @Test
    fun leadingDotCanBeDisabledWithoutBreakingDigitStartContract() {
        val scanner = ConfigurableNumberScanner(allowLeadingDot = false)

        assertNull(scanner.lexeme(".5"))
        assertEquals("5", scanner.lexeme("5"))
    }

    @Test
    fun binaryAndZeroOhOctalRequireFlags() {
        val scanner = ConfigurableNumberScanner(
            allowBinary = true,
            allowOctal = OctalMode.ZeroOhPrefix,
        )

        assertEquals("0b1010", scanner.lexeme("0b1010"))
        assertEquals("0o755", scanner.lexeme("0o755"))
        assertEquals("0", ConfigurableNumberScanner().lexeme("0b1010"))
    }

    @Test
    fun leadingZeroOctalUsesOctalDigits() {
        val scanner = ConfigurableNumberScanner(allowOctal = OctalMode.LeadingZero)

        assertEquals("0644", scanner.lexeme("0644"))
        assertEquals("0", scanner.lexeme("089"))
    }

    @Test
    fun digitSeparatorsAreConsumedOnlyBetweenDigits() {
        val scanner = ConfigurableNumberScanner(digitSeparators = setOf('_', '\''))

        assertEquals("1_000", scanner.lexeme("1_000"))
        assertEquals("1'000", scanner.lexeme("1'000"))
        assertEquals("1", scanner.lexeme("1'a'"))
        assertEquals("1", scanner.lexeme("1_"))
    }

    @Test
    fun typeSuffixesUseLongestMatch() {
        val scanner = ConfigurableNumberScanner(
            typeSuffixes = setOf("u", "u32", "usize"),
        )

        assertEquals("100u32", scanner.lexeme("100u32"))
        assertEquals("100usize", scanner.lexeme("100usize"))
    }

    @Test
    fun bigintAndImaginaryTailsAreOptIn() {
        val bigint = ConfigurableNumberScanner(supportsBigInt = true)
        val imaginary = ConfigurableNumberScanner(supportsImaginary = true)

        assertEquals("100n", bigint.lexeme("100n"))
        assertEquals("1i", imaginary.lexeme("1i"))
        assertEquals("1j", imaginary.lexeme("1j"))
        assertEquals("100", ConfigurableNumberScanner().lexeme("100n"))
    }

    private fun ConfigurableNumberScanner.lexeme(
        code: String,
        start: Int = 0,
    ): String? =
        scan(code, start)?.let { code.substring(start, it) }
}
