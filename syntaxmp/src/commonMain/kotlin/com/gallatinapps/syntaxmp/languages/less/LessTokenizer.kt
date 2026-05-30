package com.gallatinapps.syntaxmp.languages.less

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.css.CssScanner
import com.gallatinapps.syntaxmp.languages.css.CssScannerOptions
import com.gallatinapps.syntaxmp.languages.css.CssVariableRule

internal object LessTokenizer {
    private val scannerOptions = CssScannerOptions(
        variableRules = listOf(
            CssVariableRule(
                prefix = '@',
                role = SyntaxRole.Variable,
                onlyWhenUnknownAtRule = true,
            ),
        ),
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CssScanner(
                code = request.code,
                language = request.languageId,
                atRules = LessAtRules,
                namedConstants = LessNamedConstants,
                options = scannerOptions,
            ).scan(),
        )
}
