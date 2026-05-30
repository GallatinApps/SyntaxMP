package com.gallatinapps.syntaxmp.languages.postgresql

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.languages.sql.SqlBuiltinRoles
import com.gallatinapps.syntaxmp.languages.sql.SqlConstants
import com.gallatinapps.syntaxmp.languages.sql.SqlKeywordRoles
import com.gallatinapps.syntaxmp.languages.sql.SqlTypeKeywords

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
