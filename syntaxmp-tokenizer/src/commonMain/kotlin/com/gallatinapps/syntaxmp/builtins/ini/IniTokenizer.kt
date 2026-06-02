package com.gallatinapps.syntaxmp.builtins.ini

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object IniTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        IniScanner(
            code = request.code,
            language = request.languageId,
            constants = IniConstants,
        ).scan()
}
