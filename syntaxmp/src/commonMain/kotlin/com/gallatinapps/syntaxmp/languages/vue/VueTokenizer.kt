package com.gallatinapps.syntaxmp.languages.vue

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions

internal object VueTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = VueRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        expressionLanguage = SyntaxLanguageId.JavaScript,
        expressionRules = listOf(
            MarkupExpressionRule(
                opener = "{{",
                closer = "}}",
                allowedAtTopLevel = true,
                allowedInsideMarkup = true,
                allowedInsideTag = false,
            ),
            MarkupExpressionRule(
                opener = "{",
                closer = "}",
                allowedAtTopLevel = false,
                allowedInsideMarkup = false,
                allowedInsideTag = true,
            ),
        ),
        directiveAttributes = MarkupDirectiveAttributeOptions.ComponentDirectives,
        preserveTagNameCase = true,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            MarkupScanner(
                request = request,
                options = scannerOptions,
            ).scan(),
        )
}
