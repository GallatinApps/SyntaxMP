package com.gallatinapps.syntaxmp.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Foreground style contribution for a syntax role.
 *
 * Unspecified fields do not override values supplied by the host text style or earlier cascade
 * layers.
 */
public data class SyntaxStyle(
    val color: Color = Color.Unspecified,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
) {
    /** Converts this syntax style into a Compose [SpanStyle]. */
    public fun toSpanStyle(): SpanStyle =
        SpanStyle(
            color = color,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
        )
}
