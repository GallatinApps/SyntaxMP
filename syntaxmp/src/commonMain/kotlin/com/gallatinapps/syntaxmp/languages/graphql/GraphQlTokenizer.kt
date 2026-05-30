package com.gallatinapps.syntaxmp.languages.graphql

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object GraphQlTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            GraphQlScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = GraphQlKeywordRoles,
                constants = GraphQlConstants,
            ).scan(),
        )
}
