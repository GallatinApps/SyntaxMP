package com.gallatinapps.syntaxmp.languages.fixtures.groovy

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class GroovyFixtureTest {
    private val code = """
        // groovy scoring
        class Job {
          def score(name, count) {
            def label = "hello ${'$'}{name}"
            if (count > 0) {
              return label
            }
          }
        }
    """.trimIndent()

    @Test
    fun `groovy declarations interpolation conditionals and numbers`() {
        assertTokenAt("groovy", code, "// groovy scoring", "Comment")
        assertTokenAt("groovy", code, "class", "Keyword")
        assertTokenAt("groovy", code, "Job", "Type")
        assertTokenAt("groovy", code, "def", "Keyword")
        assertTokenAt("groovy", code, "score", "Function")
        assertTokenAt("groovy", code, "${'$'}{", "Escape")
        assertTokenAt("groovy", code, "name", "Variable", occurrence = 1)
        assertTokenAt("groovy", code, "if", "Keyword")
        assertTokenAt("groovy", code, "0", "Number")
        assertTokenAt("groovy", code, "return", "Keyword")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "groovy",
        code = "def label = \"class\"",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `triple quoted slashy and dollar slashy strings tokenize interpolation`() {
        val code = "def triple = \"\"\"hello ${'$'}name ${'$'}{score(count)}\"\"\"; " +
            "def single = '''class ${'$'}name'''; def slashy = /job-${'$'}{name}/; " +
            "def dollarSlashy = ${'$'}/home-${'$'}{name}/${'$'}"

        assertTokenAt("groovy", code, "\"\"\"hello ", "String")
        assertTokenAt("groovy", code, "${'$'}", "Escape")
        assertTokenAt("groovy", code, "name", "Variable")
        assertTokenAt("groovy", code, "${'$'}{", "Escape")
        assertTokenAt("groovy", code, "score", "Function")
        assertNoTokenAt("groovy", code, "class", "Keyword")
        assertTokenAt("groovy", code, "/job-", "String")
        assertTokenAt("groovy", code, "${'$'}{", "Escape", occurrence = 1)
        assertTokenAt("groovy", code, "${'$'}/home-", "String")
        assertTokenAt("groovy", code, "${'$'}{", "Escape", occurrence = 2)
    }

    @Test
    fun `groovy annotations closures dsl calls and property access keep lexical roles`() {
        val code = """
            @Grab("org.example:demo:1.0")
            def doubled = values.collect { value -> value * 2 }
            plugins { id "java" }
            project.version = "1.0"
        """.trimIndent()

        assertTokenAt("groovy", code, "@Grab", "Annotation")
        assertTokenAt("groovy", code, "collect", "Property")
        assertTokenAt("groovy", code, "values", "Variable")
        assertTokenAt("groovy", code, "value", "Variable", occurrence = 1)
        assertTokenAt("groovy", code, "->", "Operator")
        assertTokenAt("groovy", code, "plugins", "Variable")
        assertTokenAt("groovy", code, "id", "Variable")
        assertTokenAt("groovy", code, "version", "Property")
        assertTokenAt("groovy", code, "\"1.0\"", "String")
    }
}
