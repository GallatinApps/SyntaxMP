package com.gallatinapps.syntaxmp.languages.objectivec

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.PreprocessorOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.ExactStringPrefixes
import com.gallatinapps.syntaxmp.engine.primitives.strings.FormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralScope
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule

internal object ObjectiveCTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowOctal = OctalMode.LeadingZero,
        typeSuffixes = setOf("L", "l", "U", "u", "F", "f"),
    )
    private val formatSpecifiers = listOf(PercentFormatSpecifierRule, ObjectiveCObjectFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign,
        preprocessor = PreprocessorOptions.Hash,
        literals = StringLiteralOptions(
            startRules = listOf(
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("@")),
                    quotes = setOf('"'),
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

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = ObjectiveCKeywordRoles,
                constants = ObjectiveCConstants,
                typeKeywords = ObjectiveCTypeKeywords,
                builtinRoles = ObjectiveCBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}

private object ObjectiveCObjectFormatSpecifierRule : FormatSpecifierRule {
    override val leadingChars: Set<Char> = setOf('%')

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (
            context.code.getOrNull(index) != '%' ||
            index + 1 >= contentEnd ||
            context.code[index + 1] != '@'
        ) {
            return null
        }

        context.emit(index, index + 2, SyntaxRole.Escape)
        return index + 2
    }
}
