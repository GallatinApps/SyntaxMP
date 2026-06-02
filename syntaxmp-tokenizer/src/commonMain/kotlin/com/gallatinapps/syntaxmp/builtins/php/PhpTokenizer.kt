package com.gallatinapps.syntaxmp.builtins.php

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.primitives.BlockCommentOptions
import com.gallatinapps.syntaxmp.primitives.LineCommentOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.scanners.script.ScriptIdentifierOptions
import com.gallatinapps.syntaxmp.scanners.script.ScriptLikeScanner
import com.gallatinapps.syntaxmp.scanners.script.ScriptLikeScannerOptions
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.HeredocRule
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.SigilVariableInterpolationRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object PhpTokenizer : LanguageTokenizer {
    private val sigilInterpolation = listOf(SigilVariableInterpolationRule(prefixes = setOf('$')))
    private val stringFormats = listOf(PercentFormatSpecifierRule)
    private val scannerOptions = ScriptLikeScannerOptions(
        lineComments = LineCommentOptions(prefixes = setOf("#", "//")),
        blockComments = BlockCommentOptions.CLike,
        identifiers = ScriptIdentifierOptions(sigilVariablePrefixes = setOf('$')),
        hashBracketAnnotations = true,
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "namespace" to emptySet(),
                "use" to setOf("function", "const"),
            ),
            separators = setOf("\\"),
            wildcardMarkers = emptySet(),
            terminatorKeywords = setOf("as"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                HeredocRule(
                    operator = "<<<",
                    delimiterQuotes = setOf(null, '\'', '"'),
                    allowSemicolonTerminator = true,
                    bodyInterpolation = { delimiter ->
                        if (delimiter.quote == '\'') emptyList() else sigilInterpolation
                    },
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to sigilInterpolation),
                    formats = mapOf('"' to stringFormats, '\'' to stringFormats),
                ),
            ),
        ),
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ScriptLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = PhpKeywordRoles,
            constants = PhpConstants,
            typeKeywords = PhpTypeKeywords,
            builtinRoles = PhpBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
