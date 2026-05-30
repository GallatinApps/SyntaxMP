package com.gallatinapps.syntaxmp.engine.primitives

internal fun findBraceBalancedEnd(
    code: String,
    openIndex: Int,
    openBrace: Char = '{',
    closeBrace: Char = '}',
): Int {
    var depth = 1
    var index = openIndex + 1
    while (index < code.length) {
        when {
            code[index] == '\\' -> index = (index + 2).coerceAtMost(code.length)
            code[index] == '"' || code[index] == '\'' || code[index] == '`' ->
                index = skipQuoted(code, index, code[index])
            code.startsWithAt(index, "//") ->
                index = code.indexOf('\n', index).let { if (it == -1) code.length else it }
            code.startsWithAt(index, "/*") ->
                index = code.indexOf("*/", index + 2).let { if (it == -1) code.length else it + 2 }
            code[index] == openBrace -> {
                depth++
                index++
            }
            code[index] == closeBrace -> {
                depth--
                if (depth == 0) return index
                index++
            }
            else -> index++
        }
    }
    return code.length
}

private fun skipQuoted(code: String, start: Int, quote: Char): Int {
    var index = start + 1
    while (index < code.length) {
        if (code[index] == '\\') {
            index += 2
        } else if (code[index] == quote) {
            return index + 1
        } else {
            index++
        }
    }
    return code.length
}

private fun String.startsWithAt(index: Int, value: String): Boolean =
    index + value.length <= length && regionMatches(index, value, 0, value.length)
