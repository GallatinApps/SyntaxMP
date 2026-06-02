package com.gallatinapps.syntaxmp.builtins.cpp

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.scanners.clike.PreprocessorOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.ExactStringPrefixes
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.ParenthesizedRawStringRule
import com.gallatinapps.syntaxmp.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object CppTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowOctal = OctalMode.LeadingZero,
        digitSeparators = setOf('\''),
        typeSuffixes = setOf("L", "l", "U", "u", "F", "f", "UL", "ul", "LL", "ll", "ULL", "ull"),
    )
    private val formatSpecifiers = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions(prefix = null, bracketPrefixes = setOf("[[")),
        identifiers = IdentifierOptions(enumMemberRole = SyntaxRole.Constant),
        preprocessor = PreprocessorOptions.Hash,
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "namespace" to emptySet(),
                "using" to setOf("namespace"),
            ),
            separators = setOf("::"),
            wildcardMarkers = emptySet(),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                ParenthesizedRawStringRule(prefixes = setOf("R", "u8R", "uR", "UR", "LR")),
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("u8", "u", "U", "L")),
                    quotes = setOf('"', '\''),
                    escapeModeForPrefix = { EscapeMode.Backslash },
                    formats = formatSpecifiers,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    formats = mapOf('"' to formatSpecifiers, '\'' to formatSpecifiers),
                ),
            ),
        ),
        numbers = numberScanner,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = CppKeywordRoles,
            constants = CppConstants,
            typeKeywords = CppTypeKeywords,
            builtinRoles = CppBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
