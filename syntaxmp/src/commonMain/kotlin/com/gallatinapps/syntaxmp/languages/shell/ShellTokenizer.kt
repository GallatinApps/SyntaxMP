package com.gallatinapps.syntaxmp.languages.shell

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object ShellTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ShellScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = ShellKeywordRoles,
            builtinRoles = ShellBuiltinRoles,
        ).scan()
}
