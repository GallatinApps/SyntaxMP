package com.gallatinapps.syntaxmp.languages.json

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object JsonTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> {
        val scanner = JsonScanner(
            code = request.code,
            language = request.languageId,
            constants = JsonConstants,
        )
        return scanner.scan()
    }
}
