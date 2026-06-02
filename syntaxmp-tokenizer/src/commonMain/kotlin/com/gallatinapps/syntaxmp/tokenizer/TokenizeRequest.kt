package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

/**
 * Tokenization input passed to a tokenizer implementation.
 *
 * @property code Original code string to tokenize.
 * @property languageId Resolved language id for this tokenizer invocation.
 */
public class TokenizeRequest internal constructor(
    public val code: String,
    public val languageId: LanguageId,
    private val embeddedLanguageTokenizer: (String, String) -> List<SyntaxTokenSpan>,
) {
    /**
     * Constructs a request not bound to a [SyntaxTokenizer]. Useful for direct unit tests
     * of a tokenizer. [tokenizeEmbedded] returns an empty list on requests constructed this way.
     */
    public constructor(
        code: String,
        languageId: LanguageId,
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
        "TokenizeRequest(code=<${code.length} chars>, languageId=$languageId)"
}

private val noEmbeddedTokenizer: (String, String) -> List<SyntaxTokenSpan> =
    { _, _ -> emptyList() }
