package com.gallatinapps.syntaxmp.languages.json

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object JsonTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult {
        val scanner = JsonScanner(
            code = request.code,
            language = request.languageId,
            constants = JsonConstants,
        )
        return SyntaxTokenizeResult(scanner.scan())
    }
}
