package com.gallatinapps.syntaxmp.builtins.php

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val PhpKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "and", "array", "as", "declare", "default", "echo", "elseif", "enddeclare", "endfor",
        "endforeach", "endif", "endswitch", "endwhile", "false", "global", "implements",
        "instanceof", "new", "null", "or", "true", "xor",
    ),
    SyntaxRole.Keyword.Control to setOf(
        "break", "case", "catch", "continue", "do", "else", "finally", "for", "foreach", "if",
        "return", "switch", "throw", "try", "while",
    ),
    SyntaxRole.Keyword.Declaration to setOf(
        "class", "const", "extends", "fn", "function", "include", "interface", "namespace",
        "require", "trait", "use", "var",
    ),
    SyntaxRole.Keyword.Modifier to setOf("abstract", "final", "private", "protected", "public", "static"),
)
internal val PhpConstants = setOf("true", "false", "null")
internal val PhpBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("array_map", "count", "explode", "implode", "json_encode", "print_r", "strtolower"),
)
internal val PhpTypeKeywords = setOf("array", "bool", "float", "int", "iterable", "mixed", "object", "string", "void")
