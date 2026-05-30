package com.gallatinapps.syntaxmp.languages.html

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions

internal object HtmlTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = HtmlRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        preserveTagNameCase = false,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            MarkupScanner(
                request = request,
                options = scannerOptions,
            ).scan(),
        )
}
