package com.gallatinapps.syntaxmp.builtins.yaml

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
