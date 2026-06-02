package com.gallatinapps.syntaxmp.scanners.markup

internal fun findMarkupEntityEnd(code: String, start: Int, limit: Int = code.length): Int? {
    if (start >= limit || code.getOrNull(start) != '&') return null
    var index = start + 1
    if (index >= limit) return null
    if (code[index] == '#') {
        index++
        if (index < limit && code[index].lowercaseChar() == 'x') {
            index++
            val digitsStart = index
            while (index < limit && code[index].isHexEntityDigit()) index++
            return if (index > digitsStart && code.getOrNull(index) == ';') index + 1 else null
        }
        val digitsStart = index
        while (index < limit && code[index].isDigit()) index++
        return if (index > digitsStart && code.getOrNull(index) == ';') index + 1 else null
    }
    if (!code[index].isLetter()) return null
    index++
    while (index < limit && code[index].isLetterOrDigit()) index++
    return if (code.getOrNull(index) == ';') index + 1 else null
}

private fun Char.isHexEntityDigit(): Boolean =
    isDigit() || this in 'a'..'f' || this in 'A'..'F'
