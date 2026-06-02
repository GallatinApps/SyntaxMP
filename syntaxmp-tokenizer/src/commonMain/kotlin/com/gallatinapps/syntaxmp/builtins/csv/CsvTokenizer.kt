package com.gallatinapps.syntaxmp.builtins.csv

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object CsvTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CsvScanner(request.code, request.languageId).scan()
}
