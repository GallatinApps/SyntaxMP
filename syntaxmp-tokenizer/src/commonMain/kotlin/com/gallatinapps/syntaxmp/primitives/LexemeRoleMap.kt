package com.gallatinapps.syntaxmp.primitives

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal typealias LexemeRoleMap = Map<String, SyntaxRole>

internal fun lexemeRoleMap(
    vararg groups: Pair<SyntaxRole, Set<String>>,
): LexemeRoleMap =
    buildMap {
        groups.forEach { (role, lexemes) ->
            lexemes.forEach { lexeme ->
                val existing = get(lexeme)
                require(existing == null) {
                    "Lexeme '$lexeme' is assigned to multiple syntax roles: $existing and $role."
                }
                put(lexeme, role)
            }
        }
    }
