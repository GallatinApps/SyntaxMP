package com.gallatinapps.syntaxmp.languages.toml

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object TomlTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult {
        val scanner = TomlScanner(
            code = request.code,
            language = request.languageId,
            constants = TomlConstants,
        )
        return SyntaxTokenizeResult(scanner.scan())
    }
}
