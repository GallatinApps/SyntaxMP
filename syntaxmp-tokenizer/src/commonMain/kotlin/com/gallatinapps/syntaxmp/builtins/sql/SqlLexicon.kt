package com.gallatinapps.syntaxmp.builtins.sql

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole

internal val SqlKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "add", "alter", "and", "as", "asc", "begin", "between", "by", "case", "check", "commit",
        "constraint", "create", "delete", "desc", "distinct", "drop", "else", "end", "exists", "fetch",
        "first", "from", "group", "having", "if", "in", "index", "inner", "insert", "interval", "into",
        "is", "join", "left", "like", "limit", "next", "not", "null", "offset", "on", "only", "or",
        "order", "outer", "over", "primary", "references", "right", "rollback", "row", "rows", "select",
        "set", "table", "then", "ties", "union", "unique", "update", "values", "view", "when", "where",
        "with",
    ),
)
internal val SqlConstants = setOf("true", "false", "null", "current_date", "current_time", "current_timestamp")
internal val SqlTypeKeywords = setOf(
    "bigint", "bigserial", "blob", "boolean", "char", "date", "datetime", "decimal", "double", "float",
    "int", "integer", "json", "jsonb", "numeric", "real", "serial", "smallint", "smallserial", "text",
    "time", "timestamp", "uuid", "varchar",
)
internal val SqlBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf(
        "avg", "coalesce", "count", "date", "datetime", "ifnull", "json_extract", "lower", "max", "min",
        "now", "round", "row_number", "strftime", "substr", "sum", "upper",
    ),
)
