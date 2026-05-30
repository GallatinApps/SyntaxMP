package com.gallatinapps.syntaxmp.languages.json5

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object Json5Tokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            Json5Scanner(
                code = request.code,
                language = request.languageId,
                constants = Json5Constants,
            ).scan(),
        )
}
