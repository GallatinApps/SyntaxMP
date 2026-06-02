package com.gallatinapps.syntaxmp.primitives.numbers

internal object ScriptLikeNumberScanner : NumericLiteralScanner {
    override fun scan(code: String, start: Int): Int? {
        if (code.getOrNull(start)?.isDigit() != true) return null
        var end = start
        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '.')) end++
        return end
    }
}
