package com.gallatinapps.syntaxmp.languages.diff

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object DiffTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(DiffScanner(request.code, request.languageId).scan())
}
