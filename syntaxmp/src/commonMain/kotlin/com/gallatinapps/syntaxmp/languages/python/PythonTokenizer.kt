package com.gallatinapps.syntaxmp.languages.python

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.CharClassStringPrefixes
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule

internal object PythonTokenizer {
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

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            ScriptLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = PythonKeywordRoles,
                constants = PythonConstants,
                builtinRoles = PythonBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
