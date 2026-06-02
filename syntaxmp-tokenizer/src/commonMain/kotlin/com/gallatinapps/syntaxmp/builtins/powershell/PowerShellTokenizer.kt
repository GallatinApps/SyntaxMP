package com.gallatinapps.syntaxmp.builtins.powershell

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
