package com.gallatinapps.syntaxmp.languages.perl

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val PerlKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("and", "given", "local", "my", "no", "not", "our", "state"),
    SyntaxRole.Keyword.Control to setOf(
        "continue", "do", "else", "elsif", "for", "foreach", "if", "last", "next", "redo",
        "return", "unless", "until", "when", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf("package", "require", "sub", "use"),
)
internal val PerlConstants = setOf("undef")
internal val PerlBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("chomp", "die", "grep", "join", "map", "print", "push", "say", "shift", "split"),
)
