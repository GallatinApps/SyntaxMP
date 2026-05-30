package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

/**
 * Raw tokenizer output before engine normalization.
 *
 * @property spans Token spans produced by the tokenizer.
 */
public data class SyntaxTokenizeResult(
    val spans: List<SyntaxTokenSpan> = emptyList(),
)
