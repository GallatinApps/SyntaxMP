package com.gallatinapps.syntaxmp.languages.java

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object JavaTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        digitSeparators = setOf('_'),
        typeSuffixes = setOf("L", "l", "F", "f", "D", "d"),
    )
    private val formatSpecifiers = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign,
        identifiers = IdentifierOptions(enumMemberRole = SyntaxRole.Constant),
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "import" to setOf("static"),
                "package" to emptySet(),
                "module" to emptySet(),
            ),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
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
            keywordRoles = JavaKeywordRoles,
            constants = JavaConstants,
            typeKeywords = JavaTypeKeywords,
            builtinRoles = JavaBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
