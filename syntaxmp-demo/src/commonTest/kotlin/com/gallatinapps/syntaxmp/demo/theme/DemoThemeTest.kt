package com.gallatinapps.syntaxmp.demo.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import kotlin.test.Test
import kotlin.test.assertEquals

class DemoThemeTest {
    @Test
    fun diagnosticSyntaxThemeUsesUniqueRootRoleColors() {
        val theme = DemoThemeMode.Diagnostic.syntaxTheme(DemoThemeMode.Diagnostic.colors)
        val roleColors = diagnosticRootRoles.associateWith { role ->
            theme.resolveStyle(role).color
        }

        assertEquals(
            expected = roleColors.size,
            actual = roleColors.values.toSet().size,
            message = "Diagnostic theme should use one visible color per syntax root role.",
        )
    }

    @Test
    fun demoKotlinThemeOverridesDefaultStarterPalettes() {
        val light = DemoThemeMode.Light.syntaxTheme(DemoThemeMode.Light.colors)
        val dark = DemoThemeMode.Dark.syntaxTheme(DemoThemeMode.Dark.colors)
        val kotlin = requireNotNull(dark.languageOverrides[LanguageId.Kotlin])

        assertEquals(Color(0xFF649EEE), kotlin[SyntaxRole.Function]?.color)
        assertEquals(Color(0xFF0F766E), light.resolveStyle(SyntaxRole.Function, LanguageId.Kotlin).color)
        assertEquals(Color(0xFFE1E8F0), DemoThemeMode.Dark.colors.textPrimary)
        assertEquals(DemoThemeMode.Dark.colors.textPrimary, kotlin[SyntaxRole.Type]?.color)
        assertEquals(DemoThemeMode.Dark.colors.textPrimary, kotlin[SyntaxRole.Variable.Parameter]?.color)
        assertEquals(
            DemoThemeMode.Dark.colors.textPrimary,
            dark.resolveStyle(SyntaxRole.Type, LanguageId.Kotlin).color,
        )
        assertEquals(
            DemoThemeMode.Dark.colors.textPrimary,
            dark.resolveStyle(SyntaxRole.Variable.Parameter, LanguageId.Kotlin).color,
        )
        assertEquals(
            dark.resolveStyle(SyntaxRole.Function, LanguageId.Kotlin),
            dark.resolveStyle(SyntaxRole.Function.Declaration, LanguageId.Kotlin),
        )
        assertEquals(
            dark.resolveStyle(SyntaxRole.Function, LanguageId.Kotlin),
            dark.resolveStyle(SyntaxRole.Function.Member, LanguageId.Kotlin),
        )
        assertEquals(
            dark.resolveStyle(SyntaxRole.Keyword),
            dark.resolveStyle(SyntaxRole.Constant.Builtin.append("true"), LanguageId.Kotlin),
        )
        assertEquals(dark.resolveStyle(SyntaxRole.Variable), dark.resolveStyle(SyntaxRole.Variable, LanguageId.Kotlin))
    }

    @Test
    fun demoLightThemeExperimentsWithHigherContrastRootColors() {
        val light = DemoThemeMode.Light.syntaxTheme(DemoThemeMode.Light.colors)
        val dark = DemoThemeMode.Dark.syntaxTheme(DemoThemeMode.Dark.colors)

        assertEquals(Color(0xFF1E40AF), light.resolveStyle(SyntaxRole.Keyword).color)
        assertEquals(Color(0xFF00796B), light.resolveStyle(SyntaxRole.Function).color)
        assertEquals(Color(0xFF7E22CE), light.resolveStyle(SyntaxRole.Type).color)
        assertEquals(Color(0xFFB45309), light.resolveStyle(SyntaxRole.Annotation).color)
        assertEquals(Color(0xFF166534), light.resolveStyle(SyntaxRole.Tag).color)
        assertEquals(Color(0xFF1D4ED8), light.resolveStyle(SyntaxRole.Attribute).color)
        assertEquals(Color(0xFFF97316), dark.resolveStyle(SyntaxRole.Keyword).color)
        assertEquals(Color(0xFF9A3412), SyntaxTheme.DefaultLight.resolveStyle(SyntaxRole.Keyword).color)
        assertEquals(emptyMap(), SyntaxTheme.DefaultLight.languageOverrides)
    }

    @Test
    fun demoDiffThemeStylesAdditionsAndDeletionsWithoutChangingDefaults() {
        val light = DemoThemeMode.Light.syntaxTheme(DemoThemeMode.Light.colors)
        val dark = DemoThemeMode.Dark.syntaxTheme(DemoThemeMode.Dark.colors)
        val lightDiff = requireNotNull(light.languageOverrides[LanguageId.Diff])
        val darkDiff = requireNotNull(dark.languageOverrides[LanguageId.Diff])

        assertEquals(Color(0xFF0969DA), lightDiff[DiffHeaderRole]?.color)
        assertEquals(FontWeight.SemiBold, lightDiff[DiffHeaderRole]?.fontWeight)
        assertEquals(Color(0xFF6D28D9), lightDiff[DiffHunkRole]?.color)
        assertEquals(Color(0xFF15803D), lightDiff[DiffAdditionRole]?.color)
        assertEquals(Color(0xFFB91C1C), lightDiff[DiffDeletionRole]?.color)
        assertEquals(Color(0xFF7AA2F7), darkDiff[DiffHeaderRole]?.color)
        assertEquals(FontWeight.SemiBold, darkDiff[DiffHeaderRole]?.fontWeight)
        assertEquals(Color(0xFFC792EA), darkDiff[DiffHunkRole]?.color)
        assertEquals(Color(0xFF9ECE6A), darkDiff[DiffAdditionRole]?.color)
        assertEquals(Color(0xFFFF757F), darkDiff[DiffDeletionRole]?.color)
        assertEquals(
            SyntaxTheme.DefaultLight.resolveStyle(SyntaxRole.Markup),
            SyntaxTheme.DefaultLight.resolveStyle(DiffHeaderRole, LanguageId.Diff),
        )
        assertEquals(
            SyntaxTheme.DefaultDark.resolveStyle(SyntaxRole.Markup),
            SyntaxTheme.DefaultDark.resolveStyle(DiffDeletionRole, LanguageId.Diff),
        )
    }

    @Test
    fun themeToggleStaysOnLightAndDarkModes() {
        assertEquals(DemoThemeMode.Dark, DemoThemeMode.Light.toggled)
        assertEquals(DemoThemeMode.Light, DemoThemeMode.Dark.toggled)
        assertEquals(DemoThemeMode.Dark, DemoThemeMode.Diagnostic.toggled)
    }

    @Test
    fun diagnosticToggleKeepsDiagnosticModeExplicit() {
        assertEquals(DemoThemeMode.Diagnostic, DemoThemeMode.Light.diagnosticToggled)
        assertEquals(DemoThemeMode.Diagnostic, DemoThemeMode.Dark.diagnosticToggled)
        assertEquals(DemoThemeMode.Dark, DemoThemeMode.Diagnostic.diagnosticToggled)
    }
}

private val diagnosticRootRoles = listOf(
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

private val DiffHeaderRole = SyntaxRole.of("markup.diff.header")
private val DiffHunkRole = SyntaxRole.of("markup.diff.hunk")
private val DiffAdditionRole = SyntaxRole.of("markup.diff.addition")
private val DiffDeletionRole = SyntaxRole.of("markup.diff.deletion")
