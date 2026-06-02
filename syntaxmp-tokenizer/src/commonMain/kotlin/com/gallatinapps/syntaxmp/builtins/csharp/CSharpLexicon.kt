package com.gallatinapps.syntaxmp.builtins.csharp

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val CSharpKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "as", "base", "default", "delegate", "event", "explicit", "extern", "false", "fixed",
        "get", "implicit", "in", "is", "lock", "new", "null", "operator", "out", "params", "ref",
        "set", "sizeof", "stackalloc", "this", "true", "typeof", "unchecked", "unsafe", "void",
        "volatile",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "foreach", "if",
        "return", "switch", "throw", "try", "while", "yield",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "const", "enum", "interface", "namespace", "record", "struct", "using"),
    SyntaxRole.Keyword.Modifier to setOf(
        "abstract", "async", "await", "internal", "override", "private", "protected", "public",
        "readonly", "sealed", "static", "virtual",
    ),
)
internal val CSharpConstants = setOf("true", "false", "null")
internal val CSharpBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Type to setOf("Guid", "String"),
    SyntaxRole.Variable.Namespace to setOf("Console", "Enumerable", "Math"),
)
internal val CSharpTypeKeywords = setOf(
    "bool", "byte", "char", "decimal", "double", "dynamic", "float", "int", "long", "object",
    "sbyte", "short", "string", "uint", "ulong", "ushort", "var", "void",
)
