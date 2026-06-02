package com.gallatinapps.syntaxmp.builtins.go

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.primitives.strings.BacktickIdentifierRule
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object GoTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        allowOctal = OctalMode.LeadingZero,
        digitSeparators = setOf('_'),
        supportsImaginary = true,
    )
    private val formatSpecifiers = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        identifiers = IdentifierOptions(
            structFieldRole = SyntaxRole.Property,
            constantDeclarationKeywords = setOf("const"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                BacktickIdentifierRule(),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    formats = mapOf('"' to formatSpecifiers, '\'' to formatSpecifiers),
                ),
            ),
        ),
        qualifiedNames = QualifiedNameOptions.after(
            "package",
            wildcardMarkers = emptySet(),
        ),
        numbers = numberScanner,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = GoKeywordRoles,
            constants = GoConstants,
            typeKeywords = GoTypeKeywords,
            builtinRoles = GoBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
