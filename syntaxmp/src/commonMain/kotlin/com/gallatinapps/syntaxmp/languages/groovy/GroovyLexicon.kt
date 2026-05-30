package com.gallatinapps.syntaxmp.languages.groovy

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val GroovyKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("as", "assert", "default", "implements", "in", "instanceof", "new", "super", "synchronized", "this"),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "if", "return",
        "switch", "throw", "throws", "try", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "const", "def", "enum", "extends", "import", "interface", "package", "trait"),
    SyntaxRole.Keyword.Modifier to setOf("abstract", "final", "private", "protected", "public", "static"),
)
internal val GroovyConstants = setOf("false", "null", "true")
internal val GroovyBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("assert", "collect", "each", "find", "println", "print", "size", "with"),
)
internal val GroovyTypeKeywords = setOf(
    "boolean", "byte", "char", "double", "float", "int", "long", "short", "void", "BigDecimal",
    "BigInteger", "String",
)
