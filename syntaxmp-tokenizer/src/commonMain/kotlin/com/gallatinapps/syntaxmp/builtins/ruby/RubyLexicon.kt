package com.gallatinapps.syntaxmp.builtins.ruby

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val RubyKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "alias", "and", "begin", "defined?", "end", "ensure", "false", "in", "nil", "not", "or",
        "self", "super", "then", "true", "undef",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "do", "else", "elsif", "for", "if", "next", "redo", "rescue", "retry",
        "return", "unless", "until", "when", "while", "yield",
    ),
    SyntaxRole.Keyword.Declaration to setOf("class", "def", "module"),
)
internal val RubyConstants = setOf("true", "false", "nil")
internal val RubyBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("p", "print", "puts", "require"),
    SyntaxRole.Type to setOf("Array", "Hash", "Integer", "String"),
)
