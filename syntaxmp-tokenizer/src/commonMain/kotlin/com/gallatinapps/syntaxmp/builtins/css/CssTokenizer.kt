package com.gallatinapps.syntaxmp.builtins.css

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object CssTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CssScanner(
            code = request.code,
            language = request.languageId,
            atRules = CssAtRules,
            namedConstants = CssNamedConstants,
        ).scan()
}
