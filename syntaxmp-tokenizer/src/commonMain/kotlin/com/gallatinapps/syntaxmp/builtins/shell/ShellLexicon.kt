package com.gallatinapps.syntaxmp.builtins.shell

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val ShellKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword.Control to setOf(
        "case", "do", "done", "elif", "else", "esac", "fi", "for", "function", "if", "in",
        "select", "then", "until", "while",
    ),
)

internal val ShellBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf(
        "awk", "cat", "cd", "chmod", "cp", "curl", "echo", "export", "find", "grep", "mkdir", "mv",
        "printf", "pwd", "rm", "sed", "set", "tar", "test",
    ),
)
