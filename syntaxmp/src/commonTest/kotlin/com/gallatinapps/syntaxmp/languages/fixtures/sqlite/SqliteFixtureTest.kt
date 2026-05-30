package com.gallatinapps.syntaxmp.languages.fixtures.sqlite

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class SqliteFixtureTest {
    private val code = """
        -- sqlite scoring
        CREATE TABLE [note items] (
          `select` INTEGER PRIMARY KEY AUTOINCREMENT,
          title TEXT NOT NULL,
          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
          data BLOB DEFAULT X'53514C697465'
        );
        INSERT INTO [note items] (`select`, title, data) VALUES (?1, :title, @blob);
        SELECT json_extract(data, '${'$'}.title') AS title
        FROM [note items]
        WHERE title GLOB 'Plan*' AND rowid = @rowId OR path = ${'$'}path::key(extra);
    """.trimIndent()

    @Test
    fun `sqlite keywords identifiers parameters constants functions and blob literals`() {
        assertTokenAt("sqlite", code, "-- sqlite scoring", "Comment")
        assertTokenAt("sqlite", code, "CREATE", "Keyword")
        assertTokenAt("sqlite", code, "[note items]", "Property")
        assertTokenAt("sqlite", code, "`select`", "Property")
        assertTokenAt("sqlite", code, "INTEGER", "Type")
        assertTokenAt("sqlite", code, "AUTOINCREMENT", "Keyword")
        assertTokenAt("sqlite", code, "CURRENT_TIMESTAMP", "Constant")
        assertTokenAt("sqlite", code, "X'53514C697465'", "String")
        assertTokenAt("sqlite", code, "?1", "Variable")
        assertTokenAt("sqlite", code, ":title", "Variable")
        assertTokenAt("sqlite", code, "@blob", "Variable")
        assertTokenAt("sqlite", code, "json_extract", "Function")
        assertTokenAt("sqlite", code, "GLOB", "Keyword")
        assertTokenAt("sqlite", code, "@rowId", "Variable")
        assertTokenAt("sqlite", code, "${'$'}path::key(extra)", "Variable")
    }

    @Test
    fun `quoted keyword identifiers are not highlighted as keywords`() {
        assertNoTokenAt(
            language = "sqlite",
            code = code,
            substring = "select",
            category = "Keyword",
        )
    }

    @Test
    fun `sqlite pragmas conflict clauses and date functions are recognized`() {
        val code = """
            PRAGMA journal_mode = WAL;
            INSERT OR REPLACE INTO notes(id, title) VALUES(:id, :title)
            ON CONFLICT(id) DO UPDATE SET updated_at = datetime('now');
            SELECT strftime('%Y-%m-%d', created_at), julianday('now'), unixepoch('now') FROM notes;
        """.trimIndent()

        assertTokenAt("sqlite", code, "PRAGMA", "Keyword")
        assertTokenAt("sqlite", code, "INSERT", "Keyword")
        assertTokenAt("sqlite", code, "REPLACE", "Keyword")
        assertTokenAt("sqlite", code, "CONFLICT", "Keyword")
        assertTokenAt("sqlite", code, "DO", "Keyword")
        assertTokenAt("sqlite", code, "UPDATE", "Keyword")
        assertTokenAt("sqlite", code, "datetime", "Type")
        assertTokenAt("sqlite", code, "strftime", "Function")
        assertTokenAt("sqlite", code, "julianday", "Function")
        assertTokenAt("sqlite", code, "unixepoch", "Function")
    }

    @Test
    fun `sqlite keywords inside comments are not highlighted`() {
        val code = "-- PRAGMA CONFLICT"

        assertTokenAt("sqlite", code, "-- PRAGMA CONFLICT", "Comment")
        assertNoTokenAt("sqlite", code, "PRAGMA", "Keyword")
        assertNoTokenAt("sqlite", code, "CONFLICT", "Keyword")
    }
}
