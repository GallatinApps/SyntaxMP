package com.gallatinapps.syntaxmp.languages.fixtures.lua

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class LuaFixtureTest {
    private val code = """
        --[[ module comment ]]
        local jobs = {{ name = "scan", count = 4, enabled = true }}
        function score(job)
          if job.enabled then
            table.insert(jobs, job)
            return job.count * 2
          end
          return nil
        end
    """.trimIndent()

    @Test
    fun `lua comments keywords functions properties constants and numbers`() {
        assertTokenAt("lua", code, "--[[ module comment ]]", "Comment")
        assertTokenAt("lua", code, "local", "Keyword")
        assertTokenAt("lua", code, "jobs", "Variable")
        assertTokenAt("lua", code, "name", "Variable")
        assertTokenAt("lua", code, "\"scan\"", "String")
        assertTokenAt("lua", code, "4", "Number")
        assertTokenAt("lua", code, "true", "Constant")
        assertTokenAt("lua", code, "function", "Keyword")
        assertTokenAt("lua", code, "score", "Function")
        assertTokenAt("lua", code, "if", "Keyword")
        assertTokenAt("lua", code, "enabled", "Property", occurrence = 1)
        assertTokenAt("lua", code, "table", "Variable", "variable.namespace")
        assertTokenAt("lua", code, "insert", "Function")
        assertTokenAt("lua", code, "nil", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "lua",
        code = "local label = \"function\"",
        substring = "function",
        category = "Keyword",
    )

    @Test
    fun `long bracket strings and comments support equals delimiters`() {
        val code = """
            --[=[
            function comment
            ]=]
            local text = [==[
            return value
            ]==]
        """.trimIndent()

        assertTokenAt("lua", code, "--[=[\nfunction comment\n]=]", "Comment")
        assertTokenAt("lua", code, "[==[\nreturn value\n]==]", "String")
        assertNoTokenAt("lua", code, "function", "Keyword")
        assertNoTokenAt("lua", code, "return", "Keyword")
    }

    @Test
    fun `lua varargs labels goto table fields and comment markers in strings are covered`() {
        val code = """
            local function pack(...)
              local row = { name = "scan", ["count"] = 1 }
              ::again::
              if row.name then goto again end
              local marker = "-- not comment"
              return ...
            end
        """.trimIndent()

        assertTokenAt("lua", code, "(...)", "Punctuation")
        assertTokenAt("lua", code, "name", "Variable")
        assertTokenAt("lua", code, "\"count\"", "String")
        assertTokenAt("lua", code, "again", "Property")
        assertTokenAt("lua", code, "name", "Property", occurrence = 1)
        assertTokenAt("lua", code, "goto", "Keyword")
        assertTokenAt("lua", code, "\"-- not comment\"", "String")
        assertTokenAt("lua", code, "...", "Punctuation", occurrence = 1)
        assertNoTokenAt("lua", code, "-- not comment", "Comment")
    }
}
