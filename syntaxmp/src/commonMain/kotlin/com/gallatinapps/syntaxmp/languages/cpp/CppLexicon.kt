package com.gallatinapps.syntaxmp.languages.cpp

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val CppKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "alignas", "alignof", "and", "asm", "auto", "bitand", "bitor", "compl", "consteval",
        "constexpr", "constinit", "decltype", "default", "delete", "explicit", "extern", "false",
        "friend", "inline", "new", "noexcept", "not", "nullptr", "operator", "or", "requires",
        "sizeof", "template", "this", "true", "union", "volatile", "xor",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "for", "if", "return", "switch",
        "throw", "try", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf(
        "class", "concept", "const", "enum", "export", "namespace", "struct", "typedef", "typename",
        "using",
    ),
    SyntaxRole.Keyword.Modifier to setOf("final", "mutable", "override", "private", "protected", "public", "static", "virtual"),
)
internal val CppConstants = setOf("true", "false", "nullptr", "NULL")
internal val CppBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("make_unique", "move", "printf", "size"),
    SyntaxRole.Variable.Namespace to setOf("std"),
)
internal val CppTypeKeywords = setOf(
    "bool", "char", "double", "float", "int", "long", "short", "signed", "size_t", "string", "unsigned", "void",
)
