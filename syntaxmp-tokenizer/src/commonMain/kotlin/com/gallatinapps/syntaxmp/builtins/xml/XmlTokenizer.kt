package com.gallatinapps.syntaxmp.builtins.xml

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
