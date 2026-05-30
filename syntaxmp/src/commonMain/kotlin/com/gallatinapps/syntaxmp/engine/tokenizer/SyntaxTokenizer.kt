package com.gallatinapps.syntaxmp.engine.tokenizer

/**
 * Tokenizer contract for custom and built-in language implementations.
 *
 * Implementations must return UTF-16 offsets into [SyntaxTokenizeRequest.code]. They may emit
 * overlapping or out-of-order spans; [SyntaxTokenizerEngine] normalizes spans before returning
 * them to callers.
 *
 * Any [Throwable] thrown from [tokenize] is caught by [SyntaxTokenizerEngine] and translated to
 * empty spans for the affected snippet. Implementations that want to observe or log their own
 * failures should wrap their body in try/catch and return [SyntaxTokenizeResult] on error.
 */
public fun interface SyntaxTokenizer {
    /**
     * Tokenizes a code string for the resolved language.
     */
    public fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult
}
