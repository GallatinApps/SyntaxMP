package com.gallatinapps.syntaxmp.builtins.fixtures.sql

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class SqlFixtureTest {
    private val code = """
        -- recent pinned notes
        SELECT n.title, COUNT(*) AS total
        FROM notes n
        WHERE n.pinned = TRUE AND n.title LIKE 'Plan%'
        GROUP BY n.title
        ORDER BY total DESC
        LIMIT 10;
    """.trimIndent()

    @Test
    fun `sql comments keywords functions constants strings properties and numbers`() {
        assertTokenAt("sql", code, "-- recent pinned notes", "Comment")
        assertTokenAt("sql", code, "SELECT", "Keyword")
        assertTokenAt("sql", code, "title", "Property")
        assertTokenAt("sql", code, "COUNT", "Function")
        assertTokenAt("sql", code, "AS", "Keyword")
        assertTokenAt("sql", code, "FROM", "Keyword")
        assertTokenAt("sql", code, "WHERE", "Keyword")
        assertTokenAt("sql", code, "TRUE", "Constant")
        assertTokenAt("sql", code, "LIKE", "Keyword")
        assertTokenAt("sql", code, "'Plan%'", "String")
        assertTokenAt("sql", code, "GROUP", "Keyword")
        assertTokenAt("sql", code, "10", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "sql",
        code = "SELECT 'FROM'",
        substring = "FROM",
        category = "Keyword",
    )

    @Test
    fun `keywords inside comments are not highlighted`() {
        val code = """
            -- SELECT FROM WHERE
            /* INSERT UPDATE */
        """.trimIndent()

        assertTokenAt("sql", code, "-- SELECT FROM WHERE", "Comment")
        assertTokenAt("sql", code, "/* INSERT UPDATE */", "Comment")
        assertNoTokenAt("sql", code, "SELECT", "Keyword")
        assertNoTokenAt("sql", code, "UPDATE", "Keyword")
    }

    @Test
    fun `sql pagination keywords are recognized`() {
        val code = """
            SELECT title
            FROM ranked
            ORDER BY updated_at DESC
            FETCH FIRST ROW WITH TIES;

            SELECT title
            FROM ranked
            ORDER BY updated_at DESC
            OFFSET 10 ROWS FETCH NEXT 20 ROWS ONLY;
        """.trimIndent()

        assertTokenAt("sql", code, "OFFSET", "Keyword")
        assertTokenAt("sql", code, "ROWS", "Keyword", occurrence = 0)
        assertTokenAt("sql", code, "FETCH", "Keyword", occurrence = 0)
        assertTokenAt("sql", code, "NEXT", "Keyword")
        assertTokenAt("sql", code, "ONLY", "Keyword")
        assertTokenAt("sql", code, "FIRST", "Keyword")
        assertTokenAt("sql", code, "ROW", "Keyword")
        assertTokenAt("sql", code, "TIES", "Keyword")
    }

    @Test
    fun `sql window functions and date interval values are recognized`() {
        val code = """
            WITH recent_notes AS (
                SELECT id,
                    ROW_NUMBER() OVER (ORDER BY updated_at DESC) AS position
                FROM notes
                WHERE updated_at >= CURRENT_DATE - INTERVAL '14 days'
            )
            SELECT *
            FROM recent_notes;
        """.trimIndent()

        assertTokenAt("sql", code, "WITH", "Keyword")
        assertTokenAt("sql", code, "ROW_NUMBER", "Function")
        assertTokenAt("sql", code, "OVER", "Keyword")
        assertTokenAt("sql", code, "ORDER", "Keyword")
        assertTokenAt("sql", code, "BY", "Keyword")
        assertTokenAt("sql", code, "DESC", "Keyword")
        assertTokenAt("sql", code, "CURRENT_DATE", "Constant")
        assertTokenAt("sql", code, "INTERVAL", "Keyword")
        assertTokenAt("sql", code, "'14 days'", "String")
    }
}
