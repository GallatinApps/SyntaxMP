package com.gallatinapps.syntaxmp.builtins.json

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
