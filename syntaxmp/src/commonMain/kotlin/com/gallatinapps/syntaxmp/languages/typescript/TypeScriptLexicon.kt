package com.gallatinapps.syntaxmp.languages.typescript

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptBuiltinRoles
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptConstants
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptKeywordRoles

internal val TypeScriptKeywordRoles =
    JavaScriptKeywordRoles + lexemeRoleMap(
        SyntaxRole.Keyword to setOf("any", "as", "declare", "implements", "keyof", "never", "satisfies", "unknown"),
        SyntaxRole.Keyword.Declaration to setOf("enum", "interface", "namespace", "type"),
        SyntaxRole.Keyword.Modifier to setOf("private", "protected", "public", "readonly"),
    )
internal val TypeScriptConstants = JavaScriptConstants
internal val TypeScriptBuiltinRoles = JavaScriptBuiltinRoles
