package com.gallatinapps.syntaxmp.routing

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.language.normalizeLanguageValue
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer

internal data class NormalizedLanguageExtension(
    val languageId: LanguageId,
    val aliases: Set<String>,
    val tokenizer: LanguageTokenizer,
)

internal fun LanguageExtension.normalized(): NormalizedLanguageExtension {
    val normalizedAliases = aliases.mapNotNull { alias ->
        alias.normalizeLanguageLabel()
    }.toSet()
    return NormalizedLanguageExtension(
        languageId = languageId,
        aliases = normalizedAliases,
        tokenizer = tokenizer,
    )
}

internal fun String?.normalizeLanguageLabel(): String? =
    normalizeLanguageValue()
