package com.gallatinapps.syntaxmp.builtins.graphql

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object GraphQlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        GraphQlScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = GraphQlKeywordRoles,
            constants = GraphQlConstants,
        ).scan()
}
