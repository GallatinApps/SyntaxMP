package com.gallatinapps.syntaxmp.builtins.graphql

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val GraphQlKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "directive", "enum", "extend", "fragment", "implements", "input", "interface", "mutation",
        "on", "query", "scalar", "schema", "subscription", "type", "union",
    ),
)

internal val GraphQlConstants = setOf("false", "null", "true")
