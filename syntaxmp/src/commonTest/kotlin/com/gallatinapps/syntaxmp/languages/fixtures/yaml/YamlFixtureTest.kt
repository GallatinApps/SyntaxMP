package com.gallatinapps.syntaxmp.languages.fixtures.yaml

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class YamlFixtureTest {
    private val code = """
        # workspace settings
        name: "NoteKit"
        enabled: true
        retries: 3
        note: plain
        items:
          - one
          - 'two'
        nested: { count: 2, flag: false }
    """.trimIndent()

    @Test
    fun `yaml properties values comments and punctuation`() {
        assertTokenAt("yaml", code, "# workspace settings", "Comment")
        assertTokenAt("yaml", code, "name", "Property")
        assertTokenAt("yaml", code, ":", "Punctuation")
        assertTokenAt("yaml", code, "\"NoteKit\"", "String")
        assertTokenAt("yaml", code, "enabled", "Property")
        assertTokenAt("yaml", code, "true", "Constant")
        assertTokenAt("yaml", code, "retries", "Property")
        assertTokenAt("yaml", code, "3", "Number")
        assertTokenAt("yaml", code, "-", "Punctuation")
        assertTokenAt("yaml", code, "'two'", "String")
    }

    @Test
    fun `quoted keywords are not constants`() = assertNoTokenAt(
        language = "yaml",
        code = "name: \"true\"",
        substring = "true",
        category = "Constant",
    )

    @Test
    fun `anchors aliases tags and block scalars tokenize`() {
        val code = """
            defaults: &defaults !Config
              name: NoteKit
            active: *defaults
            note: |-
              class text
              still text
            folded: >+
              return text
            next: true
        """.trimIndent()

        assertTokenAt("yaml", code, "&defaults", "Variable")
        assertTokenAt("yaml", code, "!Config", "Type")
        assertTokenAt("yaml", code, "*defaults", "Variable")
        assertTokenAt("yaml", code, "|-", "String")
        assertTokenAt("yaml", code, "  class text\n  still text\n", "String")
        assertNoTokenAt("yaml", code, "class", "Property")
        assertTokenAt("yaml", code, ">+", "String")
        assertNoTokenAt("yaml", code, "return", "Property")
        assertTokenAt("yaml", code, "next", "Property")
        assertTokenAt("yaml", code, "true", "Constant")
    }
}
