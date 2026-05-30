package com.gallatinapps.syntaxmp.languages.elixir

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object ElixirTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            ElixirScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = ElixirKeywordRoles,
                constants = ElixirConstants,
                builtinRoles = ElixirBuiltinRoles,
            ).scan(),
        )
}
