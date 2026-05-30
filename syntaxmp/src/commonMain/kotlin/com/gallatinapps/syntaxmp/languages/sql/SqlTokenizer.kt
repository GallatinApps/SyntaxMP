package com.gallatinapps.syntaxmp.languages.sql

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object SqlTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            SqlScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = SqlKeywordRoles,
                constants = SqlConstants,
                typeKeywords = SqlTypeKeywords,
                builtinRoles = SqlBuiltinRoles,
            ).scan(),
        )
}
