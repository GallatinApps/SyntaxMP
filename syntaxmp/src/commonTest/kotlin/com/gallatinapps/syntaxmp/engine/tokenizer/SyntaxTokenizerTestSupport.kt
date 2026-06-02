package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal fun testEngine(
    extensions: List<LanguageExtension> = emptyList(),
): SyntaxTokenizer =
    SyntaxTokenizer(
        extensions = extensions,
    )

internal fun tokenizerWithRole(role: SyntaxRole): LanguageTokenizer =
    LanguageTokenizer { request ->
        listOf(
            SyntaxTokenSpan(
                start = 0,
                endExclusive = request.code.length,
                role = role,
                languageId = request.languageId,
            ),
        )
    }
