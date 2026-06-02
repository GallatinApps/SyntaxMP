package com.gallatinapps.syntaxmp.builtins.c

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val CKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("auto", "default", "extern", "inline", "register", "restrict", "sizeof", "union", "volatile"),
    SyntaxRole.Keyword.Control to setOf("break", "case", "continue", "do", "else", "for", "goto", "if", "return", "switch", "while"),
    SyntaxRole.Keyword.Declaration to setOf("const", "enum", "struct", "typedef"),
    SyntaxRole.Keyword.Modifier to setOf("static"),
)
internal val CConstants = setOf("NULL", "true", "false")
internal val CBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("calloc", "free", "malloc", "memcpy", "printf", "puts", "snprintf", "strlen"),
)
internal val CTypeKeywords = setOf(
    "bool", "char", "double", "float", "int", "long", "short", "signed", "size_t", "unsigned", "void",
)
