package com.gallatinapps.syntaxmp.builtins.java

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val JavaKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "assert", "default", "exports", "false", "implements", "instanceof", "native", "new", "null",
        "opens", "requires", "strictfp", "super", "synchronized", "this", "to", "transient", "true",
        "uses", "volatile", "with",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "if", "return",
        "switch", "throw", "throws", "try", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "const", "enum", "extends", "import", "interface", "module", "package"),
    SyntaxRole.Keyword.Modifier to setOf("abstract", "final", "open", "private", "protected", "public", "static"),
)
internal val JavaConstants = setOf("true", "false", "null")
internal val JavaBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Type to setOf("Double", "Integer", "Long", "String"),
    SyntaxRole.Variable.Namespace to setOf("Math", "System"),
)
internal val JavaTypeKeywords = setOf(
    "boolean", "byte", "char", "double", "float", "int", "long", "short", "void", "var",
)
