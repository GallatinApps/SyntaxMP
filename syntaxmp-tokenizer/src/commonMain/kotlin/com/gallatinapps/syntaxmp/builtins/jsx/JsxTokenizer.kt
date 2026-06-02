package com.gallatinapps.syntaxmp.builtins.jsx

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object JsxTokenizer : LanguageTokenizer {
    private val expressionDelimiterRole = SyntaxRole.Punctuation.Expression
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = JsxRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        startsInScript = true,
        expressionLanguage = LanguageId.JavaScript,
        expressionRules = listOf(
            MarkupExpressionRule(
                opener = "{",
                closer = "}",
                allowedAtTopLevel = false,
                allowedInsideMarkup = true,
                delimiterRole = expressionDelimiterRole,
            ),
        ),
        directiveAttributes = MarkupDirectiveAttributeOptions.ComponentDirectives,
        preserveTagNameCase = true,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkupScanner(
            request = request,
            options = scannerOptions,
        ).scan()
}
