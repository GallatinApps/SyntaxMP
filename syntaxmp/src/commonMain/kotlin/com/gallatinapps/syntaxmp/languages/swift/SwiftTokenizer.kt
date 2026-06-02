package com.gallatinapps.syntaxmp.languages.swift

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.HashWrappedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

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
