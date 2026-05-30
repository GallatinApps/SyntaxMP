package com.gallatinapps.syntaxmp.languages.csharp

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.AdjacentPrefixQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.AtSignVerbatimStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PrefixRun
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.RepeatedQuoteRawStringRule

internal object CSharpTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        digitSeparators = setOf('_'),
        typeSuffixes = setOf("L", "l", "U", "u", "F", "f", "M", "m", "D", "d", "UL", "ul"),
    )
    private val braceInterpolation = listOf(
        BalancedInterpolationRule(
            opener = "{",
            openBrace = '{',
            closeBrace = '}',
            ignoreDoubledOpener = true,
        ),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions(
            prefix = null,
            bracketPrefixes = setOf("["),
            bracketRequiresLineStart = true,
        ),
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "namespace" to emptySet(),
                "using" to setOf("static"),
            ),
            terminatorKeywords = setOf("as"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                RepeatedQuoteRawStringRule(
                    quote = '"',
                    minimumQuoteCount = 3,
                    interpolationPrefix = PrefixRun(char = '$'),
                    interpolation = braceInterpolation,
                ),
                AtSignVerbatimStringRule(
                    interpolationPrefixes = setOf("${'$'}@", "@${'$'}"),
                    interpolation = braceInterpolation,
                ),
                AdjacentPrefixQuotedStringRule(
                    prefix = "$",
                    quote = '"',
                    escapes = EscapeMode.Backslash,
                    interpolation = braceInterpolation,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                ),
            ),
        ),
        numbers = numberScanner,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = CSharpKeywordRoles,
                constants = CSharpConstants,
                typeKeywords = CSharpTypeKeywords,
                builtinRoles = CSharpBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
