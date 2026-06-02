package com.gallatinapps.syntaxmp.languages.tsx

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object TsxTokenizer : LanguageTokenizer {
    private val expressionDelimiterRole = SyntaxRole.Punctuation.Expression
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = TsxRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        startsInScript = true,
        expressionLanguage = LanguageId.TypeScript,
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
        typeArgumentTags = true,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkupScanner(
            request = request,
            options = scannerOptions,
        ).scan()
}
