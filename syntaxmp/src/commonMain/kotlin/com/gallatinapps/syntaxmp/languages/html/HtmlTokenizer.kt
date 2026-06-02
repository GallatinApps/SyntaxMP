package com.gallatinapps.syntaxmp.languages.html

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object HtmlTokenizer : LanguageTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = HtmlRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        preserveTagNameCase = false,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkupScanner(
            request = request,
            options = scannerOptions,
        ).scan()
}
