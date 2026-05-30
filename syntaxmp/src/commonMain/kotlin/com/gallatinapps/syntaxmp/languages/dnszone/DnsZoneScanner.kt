package com.gallatinapps.syntaxmp.languages.dnszone

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class DnsZoneScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val classes: Set<String> = emptySet(),
    private val recordTypes: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        var state = DnsZoneLineState()
        while (index < code.length) {
            val lineEnd = code.indexOf('\n', index).let { if (it == -1) code.length else it }
            state = scanLine(start = index, end = lineEnd, state = state)
            index = (lineEnd + 1).coerceAtMost(code.length)
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int, state: DnsZoneLineState): DnsZoneLineState {
        var index = firstNonWhitespace(start, end)
        var field = state.field
        var parenthesisDepth = state.parenthesisDepth
        while (index < end) {
            index = when {
                code[index].isWhitespace() -> index + 1
                code[index] == ';' -> {
                    add(index, end, SyntaxRole.Comment)
                    end
                }
                code[index] == '"' -> scanString(index, end)
                code[index] == '(' -> {
                    parenthesisDepth++
                    index + 1
                }
                code[index] == ')' -> {
                    parenthesisDepth = (parenthesisDepth - 1).coerceAtLeast(0)
                    index + 1
                }
                else -> scanWord(index, end, field).also { field++ }
            }
        }
        return if (parenthesisDepth == 0) {
            DnsZoneLineState()
        } else {
            DnsZoneLineState(field = field, parenthesisDepth = parenthesisDepth)
        }
    }

    private fun scanString(start: Int, end: Int): Int {
        var index = start + 1
        while (index < end) {
            if (code[index] == '\\') index += 2
            else if (code[index] == '"') {
                add(start, index + 1, SyntaxRole.String)
                return index + 1
            } else {
                index++
            }
        }
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun scanWord(start: Int, end: Int, field: Int): Int {
        var index = start
        while (
            index < end &&
            !code[index].isWhitespace() &&
            code[index] != ';' &&
            code[index] != '(' &&
            code[index] != ')'
        ) {
            index++
        }
        val word = code.substring(start, index)
        val upper = word.uppercase()
        val role = when {
            word.startsWith('$') -> SyntaxRole.Keyword
            upper in classes -> SyntaxRole.Keyword
            upper in recordTypes -> SyntaxRole.Type
            word.all { it.isDigit() } -> SyntaxRole.Number
            field == 0 -> SyntaxRole.Property
            else -> SyntaxRole.Variable
        }
        add(start, index, role)
        return index
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }
}

private data class DnsZoneLineState(
    val field: Int = 0,
    val parenthesisDepth: Int = 0,
)
