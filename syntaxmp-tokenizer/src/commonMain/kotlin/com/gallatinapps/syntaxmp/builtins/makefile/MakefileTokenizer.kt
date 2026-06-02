package com.gallatinapps.syntaxmp.builtins.makefile

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object MakefileTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MakefileScanner(
            code = request.code,
            language = request.languageId,
            directiveRoles = MakefileDirectiveRoles,
        ).scan()
}
