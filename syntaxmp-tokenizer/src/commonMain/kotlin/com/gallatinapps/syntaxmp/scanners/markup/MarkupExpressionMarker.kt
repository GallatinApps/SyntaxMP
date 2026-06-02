package com.gallatinapps.syntaxmp.scanners.markup

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal sealed interface MarkupExpressionMarker {
    fun markerEnd(code: String, contentStart: Int, close: Int): Int?

    object None : MarkupExpressionMarker {
        override fun markerEnd(code: String, contentStart: Int, close: Int): Int? = null
    }

    data class FirstCharKeyword(
        val chars: Set<Char>,
        val role: SyntaxRole,
    ) : MarkupExpressionMarker {
        override fun markerEnd(code: String, contentStart: Int, close: Int): Int? {
            if (contentStart >= close || code[contentStart] !in chars) return null
            var end = contentStart + 1
            while (end < close && code[end].isMarkupExpressionNamePart()) end++
            return end
        }
    }
}

private fun Char.isMarkupExpressionNamePart(): Boolean =
    isLetterOrDigit() || this == '_' || this == '-' || this == ':' || this == '.'
