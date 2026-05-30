package com.gallatinapps.syntaxmp.languages.javascript

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val JavaScriptKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "debugger", "default", "delete", "false", "in", "instanceof", "new", "null", "of", "super",
        "this", "true", "typeof", "undefined", "void",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "if", "return",
        "switch", "throw", "try", "while", "yield",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "const", "export", "extends", "from", "function", "import", "let", "var"),
    SyntaxRole.Keyword.Modifier to setOf("async", "await", "static"),
)
internal val JavaScriptConstants = setOf("true", "false", "null", "undefined", "NaN", "Infinity")
internal val JavaScriptBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Type to setOf("Array", "Boolean", "Number", "Object", "Promise", "String"),
    SyntaxRole.Variable.Namespace to setOf("JSON", "Math", "console"),
)
