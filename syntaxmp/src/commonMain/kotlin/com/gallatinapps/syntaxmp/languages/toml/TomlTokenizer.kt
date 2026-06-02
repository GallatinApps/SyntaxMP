package com.gallatinapps.syntaxmp.languages.toml

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object TomlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> {
        val scanner = TomlScanner(
            code = request.code,
            language = request.languageId,
            constants = TomlConstants,
        )
        return scanner.scan()
    }
}
