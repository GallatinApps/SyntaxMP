package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.language.builtInLanguageLabelsFor
import com.gallatinapps.syntaxmp.language.trimAndLowercaseOrNull
import com.gallatinapps.syntaxmp.routing.builtInTokenizers
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.spans.normalizeTokenSpans

private const val MaxEmbeddedDepth = 3

/**
 * Routes code snippets to built-in and host-supplied syntax tokenizers.
 *
 * @param builtInLanguages Built-in languages enabled for this engine.
 * @param extensions Host-supplied tokenizer extensions checked before built-in tokenizers.
 */
public class SyntaxTokenizer(
    builtInLanguages: Set<LanguageId> = LanguageId.BuiltIns,
    private val extensions: List<LanguageExtension> = emptyList(),
) {
    init {
        val invalidBuiltInLanguages = builtInLanguages - LanguageId.BuiltIns
        require(invalidBuiltInLanguages.isEmpty()) {
            val invalidValues = invalidBuiltInLanguages
                .sortedBy { it.value }
                .joinToString { it.value }
            "builtInLanguages must be a subset of LanguageId.BuiltIns. " +
                "Custom languages must be registered through LanguageExtension. " +
                "Invalid values: $invalidValues."
        }
    }

    private val enabledBuiltInLanguages = builtInLanguages.toSet()
    private val builtInTokenizerMap = builtInTokenizers()
        .filterKeys { it in enabledBuiltInLanguages }
    private val languageLabelMap = buildLanguageLabelMap()

    /** Language ids this tokenizer instance can tokenize. */
    public val languageIds: Set<LanguageId> =
        enabledBuiltInLanguages + extensions.map { it.languageId }

    /** Normalized language labels this tokenizer instance recognizes. */
    public val languageLabels: Set<String> = languageLabelMap.keys.toSet()

    /**
     * Resolves a raw language label to the canonical language id this engine would tokenize.
     *
     * Walks labels active for this tokenizer instance. Returns `null` for `null`, blank, or
     * unregistered labels.
     */
    public fun resolveLanguageId(languageLabel: String?): LanguageId? {
        val labelKey = languageLabel.trimAndLowercaseOrNull() ?: return null
        return languageLabelMap[labelKey]
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
        for (extension in extensions) {
            if (languageId == extension.languageId) {
                return extension.tokenizer
            }
        }

        return builtInTokenizerMap[languageId]
    }

    private fun buildLanguageLabelMap(): Map<String, LanguageId> {
        val labels = mutableMapOf<String, LanguageId>()
        enabledBuiltInLanguages.forEach { languageId ->
            builtInLanguageLabelsFor(languageId).forEach { label ->
                labels[label] = languageId
            }
        }

        val extensionAliases = mutableMapOf<String, LanguageId>()
        extensions.forEach { extension ->
            extension.aliases.forEach { alias ->
                val aliasKey = alias.trimAndLowercaseOrNull()
                if (aliasKey != null && aliasKey !in extensionAliases) {
                    extensionAliases[aliasKey] = extension.languageId
                }
            }
        }
        labels.putAll(extensionAliases)

        extensions.forEach { extension ->
            labels[extension.languageId.value] = extension.languageId
        }

        return labels
    }
}
