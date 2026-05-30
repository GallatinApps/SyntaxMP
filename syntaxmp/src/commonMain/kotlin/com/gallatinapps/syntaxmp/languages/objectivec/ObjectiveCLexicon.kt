package com.gallatinapps.syntaxmp.languages.objectivec

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val ObjectiveCKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("auto", "default", "extern", "inline", "implementation", "nonatomic", "property", "selector", "self", "strong", "super", "weak"),
    SyntaxRole.Keyword.Control to setOf("break", "case", "continue", "do", "else", "for", "goto", "if", "return", "switch", "while"),
    SyntaxRole.Keyword.Declaration to setOf("const", "enum", "interface", "struct", "typedef"),
    SyntaxRole.Keyword.Modifier to setOf("static"),
)
internal val ObjectiveCConstants = setOf("NO", "NULL", "YES", "false", "nil", "true")
internal val ObjectiveCBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("NSLog", "alloc", "copy", "init", "mutableCopy", "new"),
)
internal val ObjectiveCTypeKeywords = setOf(
    "BOOL", "CGFloat", "NSInteger", "NSUInteger", "NSString", "NSArray", "NSDictionary", "NSError",
    "id", "instancetype", "int", "void",
)
