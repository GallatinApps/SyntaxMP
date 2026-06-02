package com.gallatinapps.syntaxmp.builtins.rust

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val RustKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf("as", "crate", "dyn", "extern", "false", "in", "mod", "move", "mut", "pub", "ref", "self", "Self", "super", "true", "unsafe", "where"),
    SyntaxRole.Keyword.Control to setOf("break", "continue", "else", "for", "if", "loop", "match", "return", "while"),
    SyntaxRole.Keyword.Declaration to setOf("const", "enum", "fn", "impl", "let", "struct", "trait", "type", "use"),
    SyntaxRole.Keyword.Modifier to setOf("async", "await", "static"),
)
internal val RustConstants = setOf("true", "false", "None", "Some", "Ok", "Err")
internal val RustBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf("assert", "assert_eq", "format", "println", "vec"),
)
internal val RustTypeKeywords = setOf(
    "bool", "char", "f32", "f64", "i8", "i16", "i32", "i64", "i128", "isize", "str", "u8",
    "u16", "u32", "u64", "u128", "usize",
)
