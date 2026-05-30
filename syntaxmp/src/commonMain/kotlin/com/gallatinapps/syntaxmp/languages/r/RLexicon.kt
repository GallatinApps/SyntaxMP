package com.gallatinapps.syntaxmp.languages.r

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val RKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("in", "repeat"),
    SyntaxRole.Keyword.Control to setOf("break", "else", "for", "if", "next", "return", "while"),
    SyntaxRole.Keyword.Declaration to setOf("function"),
)

internal val RConstants = setOf("FALSE", "Inf", "NA", "NaN", "NULL", "TRUE")

internal val RBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf(
        "c", "data.frame", "filter", "library", "list", "mean", "mutate", "paste", "print", "sum",
    ),
)
