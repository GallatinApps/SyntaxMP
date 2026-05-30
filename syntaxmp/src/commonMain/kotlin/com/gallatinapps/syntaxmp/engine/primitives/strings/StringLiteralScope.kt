package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal interface StringLiteralScope {
    val code: String

    fun emit(start: Int, end: Int, role: SyntaxRole)
    fun scanNestedCode(start: Int, endExclusive: Int)
    fun previousNonWhitespace(index: Int): Char?
    fun nextNonWhitespace(index: Int): Char?
    fun startsWith(index: Int, value: String): Boolean
    fun scanNestedIdentifier(start: Int, limit: Int): Int
    fun scanStringLiteral(request: StringLiteralRequest): Int
}

internal fun canStartOperatorLikeLiteral(context: StringLiteralScope, start: Int): Boolean {
    val previous = context.previousNonWhitespace(start)
    return previous == null || previous in "([{=,:;!&|?+-*~<>"
}
