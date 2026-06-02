package com.gallatinapps.syntaxmp.engine.routing

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.language.normalizeLanguageValue
import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer

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
