package com.gallatinapps.syntaxmp.languages.fixtures.elixir

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ElixirFixtureTest {
    private val code = """
        # worker scoring
        defmodule NoteKit.Job do
          @moduledoc "Scores jobs"
          def score(%{count: count, enabled: true} = job) do
            IO.puts("#{job.name}: #{count}")
            {:ok, count * 2}
          end
        end
    """.trimIndent()

    @Test
    fun `elixir comments declarations attributes types interpolation atoms and numbers`() {
        assertTokenAt("elixir", code, "# worker scoring", "Comment")
        assertTokenAt("elixir", code, "defmodule", "Keyword")
        assertTokenAt("elixir", code, "NoteKit", "Type")
        assertTokenAt("elixir", code, "Job", "Type")
        assertTokenAt("elixir", code, "do", "Keyword")
        assertTokenAt("elixir", code, "@moduledoc", "Annotation")
        assertTokenAt("elixir", code, "\"Scores jobs\"", "String")
        assertTokenAt("elixir", code, "def", "Keyword", occurrence = 1)
        assertTokenAt("elixir", code, "score", "Function")
        assertTokenAt("elixir", code, "true", "Constant")
        assertTokenAt("elixir", code, "#{", "Escape")
        assertTokenAt("elixir", code, ":ok", "Constant")
        assertTokenAt("elixir", code, "2", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "elixir",
        code = "label = \"defmodule\"",
        substring = "defmodule",
        category = "Keyword",
    )

    @Test
    fun `elixir sigils pipelines and heredoc interpolation use current lexical roles`() {
        val code = """
            value
            |> String.trim()
            |> String.downcase()
            regex = ~r/defmodule #{name}/i
            literal = ~S(defmodule #{name})
            text = """ + "\"\"\"" + """hello #{name}""" + "\"\"\"" + """
        """.trimIndent()

        assertTokenAt("elixir", code, "|>", "Operator")
        assertTokenAt("elixir", code, "|>", "Operator", occurrence = 1)
        assertTokenAt("elixir", code, "~r/defmodule ", "String")
        assertTokenAt("elixir", code, "#{", "Escape")
        assertTokenAt("elixir", code, "~S(defmodule #{name})", "String")
        assertTokenAt("elixir", code, "\"\"\"hello ", "String")
        assertTokenAt("elixir", code, "#{", "Escape", occurrence = 2)
        assertNoTokenAt("elixir", code, "defmodule", "Keyword")
        assertNoTokenAt("elixir", code, "name", "Variable")
    }
}
