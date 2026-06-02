package com.gallatinapps.syntaxmp.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.role.rolePathValuesFromRoot

/**
 * Syntax role styles for foreground text styling only.
 *
 * SyntaxMP does not paint code backgrounds or prescribe base text styling. Hosts choose the
 * surrounding [androidx.compose.ui.text.TextStyle].
 */
public data class SyntaxTheme(
    val roleStyles: SyntaxRoleStyles = SyntaxRoleStyles(),
    val languageOverrides: Map<LanguageId, SyntaxRoleStyles> = emptyMap(),
) {
    private val globalStylesByValue: Map<String, SyntaxStyle> =
        roleStyles.mapKeys { it.key.value }

    private val languageStylesByValue: Map<LanguageId, Map<String, SyntaxStyle>> =
        languageOverrides.mapValues { (_, styles) ->
            styles.mapKeys { it.key.value }
    }

    /** Resolves a Compose span style for [span]. */
    public fun resolveSpanStyle(span: SyntaxTokenSpan): SpanStyle =
        resolveStyle(role = span.role, languageId = span.languageId).toSpanStyle()

    /** Resolves global style cascade for [role]. */
    public fun resolveStyle(role: SyntaxRole): SyntaxStyle =
        resolveStyleInternal(role = role, languageId = null)

    /** Resolves global plus [languageId] style cascade for [role]. */
    public fun resolveStyle(
        role: SyntaxRole,
        languageId: LanguageId,
    ): SyntaxStyle =
        resolveStyleInternal(role = role, languageId = languageId)

    private fun resolveStyleInternal(
        role: SyntaxRole,
        languageId: LanguageId?,
    ): SyntaxStyle {
        val roleValues = rolePathValuesFromRoot(role.value)
        var resolved = SyntaxStyle()
        roleValues.forEach { roleValue ->
            globalStylesByValue[roleValue]?.let { resolved = resolved.merge(it) }
        }
        val languageStyles = languageId?.let { languageStylesByValue[it] }
        if (languageStyles != null) {
            roleValues.forEach { roleValue ->
                languageStyles[roleValue]?.let { resolved = resolved.merge(it) }
            }
        }
        return resolved
    }

    public companion object {
        /**
         * SyntaxMP's built-in starter theme. Tuned for light surfaces. Replace with your own
         * [SyntaxTheme] matched to your application's color tokens before shipping.
         */
        public val DefaultLight: SyntaxTheme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
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
            ),
        )

        /**
         * SyntaxMP's built-in dark starter theme. Tuned for dark surfaces. Replace with your own
         * [SyntaxTheme] matched to your application's color tokens before shipping.
         */
        public val DefaultDark: SyntaxTheme = SyntaxTheme(
            roleStyles = SyntaxRoleStyles(
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
            ),
        )
    }
}

/** Returns a copy with one global role style replaced. */
public fun SyntaxTheme.withRoleStyle(
    role: SyntaxRole,
    style: SyntaxStyle,
): SyntaxTheme =
    copy(roleStyles = roleStyles.withRoleStyle(role, style))

/** Returns a copy with [languageId]'s entire role-style override replaced or removed. */
public fun SyntaxTheme.withLanguageRoleStyles(
    languageId: LanguageId,
    styles: SyntaxRoleStyles?,
): SyntaxTheme =
    copy(
        languageOverrides = if (styles == null) {
            languageOverrides - languageId
        } else {
            languageOverrides + (languageId to styles)
        },
    )

/** Returns a copy with one [languageId]-specific [role] style replaced. */
public fun SyntaxTheme.withLanguageRoleStyle(
    languageId: LanguageId,
    role: SyntaxRole,
    style: SyntaxStyle,
): SyntaxTheme =
    withLanguageRoleStyles(
        languageId = languageId,
        styles = (languageOverrides[languageId] ?: SyntaxRoleStyles()).withRoleStyle(role, style),
    )

private fun SyntaxStyle.merge(next: SyntaxStyle): SyntaxStyle =
    SyntaxStyle(
        color = if (next.color == Color.Unspecified) color else next.color,
        fontWeight = next.fontWeight ?: fontWeight,
        fontStyle = next.fontStyle ?: fontStyle,
    )
