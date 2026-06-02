package com.gallatinapps.syntaxmp.engine.primitives.numbers

/**
 * Internal implementation contract used by shared scanners to delegate numeric literal
 * recognition. Not part of the public v1 API.
 *
 * Hosts adding custom languages via LanguageExtension write their own LanguageTokenizer and
 * do their own number scanning inside it; they do not plug into this interface.
 *
 * Implementations are expected to be pure functions of (code, start): no internal state, no side
 * effects, and safe to share across scanner instances.
 */
internal fun interface NumericLiteralScanner {
    /**
     * Decides where a numeric literal ends in [code], starting at [start].
     * Returns the exclusive end index, or null if no numeric literal is present at [start].
     */
    fun scan(code: String, start: Int): Int?
}

internal enum class OctalMode {
    /** C-style: a leading `0` followed by digits is octal (`0644`). */
    LeadingZero,

    /** Modern: `0o644` is octal. */
    ZeroOhPrefix,
}
