package com.gallatinapps.syntaxmp.builtins.shell

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object ShellTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ShellScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = ShellKeywordRoles,
            builtinRoles = ShellBuiltinRoles,
        ).scan()
}
