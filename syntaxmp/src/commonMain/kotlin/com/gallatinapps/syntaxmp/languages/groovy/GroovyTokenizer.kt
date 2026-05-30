package com.gallatinapps.syntaxmp.languages.groovy

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.DollarSlashDelimitedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.SlashDelimitedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule

internal object GroovyTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        digitSeparators = setOf('_'),
        typeSuffixes = setOf("G", "g", "I", "i", "L", "l", "F", "f", "D", "d"),
    )
    private val dollarInterpolation = listOf(
        DollarIdentifierInterpolationRule,
        BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign,
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "import" to setOf("static"),
                "package" to emptySet(),
            ),
            terminatorKeywords = setOf("as"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                DollarSlashDelimitedStringRule(interpolation = dollarInterpolation),
                SlashDelimitedStringRule(interpolation = dollarInterpolation),
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
                    interpolation = dollarInterpolation,
                ),
                TripleQuotedStringRule(
                    quote = '\'',
                    escapes = EscapeMode.None,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to dollarInterpolation),
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
                keywordRoles = GroovyKeywordRoles,
                constants = GroovyConstants,
                typeKeywords = GroovyTypeKeywords,
                builtinRoles = GroovyBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
