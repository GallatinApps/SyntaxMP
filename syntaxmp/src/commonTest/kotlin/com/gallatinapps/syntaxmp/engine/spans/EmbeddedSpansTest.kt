package com.gallatinapps.syntaxmp.engine.spans

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbeddedSpansTest {
    @Test
    fun `append embedded spans offsets and clamps child spans`() {
        val tokens = mutableListOf<SyntaxTokenSpan>()
        val parentCode = "prechildpost"

        tokens.appendEmbeddedSpans(
            parentCode = parentCode,
            bodyStart = 3,
            bodyEnd = 8,
            languageLabel = "child",
            tokenizeEmbedded = { _, _ ->
                listOf(
                    SyntaxTokenSpan(-4, 2, SyntaxRole.Keyword, LanguageId.JavaScript),
                    SyntaxTokenSpan(1, 99, SyntaxRole.String, LanguageId.TypeScript),
                    SyntaxTokenSpan(4, 4, SyntaxRole.Number, LanguageId.JavaScript),
                )
            },
        )

        assertEquals(
            listOf(
                SyntaxTokenSpan(3, 5, SyntaxRole.Keyword, LanguageId.JavaScript),
                SyntaxTokenSpan(4, 8, SyntaxRole.String, LanguageId.TypeScript),
            ),
            tokens,
        )
    }

    @Test
    fun `append embedded spans ignores empty bodies`() {
        val tokens = mutableListOf<SyntaxTokenSpan>()

        tokens.appendEmbeddedSpans(
            parentCode = "body",
            bodyStart = 2,
            bodyEnd = 2,
            languageLabel = "child",
            tokenizeEmbedded = { _, _ ->
                listOf(SyntaxTokenSpan(0, 1, SyntaxRole.Keyword, LanguageId.Kotlin))
            },
        )

        assertTrue(tokens.isEmpty())
    }
}
