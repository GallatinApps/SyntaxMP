package com.gallatinapps.syntaxmp.builtins.powershell

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val PowerShellConstants = setOf("\$false", "\$null", "\$true")

internal val PowerShellKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword.Control to setOf(
        "begin", "break", "catch", "class", "continue", "data", "do", "dynamicparam", "else", "elseif",
        "end", "exit", "filter", "finally", "for", "foreach", "from", "function", "if", "in", "param",
        "process", "return", "switch", "throw", "trap", "try", "until", "using", "while",
    ),
)

internal val PowerShellDashOperators = setOf(
    "-and", "-as", "-band", "-bnot", "-bor", "-bxor", "-contains", "-eq", "-ge", "-gt", "-in",
    "-is", "-isnot", "-join", "-le", "-like", "-lt", "-match", "-ne", "-not", "-notcontains",
    "-notin", "-notlike", "-notmatch", "-or", "-replace", "-shl", "-shr", "-split", "-xor",
)
