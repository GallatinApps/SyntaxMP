package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

/**
 * Tokenizer contract for custom and built-in language implementations.
 *
 * Implementations must return UTF-16 offsets into [TokenizeRequest.code]. They may emit
 * overlapping or out-of-order spans; [SyntaxTokenizer] normalizes spans before returning
 * them to callers.
 *
 * Any [Throwable] thrown from [tokenize] is caught by [SyntaxTokenizer] and translated to
 * empty spans for the affected snippet. Implementations that want to observe or log their own
 * failures should wrap their body in try/catch and return an empty list on error.
 */
public fun interface LanguageTokenizer {
    /**
     * Tokenizes a code string for the resolved language.
     */
    public fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan>
}
