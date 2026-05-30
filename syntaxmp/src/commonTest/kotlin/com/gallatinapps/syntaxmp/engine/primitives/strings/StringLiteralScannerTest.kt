package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart
import kotlin.test.Test
import kotlin.test.assertEquals

class StringLiteralScannerTest {
    @Test
    fun `scanner owns string segments around balanced interpolation`() {
        val context = TestLiteralContext(""""value ${'$'}{name} done"""")

        val end = context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = 0,
                contentStart = 1,
                closing = FixedStringLiteralClosingRule("\""),
                escapes = EscapeMode.Backslash,
                interpolation = listOf(
                    BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
                ),
            ),
        )

        assertEquals(expected = context.code.length, actual = end)
        assertEquals(
            expected = listOf(
                SyntaxTokenSpan(0, 7, SyntaxRole.String, context.language),
                SyntaxTokenSpan(7, 9, SyntaxRole.Escape, context.language),
                SyntaxTokenSpan(9, 13, SyntaxRole.Variable, context.language),
                SyntaxTokenSpan(13, 14, SyntaxRole.Escape, context.language),
                SyntaxTokenSpan(14, 20, SyntaxRole.String, context.language),
            ),
            actual = context.tokens,
        )
    }

    @Test
    fun `scanner emits unterminated remainder as string`() {
        val context = TestLiteralContext(""""value""")

        val end = context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = 0,
                contentStart = 1,
                closing = FixedStringLiteralClosingRule("\""),
                escapes = EscapeMode.Backslash,
            ),
        )

        assertEquals(expected = context.code.length, actual = end)
        assertEquals(
            expected = listOf(SyntaxTokenSpan(0, context.code.length, SyntaxRole.String, context.language)),
            actual = context.tokens,
        )
    }

    @Test
    fun `escape and format rules split string segments`() {
        val context = TestLiteralContext(""""a\n %03d z"""")

        context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = 0,
                contentStart = 1,
                closing = FixedStringLiteralClosingRule("\""),
                escapes = EscapeMode.Backslash,
                formats = listOf(PercentFormatSpecifierRule),
            ),
        )

        assertEquals(
            expected = listOf(
                SyntaxTokenSpan(0, 2, SyntaxRole.String, context.language),
                SyntaxTokenSpan(2, 4, SyntaxRole.Escape, context.language),
                SyntaxTokenSpan(4, 5, SyntaxRole.String, context.language),
                SyntaxTokenSpan(5, 9, SyntaxRole.Escape, context.language),
                SyntaxTokenSpan(9, 12, SyntaxRole.String, context.language),
            ),
            actual = context.tokens,
        )
    }

    @Test
    fun `unknown boundary leading characters keep scanning each position`() {
        val context = TestLiteralContext(""""left @right"""")

        context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = 0,
                contentStart = 1,
                closing = FixedStringLiteralClosingRule("\""),
                escapes = EscapeMode.Backslash,
                interpolation = listOf(AtSignInterpolationRule),
            ),
        )

        assertEquals(
            expected = listOf(
                SyntaxTokenSpan(0, 6, SyntaxRole.String, context.language),
                SyntaxTokenSpan(6, 7, SyntaxRole.Escape, context.language),
                SyntaxTokenSpan(7, 13, SyntaxRole.String, context.language),
            ),
            actual = context.tokens,
        )
    }
}

private object AtSignInterpolationRule : InterpolationRule {
    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (context.code.getOrNull(index) != '@') return null
        context.emit(index, index + 1, SyntaxRole.Escape)
        return index + 1
    }
}

internal class TestLiteralContext(
    override val code: String,
    val language: SyntaxLanguageId = SyntaxLanguageId.fromString("test"),
) : StringLiteralScope {
    val tokens = mutableListOf<SyntaxTokenSpan>()

    override fun emit(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }

    override fun scanNestedCode(start: Int, endExclusive: Int) {
        var index = start
        while (index < endExclusive) {
            when {
                code[index].isWhitespace() -> index++
                code[index].isDigit() -> {
                    val numberStart = index
                    while (index < endExclusive && code[index].isDigit()) index++
                    emit(numberStart, index, SyntaxRole.Number)
                }
                else -> {
                    val identifierStart = index
                    while (index < endExclusive && code[index].isIdentifierPart()) index++
                    if (index > identifierStart) {
                        emit(identifierStart, index, SyntaxRole.Variable)
                    } else {
                        index++
                    }
                }
            }
        }
    }

    override fun previousNonWhitespace(index: Int): Char? {
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace()) cursor--
        return code.getOrNull(cursor)
    }

    override fun nextNonWhitespace(index: Int): Char? {
        var cursor = index
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        return code.getOrNull(cursor)
    }

    override fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    override fun scanNestedIdentifier(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && code[end].isIdentifierPart()) end++
        emit(start, end, SyntaxRole.Variable)
        return end
    }

    override fun scanStringLiteral(request: StringLiteralRequest): Int =
        StringLiteralScanner(this).scan(request)
}
