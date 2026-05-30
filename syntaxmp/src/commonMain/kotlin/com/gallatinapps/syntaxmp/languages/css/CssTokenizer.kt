package com.gallatinapps.syntaxmp.languages.css

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object CssTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CssScanner(
                code = request.code,
                language = request.languageId,
                atRules = CssAtRules,
                namedConstants = CssNamedConstants,
            ).scan(),
        )
}
