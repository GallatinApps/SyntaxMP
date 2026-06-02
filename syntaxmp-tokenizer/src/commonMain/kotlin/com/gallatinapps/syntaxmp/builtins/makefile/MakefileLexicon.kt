package com.gallatinapps.syntaxmp.builtins.makefile

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val MakefileDirectiveRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("define", "else", "endef", "endif", "export", "ifneq", "ifeq", "include", "override"),
)
