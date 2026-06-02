package com.gallatinapps.syntaxmp.languages.diff

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object DiffTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        DiffScanner(request.code, request.languageId).scan()
}
