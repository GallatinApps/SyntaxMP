package com.gallatinapps.syntaxmp.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class SyntaxThemeTest {
    @Test
    fun roleStylesFactoryAndHelperReplaceOnlyRequestedRole() {
        val keyword = SyntaxStyle(color = Color.Blue)
        val string = SyntaxStyle(color = Color.Green)
        val number = SyntaxStyle(color = Color.Red)
        val styles = SyntaxRoleStyles(
            SyntaxRole.Keyword to keyword,
            SyntaxRole.String to string,
        )

        val updated = styles.withRoleStyle(SyntaxRole.Number, number)

        assertEquals(keyword, updated[SyntaxRole.Keyword])
        assertEquals(string, updated[SyntaxRole.String])
        assertEquals(number, updated[SyntaxRole.Number])
        assertEquals(emptyMap(), SyntaxRoleStyles())
    }

    @Test
    fun themeResolvesGlobalRootToExactCascade() {
        val exactRole = SyntaxRole.Constant.Builtin.append("true")
        val theme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Constant to SyntaxStyle(color = Color.Blue),
                SyntaxRole.Constant.Builtin to SyntaxStyle(fontWeight = FontWeight.Bold),
                exactRole to SyntaxStyle(fontStyle = FontStyle.Italic),
            ),
        )

        assertEquals(
            SyntaxStyle(
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
            ),
            theme.resolveStyle(exactRole),
        )
    }

    @Test
    fun languageOverridesCascadeAfterGlobalStylesPerField() {
        val exactRole = SyntaxRole.Keyword.Control.append("return")
        val theme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Keyword to SyntaxStyle(color = Color.Blue),
                SyntaxRole.Keyword.Control to SyntaxStyle(fontWeight = FontWeight.Bold),
                exactRole to SyntaxStyle(fontStyle = FontStyle.Italic),
            ),
            languageOverrides = mapOf(
                SyntaxLanguageId.Kotlin to SyntaxRoleStyles(
                    SyntaxRole.Keyword to SyntaxStyle(color = Color.Red),
                    SyntaxRole.Keyword.Control to SyntaxStyle(
                        color = Color.Unspecified,
                        fontWeight = FontWeight.Medium,
                    ),
                ),
            ),
        )

        assertEquals(
            SyntaxStyle(
                color = Color.Red,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
            ),
            theme.resolveStyle(exactRole, SyntaxLanguageId.Kotlin),
        )
        assertEquals(
            SyntaxStyle(
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
            ),
            theme.resolveStyle(exactRole, SyntaxLanguageId.Swift),
        )
        assertEquals(SyntaxStyle(), theme.resolveStyle(SyntaxRole.String, SyntaxLanguageId.Kotlin))
    }

    @Test
    fun resolveSpanStyleUsesTheSpanLanguage() {
        val theme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Keyword to SyntaxStyle(color = Color.Blue),
            ),
            languageOverrides = mapOf(
                SyntaxLanguageId.Kotlin to SyntaxRoleStyles(
                    SyntaxRole.Keyword to SyntaxStyle(color = Color.Red),
                ),
            ),
        )

        assertEquals(
            Color.Red,
            theme.resolveSpanStyle(
                SyntaxTokenSpan(0, 3, SyntaxRole.Keyword, SyntaxLanguageId.Kotlin),
            ).color,
        )
        assertEquals(
            Color.Blue,
            theme.resolveSpanStyle(
                SyntaxTokenSpan(0, 3, SyntaxRole.Keyword, SyntaxLanguageId.Swift),
            ).color,
        )
    }

    @Test
    fun themeHelpersPreserveSurroundingMaps() {
        val keyword = SyntaxStyle(color = Color.Blue)
        val string = SyntaxStyle(color = Color.Green)
        val number = SyntaxStyle(color = Color.Red)
        val base = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
                SyntaxRole.Keyword to keyword,
            ),
            languageOverrides = mapOf(
                SyntaxLanguageId.Kotlin to SyntaxRoleStyles(
                    SyntaxRole.String to string,
                ),
            ),
        )

        val updated = base
            .withRoleStyle(SyntaxRole.Number, number)
            .withLanguageRoleStyle(SyntaxLanguageId.Kotlin, SyntaxRole.Keyword, keyword)
            .withLanguageRoleStyles(SyntaxLanguageId.Swift, SyntaxRoleStyles(SyntaxRole.String to number))

        assertEquals(keyword, updated.roleStyles[SyntaxRole.Keyword])
        assertEquals(number, updated.roleStyles[SyntaxRole.Number])
        assertEquals(string, updated.languageOverrides[SyntaxLanguageId.Kotlin]?.get(SyntaxRole.String))
        assertEquals(keyword, updated.languageOverrides[SyntaxLanguageId.Kotlin]?.get(SyntaxRole.Keyword))
        assertEquals(number, updated.languageOverrides[SyntaxLanguageId.Swift]?.get(SyntaxRole.String))

        val removed = updated.withLanguageRoleStyles(SyntaxLanguageId.Kotlin, null)

        assertFalse(SyntaxLanguageId.Kotlin in removed.languageOverrides)
        assertEquals(updated.roleStyles, removed.roleStyles)
    }

    @Test
    fun starterThemesExposeRoleStylesWithoutLanguageOverrides() {
        val light = SyntaxTheme.DefaultLight
        val dark = SyntaxTheme.DefaultDark

        assertSame(light, SyntaxTheme.DefaultLight)
        assertSame(dark, SyntaxTheme.DefaultDark)
        assertNotEquals(light.roleStyles, dark.roleStyles)
        assertEquals(emptyMap(), light.languageOverrides)
        assertEquals(emptyMap(), dark.languageOverrides)
        assertEquals(expectedDefaultLightRoleStyles, light.roleStyles)
        assertEquals(expectedDefaultDarkRoleStyles, dark.roleStyles)
        assertEquals(starterRootRoles, light.roleStyles.keys)
        assertEquals(starterRootRoles, dark.roleStyles.keys)
        assertEquals(Color(0xFFA16207), light.resolveStyle(SyntaxRole.Annotation).color)
        assertEquals(Color(0xFF6D28D9), light.resolveStyle(SyntaxRole.Type, SyntaxLanguageId.Kotlin).color)
        assertEquals(Color(0xFFFACC15), dark.resolveStyle(SyntaxRole.Annotation, SyntaxLanguageId.Kotlin).color)
        assertEquals(
            light.resolveStyle(SyntaxRole.Markup),
            light.resolveStyle(SyntaxRole.Markup.append("heading").append("h1"), SyntaxLanguageId.Markdown),
        )
        assertEquals(
            dark.resolveStyle(SyntaxRole.Variable),
            dark.resolveStyle(SyntaxRole.Variable.Parameter, SyntaxLanguageId.Kotlin),
        )

        listOf(light.roleStyles, dark.roleStyles).forEach { styles ->
            styles.values.forEach { style ->
                assertNull(style.fontWeight)
                assertNull(style.fontStyle)
            }
        }
    }
}

private val starterRootRoles = setOf(
    SyntaxRole.Keyword,
    SyntaxRole.String,
    SyntaxRole.Number,
    SyntaxRole.Comment,
    SyntaxRole.Function,
    SyntaxRole.Type,
    SyntaxRole.Property,
    SyntaxRole.Variable,
    SyntaxRole.Operator,
    SyntaxRole.Punctuation,
    SyntaxRole.Annotation,
    SyntaxRole.Tag,
    SyntaxRole.Attribute,
    SyntaxRole.Constant,
    SyntaxRole.Escape,
    SyntaxRole.Markup,
)

private val expectedDefaultLightRoleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF9A3412)),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF15803D)),
    SyntaxRole.Number to SyntaxStyle(color = Color(0xFF0E7490)),
    SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF6B7280)),
    SyntaxRole.Function to SyntaxStyle(color = Color(0xFF1D4ED8)),
    SyntaxRole.Type to SyntaxStyle(color = Color(0xFF6D28D9)),
    SyntaxRole.Property to SyntaxStyle(color = Color(0xFFBE185D)),
    SyntaxRole.Variable to SyntaxStyle(color = Color(0xFFBE185D)),
    SyntaxRole.Operator to SyntaxStyle(color = Color(0xFF334155)),
    SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFF64748B)),
    SyntaxRole.Annotation to SyntaxStyle(color = Color(0xFFA16207)),
    SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF047857)),
    SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF0E7490)),
    SyntaxRole.Constant to SyntaxStyle(color = Color(0xFFB91C1C)),
    SyntaxRole.Escape to SyntaxStyle(color = Color(0xFF9A3412)),
    SyntaxRole.Markup to SyntaxStyle(color = Color(0xFF475569)),
)

private val expectedDefaultDarkRoleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFFF97316)),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF9ECE6A)),
    SyntaxRole.Number to SyntaxStyle(color = Color(0xFF7DCFFF)),
    SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF7A8494)),
    SyntaxRole.Function to SyntaxStyle(color = Color(0xFF82AAFF)),
    SyntaxRole.Type to SyntaxStyle(color = Color(0xFFC792EA)),
    SyntaxRole.Property to SyntaxStyle(color = Color(0xFFFF79C6)),
    SyntaxRole.Variable to SyntaxStyle(color = Color(0xFFFF79C6)),
    SyntaxRole.Operator to SyntaxStyle(color = Color(0xFF89DDFF)),
    SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFFA9B1D6)),
    SyntaxRole.Annotation to SyntaxStyle(color = Color(0xFFFACC15)),
    SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF7AA2F7)),
    SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF7DCFFF)),
    SyntaxRole.Constant to SyntaxStyle(color = Color(0xFFFF757F)),
    SyntaxRole.Escape to SyntaxStyle(color = Color(0xFFF97316)),
    SyntaxRole.Markup to SyntaxStyle(color = Color(0xFFB7C0D8)),
)
