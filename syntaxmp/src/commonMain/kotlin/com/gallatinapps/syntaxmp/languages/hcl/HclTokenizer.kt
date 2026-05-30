package com.gallatinapps.syntaxmp.languages.hcl

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object HclTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            HclScanner(
                code = request.code,
                language = request.languageId,
                constants = HclConstants,
            ).scan(),
        )
}
