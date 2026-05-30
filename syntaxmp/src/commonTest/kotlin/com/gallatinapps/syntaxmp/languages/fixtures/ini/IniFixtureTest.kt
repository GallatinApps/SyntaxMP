package com.gallatinapps.syntaxmp.languages.fixtures.ini

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class IniFixtureTest {
    private val code = """
        ; editor settings
        [editor.preview]
        enabled = true
        title = "NoteKit"
        retries: 3
        export API_URL=https://example.com
    """.trimIndent()

    @Test
    fun `ini sections keys literals and export lines`() {
        assertTokenAt("ini", code, "; editor settings", "Comment")
        assertTokenAt("ini", code, "editor.preview", "Property")
        assertTokenAt("ini", code, "enabled", "Property")
        assertTokenAt("ini", code, "=", "Operator")
        assertTokenAt("ini", code, "true", "Constant")
        assertTokenAt("ini", code, "title", "Property")
        assertTokenAt("ini", code, "\"NoteKit\"", "String")
        assertTokenAt("ini", code, "retries", "Property")
        assertTokenAt("ini", code, "3", "Number")
        assertTokenAt("ini", code, "export", "Keyword")
    }

    @Test
    fun `section names are not plain variables`() = assertNoTokenAt(
        language = "ini",
        code = code,
        substring = "editor.preview",
        category = "Variable",
    )

    @Test
    fun `quoted delimiters and comment markers stay inside values`() {
        val code = """
            [paths]
            url = "https://example.com?a=1;still-value"
            literal: 'key=value # still-value'
            enabled = false # real comment
        """.trimIndent()

        assertTokenAt("ini", code, "paths", "Property")
        assertTokenAt("ini", code, "url", "Property")
        assertTokenAt("ini", code, "\"https://example.com?a=1;still-value\"", "String")
        assertNoTokenAt("ini", code, ";still-value", "Comment")
        assertTokenAt("ini", code, "literal", "Property")
        assertTokenAt("ini", code, "'key=value # still-value'", "String")
        assertNoTokenAt("ini", code, "# still-value", "Comment")
        assertTokenAt("ini", code, "false", "Constant")
        assertTokenAt("ini", code, "# real comment", "Comment")
    }
}
