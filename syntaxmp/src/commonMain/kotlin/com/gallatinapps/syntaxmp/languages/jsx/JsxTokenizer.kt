package com.gallatinapps.syntaxmp.languages.jsx

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

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
