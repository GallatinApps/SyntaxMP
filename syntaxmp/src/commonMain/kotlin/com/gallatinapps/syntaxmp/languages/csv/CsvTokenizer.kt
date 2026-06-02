package com.gallatinapps.syntaxmp.languages.csv

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object CsvTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CsvScanner(request.code, request.languageId).scan()
}
