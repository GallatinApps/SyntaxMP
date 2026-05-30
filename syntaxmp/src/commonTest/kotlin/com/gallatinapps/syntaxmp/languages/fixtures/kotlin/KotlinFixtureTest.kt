package com.gallatinapps.syntaxmp.languages.fixtures.kotlin

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokens
import com.gallatinapps.syntaxmp.languages.fixtures.tk
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KotlinFixtureTest {
    private val code = """
        // kotlin scoring
        import java.util.Locale
        @Deprecated("sample")
        data class Job(val name: String, val count: Int, val enabled: Boolean = true)
        fun score(job: Job): Int {
            println("Score: ${'$'}{job.count}")
            return if (job.enabled) 1 else 0
        }
    """.trimIndent()

    @Test
    fun `kotlin comments imports annotations declarations interpolation and numbers`() {
        assertTokenAt("kotlin", code, "// kotlin scoring", "Comment")
        assertTokenAt("kotlin", code, "import", "Keyword")
        assertTokenAt("kotlin", code, "java.util.Locale", "Variable")
        assertTokenAt("kotlin", code, "@Deprecated", "Annotation")
        assertTokenAt("kotlin", code, "\"sample\"", "String")
        assertTokenAt("kotlin", code, "data", "Keyword")
        assertTokenAt("kotlin", code, "class", "Keyword")
        assertTokenAt("kotlin", code, "Job", "Type")
        assertTokenAt("kotlin", code, "String", "Type")
        assertTokenAt("kotlin", code, "Boolean", "Type")
        assertTokenAt("kotlin", code, "true", "Constant")
        assertTokenAt("kotlin", code, "fun", "Keyword")
        assertTokenAt("kotlin", code, "score", "Function", "function.declaration")
        assertTokenAt("kotlin", code, "count", "Property", occurrence = 1)
        assertTokenAt("kotlin", code, "1", "Number")
    }

    @Test
    fun `strict helper still reports readable token mismatches`() {
        val failure = assertFailsWith<AssertionError> {
            assertTokens(
                language = "kotlin",
                code = "val answer = 42",
                expected = listOf(
                    tk(0, 3, "Keyword", "keyword.declaration"),
                    tk(13, 15, "String", "string"),
                ),
                onlyAssertedCategories = setOf("Keyword", "Number", "String"),
            )
        }

        val message = failure.message.orEmpty()
        assertTrue(message.contains("Token assertion failed."))
        assertTrue(message.contains("T(13..15, String/string)=\"42\""))
        assertTrue(message.contains("T(13..15, Number/number)=\"42\""))
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "kotlin",
        code = "val message = \"fun stuff\"",
        substring = "fun",
        category = "Keyword",
    )

    @Test
    fun `nested braced interpolation finds its closing brace`() {
        val code = """println("count ${'$'}{foo({ bar })}")"""

        assertTokenAt("kotlin", code, "${'$'}{", "Escape")
        assertTokenAt("kotlin", code, "foo", "Function")
        assertTokenAt("kotlin", code, "bar", "Variable")
        assertTokenAt("kotlin", code, "}", "Escape", occurrence = 1)
    }

    @Test
    fun `braced interpolation preserves exact string expression alternation`() {
        val code = """"value ${'$'}{name} done""""

        assertTokens(
            language = "kotlin",
            code = code,
            expected = listOf(
                tk(0, 7, "String", "string"),
                tk(7, 9, "Escape", "escape"),
                tk(9, 13, "Variable", "variable"),
                tk(13, 14, "Escape", "escape"),
                tk(14, 20, "String", "string"),
            ),
            onlyAssertedCategories = setOf("String", "Escape", "Variable"),
        )
    }

    @Test
    fun `triple quoted strings tokenize interpolation without escapes`() {
        val code = "val text = \"\"\"count ${'$'}name ${'$'}{score(job)} \\n fun\"\"\""

        assertTokenAt("kotlin", code, "\"\"\"count ", "String")
        assertTokenAt("kotlin", code, "${'$'}", "Escape")
        assertTokenAt("kotlin", code, "name", "Variable")
        assertTokenAt("kotlin", code, "${'$'}{", "Escape")
        assertTokenAt("kotlin", code, "score", "Function")
        assertNoTokenAt("kotlin", code, "\\n", "Escape")
        assertNoTokenAt("kotlin", code, "fun", "Keyword")
    }

    @Test
    fun `backtick identifiers are variables rather than strings`() {
        val code = """
            fun `reading label`(): String = "ok"
            fun score(): Int = 1
            val `note title` = "Plan"
            note.`reading label`()
        """.trimIndent()

        assertTokenAt("kotlin", code, "`reading label`", "Function", "function.declaration", occurrence = 0)
        assertTokenAt("kotlin", code, "score", "Function", "function.declaration")
        assertTokenAt("kotlin", code, "`note title`", "Variable")
        assertTokenAt("kotlin", code, "`reading label`", "Function", "function.member", occurrence = 1)
        assertNoTokenAt("kotlin", code, "`reading label`", "String")
        assertTokenAt("kotlin", code, "\"ok\"", "String")
    }

    @Test
    fun `kotlin uppercase calls named arguments and member calls use focused roles`() {
        val code = """
            @Composable
            fun Card(title: String) {
                Row(modifier = Modifier.padding(16.dp)) {
                    BasicText(text = title)
                }
            }
        """.trimIndent()

        assertTokenAt("kotlin", code, "@Composable", "Annotation")
        assertTokenAt("kotlin", code, "Card", "Function", "function.declaration")
        assertTokenAt("kotlin", code, "String", "Type")
        assertTokenAt("kotlin", code, "Row", "Function")
        assertTokenAt("kotlin", code, "BasicText", "Function")
        assertTokenAt("kotlin", code, "modifier", "Variable", "variable.parameter")
        assertTokenAt("kotlin", code, "text", "Variable", "variable.parameter")
        assertTokenAt("kotlin", code, "padding", "Function", "function.member")
        assertTokenAt("kotlin", code, "dp", "Property")
    }

    @Test
    fun `kotlin enum entries use property roles while enum types stay types`() {
        val code = """
            enum class NoteStatus {
                Draft,
                Review,
                Pinned;
            }

            val status: NoteStatus = NoteStatus.Pinned
        """.trimIndent()

        assertTokenAt("kotlin", code, "NoteStatus", "Type", occurrence = 0)
        assertTokenAt("kotlin", code, "Draft", "Property")
        assertTokenAt("kotlin", code, "Review", "Property")
        assertTokenAt("kotlin", code, "Pinned", "Property", occurrence = 0)
        assertTokenAt("kotlin", code, "NoteStatus", "Type", occurrence = 1)
        assertTokenAt("kotlin", code, "Pinned", "Property", occurrence = 1)
    }

    @Test
    fun `kotlin use site annotations generics and property declarations use current lexical roles`() {
        val code = """
            @file:Suppress("unused")
            class Box<T>(val item: T) {
                val name: String = item.toString()
            }
            val text = "@Deprecated"
        """.trimIndent()

        assertTokenAt("kotlin", code, "@file", "Annotation")
        assertTokenAt("kotlin", code, "Suppress", "Type")
        assertTokenAt("kotlin", code, "Box", "Type")
        assertTokenAt("kotlin", code, "T", "Type")
        assertTokenAt("kotlin", code, "val", "Keyword")
        assertTokenAt("kotlin", code, "item", "Variable")
        assertTokenAt("kotlin", code, "name", "Variable")
        assertTokenAt("kotlin", code, "String", "Type")
        assertTokenAt("kotlin", code, "toString", "Function")
        assertTokenAt("kotlin", code, "\"@Deprecated\"", "String")
        assertNoTokenAt("kotlin", code, "@Deprecated", "Annotation")
    }

    @Test
    fun `kotlin labels and return-at targets are lexical identifiers not annotations`() {
        val code = """
            @file:Suppress("unused")

            @Deprecated("fixture")
            class Outer {
                fun run(values: List<Int>) {
                    loop@ for (value in values) {
                        if (value == 0) break@loop
                    }
                    values.map mapper@{
                        return@mapper it.toString()
                    }
                    this@Outer.toString()
                }
            }
        """.trimIndent()

        assertTokenAt("kotlin", code, "@file", "Annotation")
        assertTokenAt("kotlin", code, "@Deprecated", "Annotation")
        assertTokenAt("kotlin", code, "loop", "Variable")
        assertNoTokenAt("kotlin", code, "@", "Annotation", occurrence = 2)
        assertTokenAt("kotlin", code, "loop", "Variable", occurrence = 1)
        assertNoTokenAt("kotlin", code, "@loop", "Annotation")
        assertTokenAt("kotlin", code, "mapper", "Variable")
        assertNoTokenAt("kotlin", code, "@", "Annotation", occurrence = 4)
        assertTokenAt("kotlin", code, "mapper", "Variable", occurrence = 1)
        assertNoTokenAt("kotlin", code, "@mapper", "Annotation")
        assertTokenAt("kotlin", code, "Outer", "Type", occurrence = 1)
        assertNoTokenAt("kotlin", code, "@Outer", "Annotation")
    }
}
