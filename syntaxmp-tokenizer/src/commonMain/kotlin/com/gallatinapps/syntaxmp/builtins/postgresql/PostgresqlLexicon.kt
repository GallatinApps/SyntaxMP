package com.gallatinapps.syntaxmp.builtins.postgresql

import com.gallatinapps.syntaxmp.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.builtins.sql.SqlBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.sql.SqlConstants
import com.gallatinapps.syntaxmp.builtins.sql.SqlKeywordRoles
import com.gallatinapps.syntaxmp.builtins.sql.SqlTypeKeywords

internal val PostgresqlKeywordRoles =
    SqlKeywordRoles + lexemeRoleMap(
        SyntaxRole.Keyword to setOf(
            "analyse", "analyze", "array", "collate", "conflict", "copy", "current_date", "current_role",
            "current_time", "current_timestamp", "current_user", "deferrable", "delimiter", "do", "execute",
            "explain", "filter", "function", "generated", "ilike", "language", "lateral", "listen",
            "notify", "nothing", "over", "partition", "perform", "placing", "procedure", "recursive",
            "returning", "serial", "unlisten", "variadic", "window",
        ),
    )
internal val PostgresqlConstants = SqlConstants
internal val PostgresqlTypeKeywords = SqlTypeKeywords
internal val PostgresqlBuiltinRoles = SqlBuiltinRoles
