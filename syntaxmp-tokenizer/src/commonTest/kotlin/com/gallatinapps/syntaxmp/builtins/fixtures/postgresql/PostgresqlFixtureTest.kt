package com.gallatinapps.syntaxmp.builtins.fixtures.postgresql

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class PostgresqlFixtureTest {
    private val code = """
        -- pg scoring
        CREATE TABLE notes (id serial PRIMARY KEY, data jsonb);
        CREATE FUNCTION title() RETURNS text LANGUAGE plpgsql AS ${'$'}body${'$'}
        BEGIN
          RETURN 'ok';
        END
        ${'$'}body${'$'};
        SELECT data->>'title' AS title FROM notes WHERE id::int = 1 RETURNING id;
        SELECT ${'$'}${'$'}plain${'$'}${'$'}, ${'$'}tag${'$'}not closed${'$'}TAG${'$'};
    """.trimIndent()

    @Test
    fun `postgresql keywords dollar strings casts and json operators`() {
        assertTokenAt("postgresql", code, "-- pg scoring", "Comment")
        assertTokenAt("postgresql", code, "CREATE", "Keyword")
        assertTokenAt("postgresql", code, "TABLE", "Keyword")
        assertTokenAt("postgresql", code, "serial", "Type")
        assertTokenAt("postgresql", code, "jsonb", "Type")
        assertTokenAt("postgresql", code, "FUNCTION", "Keyword")
        assertTokenAt("postgresql", code, "LANGUAGE", "Keyword")
        assertTokenAt("postgresql", code, "${'$'}body${'$'}\nBEGIN\n  RETURN 'ok';\nEND\n${'$'}body${'$'}", "String")
        assertTokenAt("postgresql", code, "->>", "Operator")
        assertTokenAt("postgresql", code, "::", "Operator")
        assertTokenAt("postgresql", code, "RETURNING", "Keyword")
        assertTokenAt("postgresql", code, "${'$'}${'$'}plain${'$'}${'$'}", "String")
    }

    @Test
    fun `mismatched dollar quote tags do not close early`() = assertNoTokenAt(
        language = "postgresql",
        code = code,
        substring = "${'$'}tag${'$'}not closed${'$'}TAG${'$'}",
        category = "String",
    )

    @Test
    fun `postgresql dialect keywords and functions are recognized`() {
        val code = """
            SELECT now(), current_user
            FROM LATERAL jsonb_each(data) AS entry(key, value)
            WHERE title ILIKE '%plan%'
            ON CONFLICT (id) DO NOTHING
            RETURNING id;
        """.trimIndent()

        assertTokenAt("postgresql", code, "SELECT", "Keyword")
        assertTokenAt("postgresql", code, "now", "Function")
        assertTokenAt("postgresql", code, "current_user", "Keyword")
        assertTokenAt("postgresql", code, "LATERAL", "Keyword")
        assertTokenAt("postgresql", code, "jsonb_each", "Function")
        assertTokenAt("postgresql", code, "ILIKE", "Keyword")
        assertTokenAt("postgresql", code, "CONFLICT", "Keyword")
        assertTokenAt("postgresql", code, "DO", "Keyword")
        assertTokenAt("postgresql", code, "NOTHING", "Keyword")
        assertTokenAt("postgresql", code, "RETURNING", "Keyword")
    }

    @Test
    fun `postgresql keywords inside strings and comments are not highlighted`() {
        val code = """
            -- RETURNING stays a comment
            DO ${'$'}${'$'}RETURNING${'$'}${'$'};
            SELECT 'FROM';
        """.trimIndent()

        assertTokenAt("postgresql", code, "-- RETURNING stays a comment", "Comment")
        assertTokenAt("postgresql", code, "${'$'}${'$'}RETURNING${'$'}${'$'}", "String")
        assertTokenAt("postgresql", code, "'FROM'", "String")
        assertNoTokenAt("postgresql", code, "RETURNING", "Keyword")
        assertNoTokenAt("postgresql", code, "RETURNING", "Keyword", occurrence = 1)
        assertNoTokenAt("postgresql", code, "FROM", "Keyword")
    }
}
