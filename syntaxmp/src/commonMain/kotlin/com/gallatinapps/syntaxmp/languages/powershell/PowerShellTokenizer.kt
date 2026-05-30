package com.gallatinapps.syntaxmp.languages.powershell

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object PowerShellTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            PowerShellScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = PowerShellKeywordRoles,
                constants = PowerShellConstants,
                dashOperators = PowerShellDashOperators,
            ).scan(),
        )
}
