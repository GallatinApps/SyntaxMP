package com.gallatinapps.syntaxmp.engine.scanners.markup

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal data class MarkupExpressionRule(
    val opener: String,
    val closer: String,
    val allowedAtTopLevel: Boolean,
    val allowedInsideMarkup: Boolean,
    val allowedInsideTag: Boolean = allowedInsideMarkup,
    val marker: MarkupExpressionMarker = MarkupExpressionMarker.None,
    val delimiterRole: SyntaxRole = SyntaxRole.Markup.Expression,
)
