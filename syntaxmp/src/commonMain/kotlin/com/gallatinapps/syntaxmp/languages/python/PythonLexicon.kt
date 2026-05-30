package com.gallatinapps.syntaxmp.languages.python

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val PythonKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("and", "as", "assert", "del", "global", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "with"),
    SyntaxRole.Keyword.Control to setOf("break", "continue", "elif", "else", "except", "finally", "for", "if", "raise", "return", "try", "while", "yield"),
    SyntaxRole.Keyword.Declaration to setOf("class", "def", "from", "import"),
    SyntaxRole.Keyword.Modifier to setOf("async", "await"),
)
internal val PythonConstants = setOf("True", "False", "None", "Ellipsis", "NotImplemented")
internal val PythonBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("enumerate", "len", "map", "print", "range", "sum"),
    SyntaxRole.Type to setOf("dict", "int", "list", "set", "str"),
)
