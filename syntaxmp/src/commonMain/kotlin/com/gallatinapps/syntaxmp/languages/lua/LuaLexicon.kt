package com.gallatinapps.syntaxmp.languages.lua

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val LuaKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("and", "elseif", "end", "in", "local", "not", "or", "repeat", "then"),
    SyntaxRole.Keyword.Control to setOf("break", "do", "else", "for", "goto", "if", "return", "until", "while"),
    SyntaxRole.Keyword.Declaration to setOf("function"),
)

internal val LuaConstants = setOf("false", "nil", "true")

internal val LuaBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("ipairs", "pairs", "print", "require", "tonumber", "tostring"),
    SyntaxRole.Variable.Namespace to setOf("string", "table"),
)
