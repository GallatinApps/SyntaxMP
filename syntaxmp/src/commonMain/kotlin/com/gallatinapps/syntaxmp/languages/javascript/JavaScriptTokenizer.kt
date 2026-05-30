package com.gallatinapps.syntaxmp.languages.javascript

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.RegexLiteralOptions

internal object JavaScriptTokenizer {
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
            propertyBeforeColon = true,
        ),
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = JavaScriptKeywordRoles,
                constants = JavaScriptConstants,
                builtinRoles = JavaScriptBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
