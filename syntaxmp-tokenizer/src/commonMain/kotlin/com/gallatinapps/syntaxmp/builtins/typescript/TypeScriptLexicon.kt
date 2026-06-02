package com.gallatinapps.syntaxmp.builtins.typescript

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.builtins.javascript.JavaScriptBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.javascript.JavaScriptConstants
import com.gallatinapps.syntaxmp.builtins.javascript.JavaScriptKeywordRoles

internal val TypeScriptKeywordRoles =
    JavaScriptKeywordRoles + lexemeRoleMap(
        SyntaxRole.Keyword to setOf("any", "as", "declare", "implements", "keyof", "never", "satisfies", "unknown"),
        SyntaxRole.Keyword.Declaration to setOf("enum", "interface", "namespace", "type"),
        SyntaxRole.Keyword.Modifier to setOf("private", "protected", "public", "readonly"),
    )
internal val TypeScriptConstants = JavaScriptConstants
internal val TypeScriptBuiltinRoles = JavaScriptBuiltinRoles
