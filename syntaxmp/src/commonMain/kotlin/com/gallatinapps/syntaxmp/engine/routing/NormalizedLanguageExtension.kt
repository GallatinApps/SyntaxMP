package com.gallatinapps.syntaxmp.engine.routing

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.language.normalizeLanguageValue
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer

internal data class NormalizedLanguageExtension(
    val languageId: SyntaxLanguageId,
    val aliases: Set<String>,
    val tokenizer: SyntaxTokenizer,
)

internal fun SyntaxLanguageExtension.normalized(): NormalizedLanguageExtension {
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
