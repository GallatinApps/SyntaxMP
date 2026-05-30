package com.gallatinapps.syntaxmp.languages.scss

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.css.CssAtRules
import com.gallatinapps.syntaxmp.languages.css.CssInterpolationOptions
import com.gallatinapps.syntaxmp.languages.css.CssNamedConstants
import com.gallatinapps.syntaxmp.languages.css.CssScanner
import com.gallatinapps.syntaxmp.languages.css.CssScannerOptions
import com.gallatinapps.syntaxmp.languages.css.CssVariableRule
import com.gallatinapps.syntaxmp.languages.css.ParentSelectorOptions

internal object ScssTokenizer {
    private val scannerOptions = CssScannerOptions(
        variableRules = listOf(
            CssVariableRule(
                prefix = '$',
                role = SyntaxRole.Variable,
            ),
        ),
        interpolation = CssInterpolationOptions.HashBrace,
        parentSelector = ParentSelectorOptions.Enabled(
            role = SyntaxRole.Attribute.append("parent-selector"),
        ),
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CssScanner(
                code = request.code,
                language = request.languageId,
                atRules = CssAtRules,
                namedConstants = CssNamedConstants,
                preprocessorKeywordRoles = ScssPreprocessorKeywordRoles,
                options = scannerOptions,
            ).scan(),
        )
}
