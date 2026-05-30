package com.gallatinapps.syntaxmp.languages.csv

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class CsvScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        var atFieldStart = true
        while (index < code.length) {
            index = when (code[index]) {
                ',' -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    atFieldStart = true
                    index + 1
                }
                '\r', '\n' -> {
                    atFieldStart = true
                    index + 1
                }
                '"' -> {
                    if (atFieldStart) {
                        val end = scanQuotedField(index)
                        atFieldStart = false
                        end
                    } else {
                        index + 1
                    }
                }
                ' ', '\t' -> index + 1
                else -> {
                    atFieldStart = false
                    index + 1
                }
            }
        }
        return tokens
    }

    private fun scanQuotedField(start: Int): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == '"') {
                if (index + 1 < code.length && code[index + 1] == '"') {
                    index += 2
                } else {
                    return index + 1
                }
            } else {
                index++
            }
        }
        return code.length
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}
