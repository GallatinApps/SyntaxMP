package com.gallatinapps.syntaxmp.scanners.markup

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal data class MarkupExpressionRule(
    val opener: String,
    val closer: String,
    val allowedAtTopLevel: Boolean,
    val allowedInsideMarkup: Boolean,
    val allowedInsideTag: Boolean = allowedInsideMarkup,
    val marker: MarkupExpressionMarker = MarkupExpressionMarker.None,
    val delimiterRole: SyntaxRole = SyntaxRole.Markup.Expression,
)
