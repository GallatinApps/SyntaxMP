package com.gallatinapps.syntaxmp.languages.dart

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val DartKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "as", "assert", "base", "covariant", "default", "deferred", "extension", "external",
        "factory", "false", "get", "hide", "implements", "in", "is", "late", "library", "mixin",
        "new", "null", "on", "operator", "part", "required", "set", "show", "super", "sync",
        "this", "true", "void", "with",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "if", "return",
        "switch", "throw", "try", "when", "while", "yield",
    ),
    SyntaxRole.Keyword.Declaration to setOf(
        "class", "const", "enum", "export", "extends", "function", "import", "interface", "typedef",
        "var",
    ),
    SyntaxRole.Keyword.Modifier to setOf("abstract", "async", "await", "final", "sealed", "static"),
)
internal val DartConstants = setOf("true", "false", "null")
internal val DartBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("print"),
    SyntaxRole.Type to setOf("DateTime", "Future", "List", "Map", "Set"),
)
internal val DartTypeKeywords = setOf("bool", "double", "dynamic", "int", "num", "String", "void")
