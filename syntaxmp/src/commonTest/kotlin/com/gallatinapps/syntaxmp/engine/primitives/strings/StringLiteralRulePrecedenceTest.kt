package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionMarker
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringLiteralRulePrecedenceTest {
    @Test
    fun `leading character buckets preserve rule order`() {
        val first = NamedNoopRule(name = "first", leadingChars = setOf('{'))
        val second = NamedNoopRule(name = "second", leadingChars = setOf('{'))
        val profile = StringLiteralOptions(startRules = listOf(first, second))

        assertEquals(listOf("first", "second"), profile.rulesStartingWith('{').map { (it as NamedNoopRule).name })
    }

    @Test
    fun `component expression rules order double mustache before single brace`() {
        val profile = MarkupScannerOptions(
            expressionLanguage = SyntaxLanguageId.fromString("test"),
            expressionRules = listOf(
                MarkupExpressionRule(
                    opener = "{",
                    closer = "}",
                    allowedAtTopLevel = true,
                    allowedInsideMarkup = true,
                    marker = MarkupExpressionMarker.None,
                ),
                MarkupExpressionRule(
                    opener = "{{",
                    closer = "}}",
                    allowedAtTopLevel = true,
                    allowedInsideMarkup = true,
                    marker = MarkupExpressionMarker.None,
                ),
            ),
        )

        assertEquals("{{", profile.orderedExpressionRules.first().opener)
    }

    @Test
    fun `quote like operators prefer longer operators before q`() {
        val context = TestLiteralContext("qq{job}")
        val rule = QuoteLikeOperatorRule(
            operators = linkedMapOf(
                "q" to QuoteOperatorOptions(),
                "qq" to QuoteOperatorOptions(),
                "qw" to QuoteOperatorOptions(),
                "qr" to QuoteOperatorOptions(),
            ),
        )

        val end = rule.tryMatch(context, start = 0)

        assertEquals(expected = context.code.length, actual = end)
        assertEquals(
            expected = listOf(SyntaxTokenSpan(0, context.code.length, SyntaxRole.String, context.language)),
            actual = context.tokens,
        )
    }

    @Test
    fun `interpolated repeated quote raw strings dispatch from dollar prefix`() {
        val code = "${'$'}\"\"\"value {name}\"\"\""
        val context = TestLiteralContext(code)
        val profile = StringLiteralOptions(
            startRules = listOf(
                TripleQuotedStringRule(quote = '"', escapes = EscapeMode.None),
                RepeatedQuoteRawStringRule(
                    quote = '"',
                    minimumQuoteCount = 3,
                    interpolationPrefix = PrefixRun('$'),
                    interpolation = listOf(BalancedInterpolationRule(opener = "{", openBrace = '{', closeBrace = '}')),
                ),
            ),
        )

        val end = profile.scanFirst(context, start = 0)

        assertEquals(expected = code.length, actual = end)
        assertEquals(SyntaxTokenSpan(0, 10, SyntaxRole.String, context.language), context.tokens.first())
        assertEquals(SyntaxTokenSpan(10, 11, SyntaxRole.Escape, context.language), context.tokens[1])
    }

    @Test
    fun `interpolated verbatim prefixes beat plain at string`() {
        val interpolation = listOf(BalancedInterpolationRule(opener = "{", openBrace = '{', closeBrace = '}'))
        val rule = AtSignVerbatimStringRule(
            interpolationPrefixes = setOf("$@", "@$"),
            interpolation = interpolation,
        )
        val dollarAt = TestLiteralContext("${'$'}@\"value {name}\"")
        val atDollar = TestLiteralContext("@${'$'}\"value {name}\"")

        assertEquals(dollarAt.code.length, rule.tryMatch(dollarAt, start = 0))
        assertEquals(atDollar.code.length, rule.tryMatch(atDollar, start = 0))
        assertEquals(SyntaxTokenSpan(0, 9, SyntaxRole.String, dollarAt.language), dollarAt.tokens.first())
        assertEquals(SyntaxTokenSpan(0, 9, SyntaxRole.String, atDollar.language), atDollar.tokens.first())
        assertEquals(SyntaxTokenSpan(9, 10, SyntaxRole.Escape, dollarAt.language), dollarAt.tokens[1])
        assertEquals(SyntaxTokenSpan(9, 10, SyntaxRole.Escape, atDollar.language), atDollar.tokens[1])
    }

    @Test
    fun `percent literals only start in expression positions`() {
        val rule = PercentLiteralRule(
            marker = '%',
            specifiers = mapOf('i' to PercentLiteralOptions()),
        )
        val modulo = TestLiteralContext("10 % 3")
        val literal = TestLiteralContext("value = %i(foo)")

        assertNull(rule.tryMatch(modulo, start = 3))
        assertEquals(literal.code.length, rule.tryMatch(literal, start = literal.code.indexOf('%')))
        assertEquals(
            expected = listOf(SyntaxTokenSpan(8, literal.code.length, SyntaxRole.String, literal.language)),
            actual = literal.tokens,
        )
    }

    private fun StringLiteralOptions.scanFirst(context: TestLiteralContext, start: Int): Int? =
        rulesStartingWith(context.code[start]).firstNotNullOfOrNull { rule ->
            rule.tryMatch(context, start)
        }
}

private data class NamedNoopRule(
    val name: String,
    override val leadingChars: Set<Char>,
) : StringLiteralStartRule {
    override fun tryMatch(context: StringLiteralScope, start: Int): Int? = null
}
