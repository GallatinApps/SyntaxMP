package com.gallatinapps.syntaxmp.languages.css

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal data class CssScannerOptions(
    val variableRules: List<CssVariableRule> = emptyList(),
    val interpolation: CssInterpolationOptions = CssInterpolationOptions.None,
    val parentSelector: ParentSelectorOptions = ParentSelectorOptions.Disabled,
) {
    companion object {
        val Standard = CssScannerOptions()
    }
}

internal data class CssVariableRule(
    val prefix: Char,
    val role: SyntaxRole,
    val onlyWhenUnknownAtRule: Boolean = false,
)

internal data class CssInterpolationOptions(
    val opener: String?,
) {
    companion object {
        val None = CssInterpolationOptions(opener = null)
        val HashBrace = CssInterpolationOptions(opener = "#{")
    }
}

internal sealed interface ParentSelectorOptions {
    object Disabled : ParentSelectorOptions

    data class Enabled(
        val role: SyntaxRole,
    ) : ParentSelectorOptions
}
