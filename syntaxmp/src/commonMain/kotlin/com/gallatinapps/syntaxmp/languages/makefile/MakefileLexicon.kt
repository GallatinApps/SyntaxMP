package com.gallatinapps.syntaxmp.languages.makefile

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val MakefileDirectiveRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("define", "else", "endef", "endif", "export", "ifneq", "ifeq", "include", "override"),
)
