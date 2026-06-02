package com.gallatinapps.syntaxmp.builtins.typescript

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.scanners.clike.PropertyBeforeColonMode
import com.gallatinapps.syntaxmp.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.RegexLiteralOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object TypeScriptTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        allowOctal = OctalMode.ZeroOhPrefix,
        digitSeparators = setOf('_'),
        supportsBigInt = true,
    )
    private val templateInterpolation = listOf(
        BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = CLikeScannerOptions(
        literals = StringLiteralOptions(
            startRules = listOf(
                QuotedStringRule(
                    quotes = setOf('"', '\'', '`'),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('`' to templateInterpolation),
                ),
            ),
            regex = RegexLiteralOptions.SlashDelimited,
        ),
        numbers = numberScanner,
        identifiers = IdentifierOptions(
            enumMemberRole = SyntaxRole.Constant,
            propertyBeforeColonMode = PropertyBeforeColonMode.ObjectLikeOnly,
        ),
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = TypeScriptKeywordRoles,
            constants = TypeScriptConstants,
            builtinRoles = TypeScriptBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
