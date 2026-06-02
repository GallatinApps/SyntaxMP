package com.gallatinapps.syntaxmp.builtins.sql

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object SqlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        SqlScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = SqlKeywordRoles,
            constants = SqlConstants,
            typeKeywords = SqlTypeKeywords,
            builtinRoles = SqlBuiltinRoles,
        ).scan()
}
