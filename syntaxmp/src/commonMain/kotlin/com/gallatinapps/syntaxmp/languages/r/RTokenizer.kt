package com.gallatinapps.syntaxmp.languages.r

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object RTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            RScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = RKeywordRoles,
                constants = RConstants,
                builtinRoles = RBuiltinRoles,
            ).scan(),
        )
}
