package com.gallatinapps.syntaxmp.primitives.strings

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.isIdentifierPart

internal interface FormatSpecifierRule : StringLiteralBoundaryRule

internal object PercentFormatSpecifierRule : FormatSpecifierRule {
    override val leadingChars: Set<Char> = setOf('%')

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (context.code.getOrNull(index) != '%') return null
        var cursor = index + 1
        if (cursor >= contentEnd) return null
        if (context.code[cursor] == '%') {
            context.emit(index, cursor + 1, SyntaxRole.Escape)
            return cursor + 1
        }

        cursor = skipFormatIndex(context.code, cursor, contentEnd)
        while (cursor < contentEnd && context.code[cursor] in "#0-+ '") cursor++
        cursor = skipFormatIndex(context.code, cursor, contentEnd)

        if (cursor < contentEnd && context.code[cursor] == '*') {
            cursor++
            cursor = skipFormatIndex(context.code, cursor, contentEnd)
        } else {
            while (cursor < contentEnd && context.code[cursor].isDigit()) cursor++
        }

        if (cursor < contentEnd && context.code[cursor] == '.') {
            cursor++
            cursor = skipFormatIndex(context.code, cursor, contentEnd)
            if (cursor < contentEnd && context.code[cursor] == '*') {
                cursor++
                cursor = skipFormatIndex(context.code, cursor, contentEnd)
            } else {
                while (cursor < contentEnd && context.code[cursor].isDigit()) cursor++
            }
        }

        while (cursor < contentEnd && context.code[cursor] in "hljztL") cursor++
        return if (cursor < contentEnd && (context.code[cursor].isLetter() || context.code[cursor] == '%')) {
            context.emit(index, cursor + 1, SyntaxRole.Escape)
            cursor + 1
        } else {
            null
        }
    }

    private fun skipFormatIndex(code: String, start: Int, contentEnd: Int): Int {
        if (code.getOrNull(start) != '[') return start
        var index = start + 1
        while (index < contentEnd && code[index].isDigit()) index++
        return if (index < contentEnd && code[index] == ']') index + 1 else start
    }
}

internal object BraceFormatSpecifierRule : FormatSpecifierRule {
    override val leadingChars: Set<Char> = setOf('{')

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (context.code.getOrNull(index) != '{' || context.code.getOrNull(index + 1) == '{') return null
        var cursor = index + 1
        while (cursor < contentEnd && context.code[cursor] != '}' && context.code[cursor] != '\n') cursor++
        if (cursor >= contentEnd || context.code[cursor] != '}') return null
        val body = context.code.substring(index + 1, cursor)
        return if (
            body.isEmpty() ||
            body.startsWith(":") ||
            body.all { it.isIdentifierPart() || it.isDigit() }
        ) {
            context.emit(index, cursor + 1, SyntaxRole.Escape)
            cursor + 1
        } else {
            null
        }
    }
}
