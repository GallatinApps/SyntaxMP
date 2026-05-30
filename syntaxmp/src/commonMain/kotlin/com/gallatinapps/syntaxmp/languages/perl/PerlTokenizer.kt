package com.gallatinapps.syntaxmp.languages.perl

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptIdentifierOptions
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.HeredocRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.HeredocMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuoteLikeOperatorRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuoteOperatorOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.RegexLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.SigilVariableInterpolationRule

internal object PerlTokenizer {
    private val sigilInterpolation = listOf(SigilVariableInterpolationRule(prefixes = setOf('$')))
    private val stringFormats = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = ScriptLikeScannerOptions(
        identifiers = ScriptIdentifierOptions(sigilVariablePrefixes = setOf('$', '@', '%')),
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "package" to emptySet(),
                "require" to emptySet(),
                "use" to emptySet(),
            ),
            separators = setOf("::"),
            wildcardMarkers = emptySet(),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                HeredocRule(
                    operator = "<<",
                    modes = setOf(HeredocMode.Plain, HeredocMode.Indented, HeredocMode.TrimIndented),
                    delimiterQuotes = setOf(null, '\'', '"'),
                    bodyInterpolation = { delimiter ->
                        if (delimiter.quote == '\'') emptyList() else sigilInterpolation
                    },
                ),
                QuoteLikeOperatorRule(
                    operators = mapOf(
                        "qq" to QuoteOperatorOptions(interpolation = sigilInterpolation),
                        "qw" to QuoteOperatorOptions(interpolation = emptyList()),
                        "qr" to QuoteOperatorOptions(),
                        "q" to QuoteOperatorOptions(interpolation = emptyList()),
                        "s" to QuoteOperatorOptions(
                            role = SyntaxRole.String.Regex,
                            segmentCount = 2,
                        ),
                    ),
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to sigilInterpolation),
                    formats = mapOf('"' to stringFormats, '\'' to stringFormats),
                ),
            ),
            regex = RegexLiteralOptions.SlashDelimited,
        ),
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            ScriptLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = PerlKeywordRoles,
                constants = PerlConstants,
                builtinRoles = PerlBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
