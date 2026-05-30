package com.gallatinapps.syntaxmp.languages.elixir

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val ElixirKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("after", "cond", "defmodule", "defp", "end", "in", "quote", "receive"),
    SyntaxRole.Keyword.Control to setOf("case", "catch", "do", "else", "for", "if", "rescue", "try", "unless"),
    SyntaxRole.Keyword.Declaration to setOf("def", "fn", "import", "use"),
)

internal val ElixirConstants = setOf("false", "nil", "true")

internal val ElixirBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("inspect", "is_nil", "length"),
    SyntaxRole.Type to setOf("Enum", "IO", "Map", "String"),
)
