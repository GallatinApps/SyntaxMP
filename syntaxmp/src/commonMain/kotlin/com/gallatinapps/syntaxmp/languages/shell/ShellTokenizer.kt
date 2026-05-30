package com.gallatinapps.syntaxmp.languages.shell

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object ShellTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            ShellScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = ShellKeywordRoles,
                builtinRoles = ShellBuiltinRoles,
            ).scan(),
        )
}
