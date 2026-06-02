package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.appendEmbeddedSpans
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyntaxTokenizerEmbeddedTest {
    @Test
    fun `engine-backed embedded routing returns empty spans for unknown language`() {
        val tokens = mutableListOf<SyntaxTokenSpan>()
        val engine = SyntaxTokenizer()

        tokens.appendEmbeddedSpans(
            parentCode = "prechildpost",
            bodyStart = 3,
            bodyEnd = 8,
            languageLabel = "madeuplang",
            tokenizeEmbedded = { code, language ->
                engine.tokenize(code = code, languageLabel = language)
            },
        )

        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `engine-backed embedded routing returns empty spans for tokenizer failures`() {
        val childLanguage = LanguageId.fromString("child")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = childLanguage,
                    tokenizer = LanguageTokenizer { error("child boom") },
                ),
            ),
        )
        val tokens = mutableListOf<SyntaxTokenSpan>()

        tokens.appendEmbeddedSpans(
            parentCode = "prechildpost",
            bodyStart = 3,
            bodyEnd = 8,
            languageLabel = childLanguage.value,
            tokenizeEmbedded = { code, language ->
                engine.tokenize(code = code, languageLabel = language)
            },
        )

        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `engine-backed embedded routing can resolve extension languages`() {
        val childLanguage = LanguageId.fromString("mydsl")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = childLanguage,
                    tokenizer = LanguageTokenizer { request ->
                        listOf(
                            SyntaxTokenSpan(
                                start = request.code.indexOf("token"),
                                endExclusive = request.code.indexOf("token") + "token".length,
                                role = SyntaxRole.Function,
                                languageId = request.languageId,
                            ),
                        )
                    },
                ),
            ),
        )
        val tokens = mutableListOf<SyntaxTokenSpan>()

        tokens.appendEmbeddedSpans(
            parentCode = "pre token post",
            bodyStart = 4,
            bodyEnd = 9,
            languageLabel = childLanguage.value,
            tokenizeEmbedded = { code, language ->
                engine.tokenize(code = code, languageLabel = language)
            },
        )

        assertEquals(
            listOf(SyntaxTokenSpan(4, 9, SyntaxRole.Function, childLanguage)),
            tokens,
        )
    }

    @Test
    fun `extension tokenizer can call tokenizeEmbedded to route to another extension`() {
        val hostLanguage = LanguageId.fromString("template-host")
        val childLanguage = LanguageId.fromString("template-child")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = hostLanguage,
                    tokenizer = LanguageTokenizer { request ->
                        request.tokenizeEmbedded(code = "inner", languageLabel = "template-child")
                    },
                ),
                LanguageExtension(
                    languageId = childLanguage,
                    tokenizer = LanguageTokenizer { request ->
                        listOf(
                            SyntaxTokenSpan(
                                start = 0,
                                endExclusive = request.code.length,
                                role = SyntaxRole.Function,
                                languageId = request.languageId,
                            ),
                        )
                    },
                ),
            ),
        )

        val span = engine.tokenize(code = "{{ inner }}", languageLabel = "template-host").single()

        assertEquals(SyntaxRole.Function, span.role)
        assertEquals(childLanguage, span.languageId)
    }

    @Test
    fun `manually constructed request returns empty embedded spans`() {
        val request = TokenizeRequest(
            code = "host",
            languageId = LanguageId.Markdown,
        )

        assertTrue(request.tokenizeEmbedded(code = "const value = 1", languageLabel = "javascript").isEmpty())
    }

    @Test
    fun `embedded depth cap remains three`() {
        val first = LanguageId.fromString("depth-one")
        val second = LanguageId.fromString("depth-two")
        val third = LanguageId.fromString("depth-three")
        val fourth = LanguageId.fromString("depth-four")
        val fifth = LanguageId.fromString("depth-five")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                depthTokenizer(first, SyntaxRole.Keyword, 0, "depth-two"),
                depthTokenizer(second, SyntaxRole.String, 1, "depth-three"),
                depthTokenizer(third, SyntaxRole.Number, 2, "depth-four"),
                depthTokenizer(fourth, SyntaxRole.Comment, 3, "depth-five"),
                depthTokenizer(fifth, SyntaxRole.Operator, 4, null),
            ),
        )

        val spans = engine.tokenize(code = "abcde", languageLabel = "depth-one")

        assertTrue(spans.any { it.languageId == first && it.role == SyntaxRole.Keyword })
        assertTrue(spans.any { it.languageId == second && it.role == SyntaxRole.String })
        assertTrue(spans.any { it.languageId == third && it.role == SyntaxRole.Number })
        assertTrue(spans.any { it.languageId == fourth && it.role == SyntaxRole.Comment })
        assertFalse(spans.any { it.languageId == fifth && it.role == SyntaxRole.Operator })
    }

    private fun depthTokenizer(
        languageId: LanguageId,
        role: SyntaxRole,
        start: Int,
        nextLanguageLabel: String?,
    ): LanguageExtension =
        LanguageExtension(
            languageId = languageId,
            tokenizer = LanguageTokenizer { request ->
                val ownSpan = SyntaxTokenSpan(
                    start = start,
                    endExclusive = start + 1,
                    role = role,
                    languageId = request.languageId,
                )
                val embeddedSpans = nextLanguageLabel
                    ?.let { request.tokenizeEmbedded(code = request.code, languageLabel = it) }
                    .orEmpty()
                listOf(ownSpan) + embeddedSpans
            },
        )
}
