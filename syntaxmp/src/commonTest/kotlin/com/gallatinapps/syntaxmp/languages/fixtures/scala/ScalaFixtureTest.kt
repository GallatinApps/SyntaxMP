package com.gallatinapps.syntaxmp.languages.fixtures.scala

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ScalaFixtureTest {
    private val code = """
        // scala scoring
        final case class Job(name: String, count: Int, enabled: Boolean = true)
        object Jobs {
          def score(job: Job): Int =
            if job.enabled then println("ok") else 0
        }
    """.trimIndent()

    @Test
    fun `scala comments modifiers declarations types functions constants and properties`() {
        assertTokenAt("scala", code, "// scala scoring", "Comment")
        assertTokenAt("scala", code, "final", "Keyword")
        assertTokenAt("scala", code, "case", "Keyword")
        assertTokenAt("scala", code, "class", "Keyword")
        assertTokenAt("scala", code, "Job", "Function")
        assertTokenAt("scala", code, "Job", "Type", occurrence = 2)
        assertTokenAt("scala", code, "String", "Type")
        assertTokenAt("scala", code, "Int", "Type")
        assertTokenAt("scala", code, "Boolean", "Type")
        assertTokenAt("scala", code, "true", "Constant")
        assertTokenAt("scala", code, "object", "Keyword")
        assertTokenAt("scala", code, "def", "Keyword")
        assertTokenAt("scala", code, "score", "Function")
        assertTokenAt("scala", code, "enabled", "Property", occurrence = 1)
        assertTokenAt("scala", code, "\"ok\"", "String")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "scala",
        code = "val label = \"class\"",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `interpolated and raw strings tokenize dollar expressions`() {
        val code = "val label = s\"\"\"Hello ${'$'}name ${'$'}{score(job)}\"\"\"; " +
            "val rawText = raw\"${'$'}name\\n\""

        assertTokenAt("scala", code, "s\"\"\"Hello ", "String")
        assertTokenAt("scala", code, "${'$'}", "Escape")
        assertTokenAt("scala", code, "name", "Variable")
        assertTokenAt("scala", code, "${'$'}{", "Escape")
        assertTokenAt("scala", code, "score", "Function")
        assertTokenAt("scala", code, "${'$'}", "Escape", occurrence = 2)
        assertNoTokenAt("scala", code, "\\n", "Escape")
    }

    @Test
    fun `backtick identifiers are variables rather than strings`() {
        val code = """
            val `reading label` = "ok"
            def `format title`(title: String) = title.trim
        """.trimIndent()

        assertTokenAt("scala", code, "`reading label`", "Variable")
        assertTokenAt("scala", code, "`format title`", "Variable")
        assertNoTokenAt("scala", code, "`reading label`", "String")
        assertTokenAt("scala", code, "\"ok\"", "String")
    }

    @Test
    fun `scala traits annotations generics and multiline interpolation are covered`() {
        val code = """
            @deprecated("use NewRepo", "1.0")
            trait Repository[T] {
              def find(id: String): Option[T]
            }
            object Repos {
              val label = s""" + "\"\"\"" + """Hello ${'$'}name ${'$'}{score(id)}""" + "\"\"\"" + """
            }
        """.trimIndent()

        assertTokenAt("scala", code, "@deprecated", "Annotation")
        assertTokenAt("scala", code, "\"use NewRepo\"", "String")
        assertTokenAt("scala", code, "trait", "Keyword")
        assertTokenAt("scala", code, "Repository", "Type")
        assertTokenAt("scala", code, "T", "Type")
        assertTokenAt("scala", code, "def", "Keyword")
        assertTokenAt("scala", code, "find", "Function")
        assertTokenAt("scala", code, "Option", "Type")
        assertTokenAt("scala", code, "object", "Keyword")
        assertTokenAt("scala", code, "s\"\"\"Hello ", "String")
        assertTokenAt("scala", code, "${'$'}", "Escape")
        assertTokenAt("scala", code, "name", "Variable")
        assertTokenAt("scala", code, "${'$'}{", "Escape")
        assertTokenAt("scala", code, "score", "Function")
    }

    @Test
    fun `scala symbol literals are constants while single quoted literals stay strings`() {
        val code = """
            val symbol = 'name
            val other = 'route_42
            val plus = '+
            val charLike = 'x'
            val plusChar = '+'
            val text = "class"
        """.trimIndent()

        assertTokenAt("scala", code, "'name", "Constant")
        assertTokenAt("scala", code, "'route_42", "Constant")
        assertTokenAt("scala", code, "'+", "Constant")
        assertTokenAt("scala", code, "'x'", "String")
        assertTokenAt("scala", code, "'+'", "String")
        assertTokenAt("scala", code, "\"class\"", "String")
        assertNoTokenAt("scala", code, "class", "Keyword")
    }
}
