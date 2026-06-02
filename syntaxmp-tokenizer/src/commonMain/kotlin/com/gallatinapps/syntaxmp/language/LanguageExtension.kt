package com.gallatinapps.syntaxmp.language

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer

/**
 * Host-supplied language, aliases, and tokenizer for extending SyntaxMP.
 *
 * @property languageId Language id handled by [tokenizer].
 * @property aliases Optional labels that resolve to [languageId].
 * @property tokenizer Tokenizer used for [languageId].
 */
public data class LanguageExtension(
    val languageId: LanguageId,
    val aliases: Set<String> = emptySet(),
    val tokenizer: LanguageTokenizer,
)
