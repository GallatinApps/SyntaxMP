package com.gallatinapps.syntaxmp.languages.makefile

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object MakefileTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MakefileScanner(
            code = request.code,
            language = request.languageId,
            directiveRoles = MakefileDirectiveRoles,
        ).scan()
}
