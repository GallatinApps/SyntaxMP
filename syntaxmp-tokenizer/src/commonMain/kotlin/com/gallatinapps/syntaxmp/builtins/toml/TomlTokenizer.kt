package com.gallatinapps.syntaxmp.builtins.toml

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
