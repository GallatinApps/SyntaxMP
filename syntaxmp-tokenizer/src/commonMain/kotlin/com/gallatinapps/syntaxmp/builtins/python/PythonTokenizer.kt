package com.gallatinapps.syntaxmp.builtins.python

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.scanners.script.ScriptLikeScanner
import com.gallatinapps.syntaxmp.scanners.script.ScriptLikeScannerOptions
import com.gallatinapps.syntaxmp.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.CharClassStringPrefixes
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object PythonTokenizer : LanguageTokenizer {
    private val stringFormats = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = ScriptLikeScannerOptions(
        qualifiedNames = QualifiedNameOptions.after(
            "from",
            "import",
            terminatorKeywords = setOf("as", "import"),
        ),
        lineStartDecoratorRole = SyntaxRole.Annotation,
        literals = StringLiteralOptions(
            startRules = listOf(
                PrefixedQuotedStringRule(
                    prefixes = CharClassStringPrefixes(
                        allowedChars = setOf('r', 'R', 'u', 'U', 'b', 'B', 'f', 'F'),
                        maxLength = 3,
                    ),
                    quotes = setOf('"', '\''),
                    allowTripleQuote = true,
                    escapeModeForPrefix = { prefix ->
                        if ('r' in prefix.lowercase()) EscapeMode.None else EscapeMode.Backslash
                    },
                    tripleEscapeModeForPrefix = { prefix ->
                        if ('r' in prefix.lowercase()) EscapeMode.None else EscapeMode.Backslash
                    },
                    interpolationRulesForPrefix = { prefix ->
                        if ('f' in prefix.lowercase()) {
                            listOf(
                                BalancedInterpolationRule(
                                    opener = "{",
                                    openBrace = '{',
                                    closeBrace = '}',
                                    ignoreDoubledOpener = true,
                                ),
                            )
                        } else {
                            emptyList()
                        }
                    },
                    formats = stringFormats,
                ),
                TripleQuotedStringRule(quote = '"', escapes = EscapeMode.None),
                TripleQuotedStringRule(quote = '\'', escapes = EscapeMode.None),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    formats = mapOf('"' to stringFormats, '\'' to stringFormats),
                ),
            ),
        ),
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ScriptLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = PythonKeywordRoles,
            constants = PythonConstants,
            builtinRoles = PythonBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
