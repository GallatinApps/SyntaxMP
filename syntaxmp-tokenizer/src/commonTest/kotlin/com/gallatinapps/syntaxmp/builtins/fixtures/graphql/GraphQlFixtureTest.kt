package com.gallatinapps.syntaxmp.builtins.fixtures.graphql

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class GraphQlFixtureTest {
    private val code = """
        # Fetch a pinned note
        query Note(${'$'}id: ID!, ${'$'}includeBody: Boolean = true) {
          note(id: ${'$'}id) @include(if: ${'$'}includeBody) {
            title
            body
            count(limit: 10)
          }
        }
    """.trimIndent()

    @Test
    fun `graphql comments keywords variables types directives properties and numbers`() {
        assertTokenAt("graphql", code, "# Fetch a pinned note", "Comment")
        assertTokenAt("graphql", code, "query", "Keyword")
        assertTokenAt("graphql", code, "Note", "Type")
        assertTokenAt("graphql", code, "${'$'}id", "Variable")
        assertTokenAt("graphql", code, "ID", "Type")
        assertTokenAt("graphql", code, "${'$'}includeBody", "Variable")
        assertTokenAt("graphql", code, "Boolean", "Type")
        assertTokenAt("graphql", code, "true", "Constant")
        assertTokenAt("graphql", code, "note", "Function", occurrence = 1)
        assertTokenAt("graphql", code, "@include", "Annotation")
        assertTokenAt("graphql", code, "title", "Property")
        assertTokenAt("graphql", code, "10", "Number")
    }

    @Test
    fun `comment contents are not keywords`() = assertNoTokenAt(
        language = "graphql",
        code = code,
        substring = "Fetch",
        category = "Keyword",
    )

    @Test
    fun `graphql schema type fragments and block strings are anchored`() {
        val code = """
            schema { query: Query }
            type Query {
              note(id: ID!): Note
            }
            type Note {
              title: String
              body: String
              summary: String
            }
            fragment NoteFields on Note {
              title
              body
              summary(text: """ + "\"\"\"" + """type is prose""" + "\"\"\"" + """)
            }
        """.trimIndent()

        assertTokenAt("graphql", code, "schema", "Keyword")
        assertTokenAt("graphql", code, "query", "Keyword")
        assertTokenAt("graphql", code, "Query", "Type")
        assertTokenAt("graphql", code, "type", "Keyword")
        assertTokenAt("graphql", code, "note", "Function")
        assertTokenAt("graphql", code, "ID", "Type")
        assertTokenAt("graphql", code, "Note", "Type")
        assertTokenAt("graphql", code, "fragment", "Keyword")
        assertTokenAt("graphql", code, "NoteFields", "Type")
        assertTokenAt("graphql", code, "on", "Keyword")
        assertTokenAt("graphql", code, "\"\"\"type is prose\"\"\"", "String")
        assertNoTokenAt("graphql", code, "type", "Keyword", occurrence = 2)
    }
}
