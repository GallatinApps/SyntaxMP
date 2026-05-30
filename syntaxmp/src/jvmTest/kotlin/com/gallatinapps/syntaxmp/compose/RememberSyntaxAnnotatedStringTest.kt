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
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class RememberSyntaxAnnotatedStringTest {
    @Test
    fun nullLanguageBuildsPlainAnnotatedStringWithoutTokenizing() = runComposeUiTest {
        var didTokenize = false
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = EqualSpanLanguage,
                    tokenizer = SyntaxTokenizer {
                        didTokenize = true
                        SyntaxTokenizeResult()
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
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = EqualSpanLanguage,
                    aliases = setOf("equal"),
                    tokenizer = SyntaxTokenizer {
                        didTokenize = true
                        SyntaxTokenizeResult()
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
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = EqualSpanLanguage,
                    tokenizer = SyntaxTokenizer { request ->
                        tokenizeCount += 1
                        SyntaxTokenizeResult(
                            spans = listOf(
                                SyntaxTokenSpan(
                                    start = 0,
                                    endExclusive = request.code.length,
                                    role = SyntaxRole.Comment,
                                    languageId = request.languageId,
                                ),
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
        val EqualSpanLanguage = SyntaxLanguageId.fromString("equal")
    }
}

private data class RenderedStyleRange(
    val start: Int,
    val end: Int,
    val style: SpanStyle,
)
