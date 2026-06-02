package com.gallatinapps.syntaxmp.builtins.swift

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val SwiftKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "as", "associatedtype", "default", "defer", "deinit", "extension", "false", "in", "init",
        "inout", "is", "nil", "operator", "repeat", "self", "subscript", "super", "true", "where",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "fallthrough", "for", "guard", "if",
        "return", "switch", "throw", "throws", "try", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "enum", "func", "import", "let", "protocol", "struct", "typealias", "var"),
    SyntaxRole.Keyword.Modifier to setOf("internal", "open", "private", "public", "static"),
)
internal val SwiftConstants = setOf("true", "false", "nil")
internal val SwiftBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("abs", "max", "min", "print"),
)
