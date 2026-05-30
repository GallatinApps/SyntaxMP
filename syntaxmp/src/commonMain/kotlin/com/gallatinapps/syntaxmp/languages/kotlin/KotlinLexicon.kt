package com.gallatinapps.syntaxmp.languages.kotlin

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val KotlinKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("as", "by", "companion", "constructor", "false", "in", "is", "null", "out", "super", "this", "true", "where"),
    SyntaxRole.Keyword.Control to setOf(
        "break", "catch", "continue", "do", "else", "finally", "for", "if", "return", "throw",
        "try", "when", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "data", "enum", "fun", "import", "interface", "object", "package", "typealias", "val", "var"),
    SyntaxRole.Keyword.Modifier to setOf("override", "private", "protected", "public", "sealed"),
)
internal val KotlinConstants = setOf("true", "false", "null")
internal val KotlinBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("arrayOf", "emptyList", "listOf", "mapOf", "print", "println", "setOf"),
)
