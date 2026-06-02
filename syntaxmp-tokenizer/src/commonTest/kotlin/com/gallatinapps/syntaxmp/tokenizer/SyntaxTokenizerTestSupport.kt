package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

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
