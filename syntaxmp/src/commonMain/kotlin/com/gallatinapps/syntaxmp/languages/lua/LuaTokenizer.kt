package com.gallatinapps.syntaxmp.languages.lua

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object LuaTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            LuaScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = LuaKeywordRoles,
                constants = LuaConstants,
                builtinRoles = LuaBuiltinRoles,
            ).scan(),
        )
}
