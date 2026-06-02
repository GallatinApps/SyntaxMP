package com.gallatinapps.syntaxmp.builtins.protobuf

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val ProtobufKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "extend", "extensions", "map", "message", "oneof", "option", "optional", "repeated",
        "required", "reserved", "returns", "rpc", "service", "syntax", "to",
    ),
    SyntaxRole.Keyword.Declaration to setOf("enum", "import", "package"),
    SyntaxRole.Keyword.Modifier to setOf("public"),
)
internal val ProtobufConstants = setOf("false", "max", "true")
internal val ProtobufTypeKeywords = setOf(
    "bool", "bytes", "double", "fixed32", "fixed64", "float", "int32", "int64", "map", "sfixed32",
    "sfixed64", "sint32", "sint64", "string", "uint32", "uint64",
)
