package com.gallatinapps.syntaxmp.languages.yaml

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object YamlTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult {
        val scanner = YamlScanner(
            code = request.code,
            language = request.languageId,
            constants = YamlConstants,
        )
        return SyntaxTokenizeResult(scanner.scan())
    }
}
