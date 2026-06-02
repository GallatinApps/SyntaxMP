package com.gallatinapps.syntaxmp.languages.powershell

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object PowerShellTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        PowerShellScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = PowerShellKeywordRoles,
            constants = PowerShellConstants,
            dashOperators = PowerShellDashOperators,
        ).scan()
}
