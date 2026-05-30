package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal fun testEngine(
    extensions: List<SyntaxLanguageExtension> = emptyList(),
): SyntaxTokenizerEngine =
    SyntaxTokenizerEngine(
        extensions = extensions,
    )

internal fun tokenizerWithRole(role: SyntaxRole): SyntaxTokenizer =
    SyntaxTokenizer { request ->
        SyntaxTokenizeResult(
            spans = listOf(
                SyntaxTokenSpan(
                    start = 0,
                    endExclusive = request.code.length,
                    role = role,
                    languageId = request.languageId,
                ),
            ),
        )
    }
