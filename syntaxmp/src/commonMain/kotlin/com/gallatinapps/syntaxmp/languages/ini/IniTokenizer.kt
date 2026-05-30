package com.gallatinapps.syntaxmp.languages.ini

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object IniTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            IniScanner(
                code = request.code,
                language = request.languageId,
                constants = IniConstants,
            ).scan(),
        )
}
