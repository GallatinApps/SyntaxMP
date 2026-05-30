package com.gallatinapps.syntaxmp.languages.scala

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val ScalaKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("derives", "given", "implicit", "infix", "inline", "lazy", "new", "opaque", "then", "with"),
    SyntaxRole.Keyword.Control to setOf("case", "catch", "do", "else", "finally", "for", "if", "match", "return", "throw", "try", "while", "yield"),
    SyntaxRole.Keyword.Declaration to setOf("class", "def", "enum", "export", "extends", "import", "object", "package", "trait", "type", "val", "var"),
    SyntaxRole.Keyword.Modifier to setOf("abstract", "final", "open", "override", "private", "protected", "sealed"),
)
internal val ScalaConstants = setOf("false", "null", "true")
internal val ScalaBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("println"),
    SyntaxRole.Type to setOf("List", "Map", "Option", "Some"),
)
internal val ScalaTypeKeywords = setOf("Any", "Boolean", "Double", "Float", "Int", "Long", "Nothing", "String", "Unit")
