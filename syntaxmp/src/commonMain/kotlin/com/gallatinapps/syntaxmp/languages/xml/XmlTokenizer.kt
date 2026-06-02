package com.gallatinapps.syntaxmp.languages.xml

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object XmlTokenizer : LanguageTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        preserveTagNameCase = true,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkupScanner(
            request = request,
            options = scannerOptions,
        ).scan()
}
