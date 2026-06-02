package com.gallatinapps.syntaxmp.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.gallatinapps.syntaxmp.compose.theme.SyntaxRoleStyles
import com.gallatinapps.syntaxmp.compose.theme.SyntaxStyle
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import kotlin.test.Test
import kotlin.test.assertEquals

class SyntaxStyledSpanTest {
    @Test
    fun syntaxStyleProjectsToSpanStyle() {
        val style = SyntaxStyle(
            color = Color.Blue,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
        )

        assertEquals(
            SpanStyle(
                color = Color.Blue,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
            ),
            style.toSpanStyle(),
        )
    }

    @Test
    fun textSpansSplitMultilineStylesForEditableSurfaces() {
        val code = "/*\r\n * comment\r\n */"
        val comment = SyntaxStyle(color = Color.Gray)
        val spans = buildSyntaxStyledSpans(
            code = code,
            spans = listOf(SyntaxTokenSpan(0, code.length, SyntaxRole.Comment, LanguageId.Kotlin)),
            theme = SyntaxTheme(
                roleStyles = SyntaxRoleStyles(
                    SyntaxRole.Comment to comment,
                ),
            ),
        )

        assertEquals(
            listOf(
                SyntaxStyledSpan(0, 2, comment.toSpanStyle()),
                SyntaxStyledSpan(4, 14, comment.toSpanStyle()),
                SyntaxStyledSpan(16, 19, comment.toSpanStyle()),
            ),
            spans,
        )
    }

    @Test
    fun bufferSpansClampRangesToCurrentText() {
        val keyword = SpanStyle(color = Color.Blue)
        val string = SpanStyle(color = Color.Red)

        val spans = syntaxStyledBufferSpans(
            textLength = 6,
            spans = listOf(
                SyntaxStyledSpan(-4, 3, keyword),
                SyntaxStyledSpan(4, 99, string),
                SyntaxStyledSpan(8, 12, keyword),
            ),
        )

        assertEquals(
            listOf(
                SyntaxStyledSpan(0, 3, keyword),
                SyntaxStyledSpan(4, 6, string),
            ),
            spans,
        )
    }

    @Test
    fun bufferSpansKeepOverlappingOrder() {
        val first = SpanStyle(color = Color.Blue)
        val second = SpanStyle(color = Color.Red)

        val spans = syntaxStyledBufferSpans(
            textLength = 8,
            spans = listOf(
                SyntaxStyledSpan(1, 6, first),
                SyntaxStyledSpan(3, 7, second),
            ),
        )

        assertEquals(
            listOf(
                SyntaxStyledSpan(1, 6, first),
                SyntaxStyledSpan(3, 7, second),
            ),
            spans,
        )
    }

    @Test
    fun bufferSpansHandleEmptyText() {
        val spans = syntaxStyledBufferSpans(
            textLength = 0,
            spans = listOf(
                SyntaxStyledSpan(0, 12, SpanStyle(color = Color.Blue)),
            ),
        )

        assertEquals(emptyList(), spans)
    }

    @Test
    fun annotatedStringAppliesOnlyTokenStyles() {
        val theme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Keyword to SyntaxStyle(color = Color.Blue),
                SyntaxRole.Number to SyntaxStyle(color = Color.Red),
            ),
        )
        val code = "val answer = 42"
        val annotatedString = buildSyntaxAnnotatedString(
            code = code,
            spans = listOf(
                SyntaxTokenSpan(0, 3, SyntaxRole.Keyword, LanguageId.Kotlin),
                SyntaxTokenSpan(13, 15, SyntaxRole.Number, LanguageId.Kotlin),
            ),
            theme = theme,
        )

        assertEquals(code, annotatedString.text)
        assertEquals(
            listOf(
                AnnotatedStyleRange(0, 3, SpanStyle(color = Color.Blue)),
                AnnotatedStyleRange(13, 15, SpanStyle(color = Color.Red)),
            ),
            annotatedString.spanStyles.map { range ->
                AnnotatedStyleRange(range.start, range.end, range.item)
            },
        )
    }
}

private data class AnnotatedStyleRange(
    val start: Int,
    val end: Int,
    val style: SpanStyle,
)
