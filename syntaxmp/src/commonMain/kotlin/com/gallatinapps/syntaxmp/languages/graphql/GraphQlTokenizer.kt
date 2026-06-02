package com.gallatinapps.syntaxmp.languages.graphql

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object GraphQlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        GraphQlScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = GraphQlKeywordRoles,
            constants = GraphQlConstants,
        ).scan()
}
