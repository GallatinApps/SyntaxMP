package com.gallatinapps.syntaxmp.engine.scanners.markup

import com.gallatinapps.syntaxmp.engine.language.LanguageId

internal data class MarkupScannerOptions(
    val rawTextTags: Set<String> = emptySet(),
    val rawTextLanguageForTag: (tagName: String, lang: String?) -> String? =
        Companion::noRawTextLanguageForTag,
    val expressionRules: List<MarkupExpressionRule> = emptyList(),
    val startsInScript: Boolean = false,
    val expressionLanguage: LanguageId? = null,
    val frontMatterLanguage: LanguageId? = null,
    val directiveAttributes: MarkupDirectiveAttributeOptions = MarkupDirectiveAttributeOptions.None,
    val preserveTagNameCase: Boolean = false,
    val typeArgumentTags: Boolean = false,
) {
    val orderedExpressionRules: List<MarkupExpressionRule> =
        expressionRules.sortedByDescending { it.opener.length }

    init {
        require(expressionLanguage != null || (!startsInScript && expressionRules.isEmpty())) {
            "expressionLanguage must be non-null when startsInScript is true or expressionRules are configured"
        }
    }

    companion object {
        fun noRawTextLanguageForTag(
            tagName: String,
            lang: String?,
        ): String? = null

        fun defaultRawTextLanguageForTag(
            tagName: String,
            lang: String?,
        ): String? {
            val normalizedLang = lang?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            return when (tagName) {
                "script" -> normalizedLang ?: "javascript"
                "style" -> normalizedLang ?: "css"
                else -> null
            }
        }
    }
}
