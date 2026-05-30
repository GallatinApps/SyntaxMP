package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

/**
 * Tokenization input passed to a tokenizer implementation.
 *
 * @property code Original code string to tokenize.
 * @property languageId Resolved language id for this tokenizer invocation.
 */
public class SyntaxTokenizeRequest internal constructor(
    public val code: String,
    public val languageId: SyntaxLanguageId,
    private val embeddedLanguageTokenizer: (String, String) -> List<SyntaxTokenSpan>,
) {
    /**
     * Constructs a request not bound to a [SyntaxTokenizerEngine]. Useful for direct unit tests
     * of a tokenizer. [tokenizeEmbedded] returns an empty list on requests constructed this way.
     */
    public constructor(
        code: String,
        languageId: SyntaxLanguageId,
    ) : this(
        code = code,
        languageId = languageId,
        embeddedLanguageTokenizer = noEmbeddedTokenizer,
    )

    /**
     * Tokenizes [code] under [languageLabel] against the engine that created this request.
     *
     * Returns an empty list when the label does not resolve to a registered tokenizer, when the
     * embedded-language depth cap has been reached, or when this request was constructed manually.
     */
    public fun tokenizeEmbedded(code: String, languageLabel: String): List<SyntaxTokenSpan> =
        embeddedLanguageTokenizer(code, languageLabel)

    override fun toString(): String =
        "SyntaxTokenizeRequest(code=<${code.length} chars>, languageId=$languageId)"
}

private val noEmbeddedTokenizer: (String, String) -> List<SyntaxTokenSpan> =
    { _, _ -> emptyList() }
