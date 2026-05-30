package com.gallatinapps.syntaxmp.languages.go

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BacktickIdentifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule

internal object GoTokenizer {
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

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = GoKeywordRoles,
                constants = GoConstants,
                typeKeywords = GoTypeKeywords,
                builtinRoles = GoBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
