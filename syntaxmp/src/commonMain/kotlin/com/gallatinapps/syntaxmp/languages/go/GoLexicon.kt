package com.gallatinapps.syntaxmp.languages.go

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val GoKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("chan", "default", "defer", "go", "map", "range", "select"),
    SyntaxRole.Keyword.Control to setOf("break", "case", "continue", "else", "fallthrough", "for", "goto", "if", "return", "switch"),
    SyntaxRole.Keyword.Declaration to setOf("const", "func", "import", "interface", "package", "struct", "type", "var"),
)
internal val GoConstants = setOf("true", "false", "nil", "iota")
internal val GoBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf(
        "append", "cap", "close", "copy", "delete", "len", "make", "new", "panic", "print", "println",
    ),
)
internal val GoTypeKeywords = setOf(
    "bool", "byte", "complex64", "complex128", "error", "float32", "float64", "int", "int8",
    "int16", "int32", "int64", "rune", "string", "uint", "uint8", "uint16", "uint32", "uint64", "uintptr",
)
