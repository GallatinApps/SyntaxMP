package com.gallatinapps.syntaxmp.demo.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.gallatinapps.syntaxmp.compose.theme.SyntaxRoleStyles
import com.gallatinapps.syntaxmp.compose.theme.SyntaxStyle
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal enum class DemoThemeMode {
    Light,
    Dark,
    Diagnostic,
}

internal data class DemoColorScheme(
    val appBackground: Color,
    val paneBackground: Color,
    val codeBackground: Color,
    val selectedBackground: Color,
    val hoverBackground: Color,
    val border: Color,
    val accent: Color,
    val accentMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,
)

internal fun DemoThemeMode.syntaxTheme(colors: DemoColorScheme): SyntaxTheme {
    val baseTheme = when (this) {
        DemoThemeMode.Light -> SyntaxTheme.DefaultLight
        DemoThemeMode.Dark -> SyntaxTheme.DefaultDark
        DemoThemeMode.Diagnostic -> DiagnosticSyntaxTheme
    }
    if (this == DemoThemeMode.Diagnostic) {
        return baseTheme
    }
    val roleStyles = demoRootRoleStyles(baseTheme.roleStyles)
    return baseTheme.copy(
        roleStyles = roleStyles,
        languageOverrides = baseTheme.languageOverrides + mapOf(
            LanguageId.Kotlin to kotlinSyntaxRoleStyles(roleStyles, colors),
            LanguageId.Csv to SyntaxRoleStyles(
                SyntaxRole.Punctuation to SyntaxStyle(
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                ),
            ),
            LanguageId.Diff to diffSyntaxRoleStyles(),
        ),
    )
}

private fun DemoThemeMode.demoRootRoleStyles(base: SyntaxRoleStyles): SyntaxRoleStyles =
    when (this) {
        DemoThemeMode.Light -> base + SyntaxRoleStyles(
            SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF1E40AF)),
            SyntaxRole.Function to SyntaxStyle(color = Color(0xFF00796B)),
            SyntaxRole.Type to SyntaxStyle(color = Color(0xFF7E22CE)),
            SyntaxRole.Annotation to SyntaxStyle(color = Color(0xFFB45309)),
            SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF166534)),
            SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF1D4ED8)),
            SyntaxRole.Escape to SyntaxStyle(color = Color(0xFF1E40AF)),
        )
        DemoThemeMode.Dark,
        DemoThemeMode.Diagnostic -> base
    }

private fun DemoThemeMode.kotlinSyntaxRoleStyles(
    base: SyntaxRoleStyles,
    colors: DemoColorScheme,
): SyntaxRoleStyles {
    val keyword = base[SyntaxRole.Keyword] ?: SyntaxStyle()
    return when (this) {
        DemoThemeMode.Light -> SyntaxRoleStyles(
            SyntaxRole.Function to SyntaxStyle(color = Color(0xFF0F766E)),
            SyntaxRole.Type to SyntaxStyle(color = Color(0xFF24292F)),
            SyntaxRole.Constant to keyword,
            SyntaxRole.Escape to keyword,
            SyntaxRole.Variable.Parameter to SyntaxStyle(color = Color(0xFF24292F)),
        )
        DemoThemeMode.Dark -> SyntaxRoleStyles(
            SyntaxRole.Function to SyntaxStyle(color = Color(0xFF649EEE)),
            SyntaxRole.Type to SyntaxStyle(color = colors.textPrimary),
            SyntaxRole.Constant to keyword,
            SyntaxRole.Escape to keyword,
            SyntaxRole.Variable.Parameter to SyntaxStyle(color = colors.textPrimary),
        )
        DemoThemeMode.Diagnostic -> SyntaxRoleStyles()
    }
}

private fun DemoThemeMode.diffSyntaxRoleStyles(): SyntaxRoleStyles =
    when (this) {
        DemoThemeMode.Light -> SyntaxRoleStyles(
            DiffHeaderRole to SyntaxStyle(
                color = Color(0xFF0969DA),
                fontWeight = FontWeight.SemiBold,
            ),
            DiffHunkRole to SyntaxStyle(
                color = Color(0xFF6D28D9),
                fontWeight = FontWeight.SemiBold,
            ),
            DiffAdditionRole to SyntaxStyle(color = Color(0xFF15803D)),
            DiffDeletionRole to SyntaxStyle(color = Color(0xFFB91C1C)),
        )
        DemoThemeMode.Dark -> SyntaxRoleStyles(
            DiffHeaderRole to SyntaxStyle(
                color = Color(0xFF7AA2F7),
                fontWeight = FontWeight.SemiBold,
            ),
            DiffHunkRole to SyntaxStyle(
                color = Color(0xFFC792EA),
                fontWeight = FontWeight.SemiBold,
            ),
            DiffAdditionRole to SyntaxStyle(color = Color(0xFF9ECE6A)),
            DiffDeletionRole to SyntaxStyle(color = Color(0xFFFF757F)),
        )
        DemoThemeMode.Diagnostic -> SyntaxRoleStyles()
    }

internal val DemoThemeMode.toggled: DemoThemeMode
    get() = when (this) {
        DemoThemeMode.Light -> DemoThemeMode.Dark
        DemoThemeMode.Dark -> DemoThemeMode.Light
        DemoThemeMode.Diagnostic -> DemoThemeMode.Dark
    }

internal val DemoThemeMode.diagnosticToggled: DemoThemeMode
    get() = when (this) {
        DemoThemeMode.Light, DemoThemeMode.Dark -> DemoThemeMode.Diagnostic
        DemoThemeMode.Diagnostic -> DemoThemeMode.Dark
    }

internal val DemoThemeMode.colors: DemoColorScheme
    get() = when (this) {
        DemoThemeMode.Light -> DemoColorScheme(
            appBackground = Color(0xFFF4F4F5),
            paneBackground = Color(0xFFFFFFFF),
            codeBackground = Color(0xFFFFFFFF),
            selectedBackground = Color(0xFFEAF4FF),
            hoverBackground = Color(0xFFF5F5F5),
            border = Color(0xFFD4D4D4),
            accent = Color(0xFF0969DA),
            accentMuted = Color(0xFFC8E1FF),
            textPrimary = Color(0xFF18181B),
            textSecondary = Color(0xFF5F5F64),
            textOnAccent = Color(0xFFFFFFFF),
        )
        DemoThemeMode.Dark -> DemoColorScheme(
            appBackground = Color(0xFF141414),
            paneBackground = Color(0xFF1F1F1F),
            codeBackground = Color(0xFF101010),
            selectedBackground = Color(0xFF332D23),
            hoverBackground = Color(0xFF292929),
            border = Color(0xFF404040),
            accent = Color(0xFFF59E0B),
            accentMuted = Color(0xFF3B2F18),
            textPrimary = Color(0xFFE1E8F0),
            textSecondary = Color(0xFFC6C1B8),
            textOnAccent = Color(0xFF1C1200),
        )
        DemoThemeMode.Diagnostic -> DemoColorScheme(
            appBackground = Color(0xFF0A0A0A),
            paneBackground = Color(0xFF111111),
            codeBackground = Color(0xFF000000),
            selectedBackground = Color(0xFF2F2500),
            hoverBackground = Color(0xFF202020),
            border = Color(0xFF545454),
            accent = Color(0xFFFFD60A),
            accentMuted = Color(0xFF4A3F00),
            textPrimary = Color(0xFFF8F8F2),
            textSecondary = Color(0xFFCFCFCF),
            textOnAccent = Color(0xFF000000),
        )
    }

private val DiagnosticSyntaxTheme = SyntaxTheme(
    roleStyles = SyntaxRoleStyles(
        SyntaxRole.Keyword to SyntaxStyle(
            color = Color(0xFFFF2D55),
            fontWeight = FontWeight.Bold,
        ),
        SyntaxRole.String to SyntaxStyle(color = Color(0xFF32D74B)),
        SyntaxRole.Number to SyntaxStyle(color = Color(0xFFFF9F0A)),
        SyntaxRole.Comment to SyntaxStyle(
            color = Color(0xFF8E8E93),
            fontStyle = FontStyle.Italic,
        ),
        SyntaxRole.Function to SyntaxStyle(color = Color(0xFFBF5AF2)),
        SyntaxRole.Type to SyntaxStyle(color = Color(0xFF64D2FF)),
        SyntaxRole.Property to SyntaxStyle(color = Color(0xFFFFD60A)),
        SyntaxRole.Variable to SyntaxStyle(color = Color(0xFFFF7AB6)),
        SyntaxRole.Operator to SyntaxStyle(
            color = Color(0xFFFF453A),
            fontWeight = FontWeight.SemiBold,
        ),
        SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFF0A84FF)),
        SyntaxRole.Annotation to SyntaxStyle(color = Color(0xFF30D158)),
        SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF5E5CE6)),
        SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFFFFCC80)),
        SyntaxRole.Constant to SyntaxStyle(color = Color(0xFF00C7BE)),
        SyntaxRole.Escape to SyntaxStyle(
            color = Color(0xFFFF375F),
            fontWeight = FontWeight.SemiBold,
        ),
        SyntaxRole.Markup to SyntaxStyle(
            color = Color(0xFFBDB2FF),
            fontWeight = FontWeight.SemiBold,
        ),
    ),
)

private val DiffHeaderRole = SyntaxRole.of("markup.diff.header")
private val DiffHunkRole = SyntaxRole.of("markup.diff.hunk")
private val DiffAdditionRole = SyntaxRole.of("markup.diff.addition")
private val DiffDeletionRole = SyntaxRole.of("markup.diff.deletion")
