package com.gallatinapps.syntaxmp.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.SpanStyle
import com.gallatinapps.syntaxmp.compose.theme.SyntaxRoleStyles
import com.gallatinapps.syntaxmp.compose.theme.SyntaxStyle
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class RememberSyntaxAnnotatedStringTest {
    @Test
    fun nullLanguageBuildsPlainAnnotatedStringWithoutTokenizing() = runComposeUiTest {
        var didTokenize = false
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = EqualSpanLanguage,
                    tokenizer = LanguageTokenizer {
                        didTokenize = true
                        emptyList()
                    },
                ),
            ),
        )
        var renderedText = ""
        var renderedRanges = emptyList<RenderedStyleRange>()

        setContent {
            val annotated = rememberSyntaxAnnotatedString(
                code = "plain text",
                languageLabel = null,
                engine = engine,
                theme = theme(commentColor = Color.Blue),
            )
            renderedText = annotated.text
            renderedRanges = annotated.styleRanges()
        }
        waitForIdle()

        assertEquals("plain text", renderedText)
        assertEquals(emptyList(), renderedRanges)
        assertFalse(didTokenize)
    }

    @Test
    fun blankLanguageBuildsPlainAnnotatedStringWithoutTokenizing() = runComposeUiTest {
        var didTokenize = false
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = EqualSpanLanguage,
                    aliases = setOf("equal"),
                    tokenizer = LanguageTokenizer {
                        didTokenize = true
                        emptyList()
                    },
                ),
            ),
        )
        var renderedText = ""
        var renderedRanges = emptyList<RenderedStyleRange>()

        setContent {
            val annotated = rememberSyntaxAnnotatedString(
                code = "plain text",
                languageLabel = " ",
                engine = engine,
                theme = theme(commentColor = Color.Blue),
            )
            renderedText = annotated.text
            renderedRanges = annotated.styleRanges()
        }
        waitForIdle()

        assertEquals("plain text", renderedText)
        assertEquals(emptyList(), renderedRanges)
        assertFalse(didTokenize)
    }

    @Test
    fun restylesWithoutRetokenizingAndRebuildsForEqualTokenSpans() = runComposeUiTest {
        var tokenizeCount = 0
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = EqualSpanLanguage,
                    tokenizer = LanguageTokenizer { request ->
                        tokenizeCount += 1

                            listOf(
                                SyntaxTokenSpan(
                                    start = 0,
                                    endExclusive = request.code.length,
                                    role = SyntaxRole.Comment,
                                    languageId = request.languageId,
                                ),
                            )
                    },
                ),
            ),
        )
        var code by mutableStateOf("// foo")
        var theme by mutableStateOf(theme(commentColor = Color.Blue))
        var renderedRanges = emptyList<RenderedStyleRange>()
        var renderedText = ""

        setContent {
            val annotated = rememberSyntaxAnnotatedString(
                code = code,
                languageLabel = EqualSpanLanguage.value,
                engine = engine,
                theme = theme,
            )
            renderedText = annotated.text
            renderedRanges = annotated.styleRanges()
        }
        waitForIdle()

        assertEquals(1, tokenizeCount)
        assertEquals("// foo", renderedText)
        assertEquals(
            listOf(
                RenderedStyleRange(0, 6, SpanStyle(color = Color.Blue)),
            ),
            renderedRanges,
        )

        theme = theme(commentColor = Color.Red)
        waitForIdle()
        assertEquals(1, tokenizeCount)
        assertEquals(
            listOf(
                RenderedStyleRange(0, 6, SpanStyle(color = Color.Red)),
            ),
            renderedRanges,
        )

        code = "// bar"
        waitForIdle()
        assertEquals(2, tokenizeCount)
        assertEquals("// bar", renderedText)
        assertEquals(
            listOf(
                RenderedStyleRange(0, 6, SpanStyle(color = Color.Red)),
            ),
            renderedRanges,
        )
    }

    private fun theme(commentColor: Color): SyntaxTheme =
        SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Comment to SyntaxStyle(color = commentColor),
            ),
        )

    private fun androidx.compose.ui.text.AnnotatedString.styleRanges(): List<RenderedStyleRange> =
        spanStyles.map { range ->
            RenderedStyleRange(
                start = range.start,
                end = range.end,
                style = range.item,
            )
        }

    private companion object {
        val EqualSpanLanguage = LanguageId.fromString("equal")
    }
}

private data class RenderedStyleRange(
    val start: Int,
    val end: Int,
    val style: SpanStyle,
)
