package com.gallatinapps.syntaxmp.languages.scss

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val ScssPreprocessorKeywordRoles = setOf(
    "at-root", "content", "debug", "each", "else", "error", "extend", "for", "function",
    "if", "include", "mixin", "return", "use", "warn", "while",
).associateWith { name ->
    SyntaxRole.Keyword.AtRule
}
