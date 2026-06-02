package com.gallatinapps.syntaxmp.builtins.fixtures.toml

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class TomlFixtureTest {
    private val code = """
        # version catalog
        [versions]
        compose = "1.8.0"
        enabled = true
        retries = 3
        released = 2026-05-20T12:30:00Z
        [libraries.markdownmp]
        module = "com.gallatinapps.markdownmp:markdownmp-compose"
        version.ref = "markdownmp"
    """.trimIndent()

    @Test
    fun `toml keys table headers values and comments`() {
        assertTokenAt("toml", code, "# version catalog", "Comment")
        assertTokenAt("toml", code, "[", "Punctuation")
        assertTokenAt("toml", code, "versions", "Property")
        assertTokenAt("toml", code, "compose", "Property")
        assertTokenAt("toml", code, "=", "Operator")
        assertTokenAt("toml", code, "\"1.8.0\"", "String")
        assertTokenAt("toml", code, "true", "Constant")
        assertTokenAt("toml", code, "3", "Number")
        assertTokenAt("toml", code, "2026-05-20T12:30:00Z", "Number")
        assertTokenAt("toml", code, "ref", "Property")
    }

    @Test
    fun `literal text inside strings is not a property`() = assertNoTokenAt(
        language = "toml",
        code = "title = \"version\"",
        substring = "version",
        category = "Property",
    )

    @Test
    fun `multiline basic and literal strings tokenize as single strings`() {
        val code = """
            basic = ""${'"'}
            class text
            ${'"'}""
            literal = '''
            version text
            '''
            enabled = false
        """.trimIndent()
        val basic = "\"\"\"\nclass text\n\"\"\""
        val literal = "'''\nversion text\n'''"

        assertTokenAt("toml", code, basic, "String")
        assertTokenAt("toml", code, literal, "String")
        assertNoTokenAt("toml", code, "class", "Property")
        assertNoTokenAt("toml", code, "version", "Property")
        assertTokenAt("toml", code, "false", "Constant")
    }
}
