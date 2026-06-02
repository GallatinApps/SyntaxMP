package com.gallatinapps.syntaxmp.languages.sql

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

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
