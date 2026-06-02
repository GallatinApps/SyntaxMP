package com.gallatinapps.syntaxmp.builtins.html

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
