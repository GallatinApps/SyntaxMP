package com.gallatinapps.syntaxmp.languages.yaml

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object YamlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> {
        val scanner = YamlScanner(
            code = request.code,
            language = request.languageId,
            constants = YamlConstants,
        )
        return scanner.scan()
    }
}
