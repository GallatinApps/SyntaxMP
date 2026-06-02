package com.gallatinapps.syntaxmp.languages.css

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object CssTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CssScanner(
            code = request.code,
            language = request.languageId,
            atRules = CssAtRules,
            namedConstants = CssNamedConstants,
        ).scan()
}
