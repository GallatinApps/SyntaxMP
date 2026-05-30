package com.gallatinapps.syntaxmp.languages.c

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.PreprocessorOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule

internal object CTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowOctal = OctalMode.LeadingZero,
        typeSuffixes = setOf("L", "l", "U", "u", "F", "f", "UL", "ul", "LL", "ll", "ULL", "ull"),
    )
    private val formatSpecifiers = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        identifiers = IdentifierOptions(
            enumMemberRole = SyntaxRole.Constant,
            structFieldRole = SyntaxRole.Property,
        ),
        preprocessor = PreprocessorOptions.Hash,
        literals = StringLiteralOptions(
            startRules = listOf(
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    formats = mapOf('"' to formatSpecifiers, '\'' to formatSpecifiers),
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
                keywordRoles = CKeywordRoles,
                constants = CConstants,
                typeKeywords = CTypeKeywords,
                builtinRoles = CBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
