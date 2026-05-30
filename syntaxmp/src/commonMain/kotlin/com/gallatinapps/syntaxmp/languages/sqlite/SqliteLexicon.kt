package com.gallatinapps.syntaxmp.languages.sqlite

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.languages.sql.SqlBuiltinRoles
import com.gallatinapps.syntaxmp.languages.sql.SqlConstants
import com.gallatinapps.syntaxmp.languages.sql.SqlKeywordRoles
import com.gallatinapps.syntaxmp.languages.sql.SqlTypeKeywords

internal val SqliteKeywordRoles =
    SqlKeywordRoles + lexemeRoleMap(
        SyntaxRole.Keyword to setOf(
            "abort", "action", "after", "all", "always", "analyze", "attach", "autoincrement", "before",
            "cascade", "cast", "collate", "column", "conflict", "cross", "current", "database", "deferrable",
            "deferred", "detach", "do", "each", "escape", "except", "exclude", "exclusive", "explain",
            "fail", "filter", "first", "following", "foreign", "full", "generated", "glob", "groups",
            "ignore", "immediate", "indexed", "initially", "instead", "intersect", "isnull", "key", "last",
            "match", "materialized", "natural", "no", "nothing", "notnull", "nulls", "of", "offset",
            "others", "over", "partition", "plan", "pragma", "preceding", "query", "raise", "range",
            "recursive", "regexp", "reindex", "release", "rename", "replace", "restrict", "returning",
            "row", "rows", "savepoint", "temp", "temporary", "ties", "to", "transaction", "trigger",
            "unbounded", "using", "vacuum", "virtual", "window", "without",
        ),
    )
internal val SqliteConstants = SqlConstants + setOf("current_date", "current_time", "current_timestamp")
internal val SqliteTypeKeywords = SqlTypeKeywords
internal val SqliteBuiltinRoles =
    SqlBuiltinRoles + lexemeRoleMap(
        SyntaxRole.Function.Builtin to setOf(
            "abs", "changes", "char", "glob", "hex", "iif", "instr", "json_array", "json_extract",
            "json_insert", "json_object", "json_remove", "json_set", "last_insert_rowid", "length",
            "like", "likelihood", "likely", "load_extension", "ltrim", "nullif", "printf", "quote",
            "random", "randomblob", "replace", "rtrim", "sign", "sqlite_version", "substring",
            "total", "total_changes", "trim", "typeof", "unicode", "unlikely", "zeroblob",
        ),
    )
