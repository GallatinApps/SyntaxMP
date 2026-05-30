package com.gallatinapps.syntaxmp.languages.makefile

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object MakefileTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            MakefileScanner(
                code = request.code,
                language = request.languageId,
                directiveRoles = MakefileDirectiveRoles,
            ).scan(),
        )
}
