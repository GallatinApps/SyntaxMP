package com.gallatinapps.syntaxmp.languages.astro

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions

internal object AstroTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = AstroRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        expressionLanguage = SyntaxLanguageId.JavaScript,
        frontMatterLanguage = SyntaxLanguageId.TypeScript,
        expressionRules = listOf(
            MarkupExpressionRule(
                opener = "{",
                closer = "}",
                allowedAtTopLevel = true,
                allowedInsideMarkup = true,
            ),
        ),
        directiveAttributes = MarkupDirectiveAttributeOptions.AstroDirectives,
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
