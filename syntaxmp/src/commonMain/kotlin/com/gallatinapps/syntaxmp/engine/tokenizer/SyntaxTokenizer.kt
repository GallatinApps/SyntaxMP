package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.language.normalizeLanguageValue
import com.gallatinapps.syntaxmp.engine.routing.builtInTokenizers
import com.gallatinapps.syntaxmp.engine.routing.normalized
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.spans.normalizeTokenSpans

private const val MaxEmbeddedDepth = 3

/**
 * Routes code snippets to built-in and host-supplied syntax tokenizers.
 *
 * @param builtInLanguages Built-in languages enabled for this engine.
 * @param extensions Host-supplied tokenizer extensions checked before built-in tokenizers.
 */
public class SyntaxTokenizer(
    builtInLanguages: Set<LanguageId> = LanguageId.BuiltIns,
    extensions: List<LanguageExtension> = emptyList(),
) {
    private val enabledBuiltInLanguages = builtInLanguages.toSet()
    private val builtInTokenizerMap = builtInTokenizers()
        .filterKeys { it in enabledBuiltInLanguages }
    private val normalizedExtensions = extensions.map { it.normalized() }

    /**
     * Resolves a raw language label to the canonical language id this engine would tokenize.
     *
     * Walks extension exact ids, extension aliases, then built-in aliases and ids. Returns an
     * exact custom id for an unknown non-blank label. Returns `null` for `null` or blank input.
     */
    public fun resolveLanguageId(languageLabel: String?): LanguageId? {
        val normalized = languageLabel.normalizeLanguageValue() ?: return null
        normalizedExtensions.firstOrNull { extension ->
            normalized == extension.languageId.value
        }?.let { return it.languageId }
        normalizedExtensions.firstOrNull { extension ->
            normalized in extension.aliases
        }?.let { return it.languageId }
        return LanguageId.resolve(normalized)
    }

    /**
     * Tokenizes [code] using [languageLabel] as a raw language label.
     *
     * @return Empty spans when [languageLabel] is null, empty, unregistered, or fails to tokenize.
     */
    public fun tokenize(
        code: String,
        languageLabel: String?,
    ): List<SyntaxTokenSpan> =
        tokenizeResolved(code = code, languageId = resolveLanguageId(languageLabel), depth = 0)

    private fun tokenizeResolved(
        code: String,
        languageId: LanguageId?,
        depth: Int,
    ): List<SyntaxTokenSpan> {
        if (languageId == null) return emptyList()
        val tokenizer = resolveTokenizer(languageId) ?: return emptyList()
        if (code.isEmpty()) {
            return emptyList()
        }

        val embeddedLanguageTokenizer: (String, String) -> List<SyntaxTokenSpan> = if (depth < MaxEmbeddedDepth) {
            { embeddedCode, embeddedLabel ->
                tokenizeResolved(
                    code = embeddedCode,
                    languageId = resolveLanguageId(embeddedLabel),
                    depth = depth + 1,
                )
            }
        } else {
            { _, _ -> emptyList() }
        }
        val request = TokenizeRequest(
            code = code,
            languageId = languageId,
            embeddedLanguageTokenizer = embeddedLanguageTokenizer,
        )
        val spans = try {
            tokenizer.tokenize(request)
        } catch (_: Throwable) {
            emptyList()
        }
        return normalizeTokenSpans(
            spans = spans,
            codeLength = code.length,
        )
    }

    private fun resolveTokenizer(languageId: LanguageId): LanguageTokenizer? {
        for (extension in normalizedExtensions) {
            if (languageId == extension.languageId) {
                return extension.tokenizer
            }
        }

        return builtInTokenizerMap[languageId]
    }
}
