package com.gallatinapps.syntaxmp.engine.language

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer

/**
 * Host-supplied language, aliases, and tokenizer for extending SyntaxMP.
 *
 * @property languageId Language id handled by [tokenizer].
 * @property aliases Optional labels that resolve to [languageId].
 * @property tokenizer Tokenizer used for [languageId].
 */
public data class SyntaxLanguageExtension(
    val languageId: SyntaxLanguageId,
    val aliases: Set<String> = emptySet(),
    val tokenizer: SyntaxTokenizer,
)
