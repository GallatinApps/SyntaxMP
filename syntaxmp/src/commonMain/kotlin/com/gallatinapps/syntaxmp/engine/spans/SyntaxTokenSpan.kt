package com.gallatinapps.syntaxmp.engine.spans

import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

/**
 * UTF-16 code-unit span emitted by a syntax tokenizer.
 *
 * @property start Inclusive start offset in the original code string.
 * @property endExclusive Exclusive end offset in the original code string.
 * @property role Styling role for this span.
 * @property languageId Resolved language id that produced this span.
 */
public data class SyntaxTokenSpan(
    val start: Int,
    val endExclusive: Int,
    val role: SyntaxRole,
    val languageId: LanguageId,
)
