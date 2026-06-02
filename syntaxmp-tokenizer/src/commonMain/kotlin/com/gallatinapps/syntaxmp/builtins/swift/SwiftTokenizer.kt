package com.gallatinapps.syntaxmp.builtins.swift

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.HashWrappedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object SwiftTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        allowOctal = OctalMode.ZeroOhPrefix,
        digitSeparators = setOf('_'),
        typeSuffixes = emptySet(),
    )
    private val parenInterpolation = listOf(
        BalancedInterpolationRule(opener = "\\(", openBrace = '(', closeBrace = ')'),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSignAndHash,
        identifiers = IdentifierOptions(
            classifyUppercaseCallsAsTypes = true,
            wildcardIdentifierRole = SyntaxRole.Keyword.append("wildcard"),
            sigilVariablePrefixes = setOf('$'),
        ),
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "import" to setOf("class", "enum", "func", "let", "protocol", "struct", "typealias", "var"),
            ),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                HashWrappedStringRule(
                    quote = '"',
                    allowTripleQuote = true,
                    interpolationFactory = { hashCount ->
                        listOf(
                            BalancedInterpolationRule(
                                opener = "\\" + "#".repeat(hashCount) + "(",
                                openBrace = '(',
                                closeBrace = ')',
                            ),
                        )
                    },
                ),
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
                    interpolation = parenInterpolation,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to parenInterpolation),
                ),
            ),
        ),
        numbers = numberScanner,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = SwiftKeywordRoles,
            constants = SwiftConstants,
            builtinRoles = SwiftBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
