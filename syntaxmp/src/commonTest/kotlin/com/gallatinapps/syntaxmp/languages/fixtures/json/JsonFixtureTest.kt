package com.gallatinapps.syntaxmp.languages.fixtures.json

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class JsonFixtureTest {
    private val code = """
        {
          "name": "NoteKit",
          "enabled": true,
          "count": -12.5e+2,
          "items": ["one", null, false],
          "escaped": "line\nnext"
        }
    """.trimIndent()

    @Test
    fun `json properties strings numbers constants and punctuation`() {
        assertTokenAt("json", code, "\"name\"", "Property")
        assertTokenAt("json", code, "\"NoteKit\"", "String")
        assertTokenAt("json", code, "true", "Constant")
        assertTokenAt("json", code, "-12.5e+2", "Number")
        assertTokenAt("json", code, "\"items\"", "Property")
        assertTokenAt("json", code, "[", "Punctuation")
        assertTokenAt("json", code, "\"one\"", "String")
        assertTokenAt("json", code, "null", "Constant")
        assertTokenAt("json", code, "false", "Constant")
        assertTokenAt("json", code, "\\n", "Escape")
    }

    @Test
    fun `words inside strings are not constants`() = assertNoTokenAt(
        language = "json",
        code = """{"literal": "true"}""",
        substring = "true",
        category = "Constant",
    )

    @Test
    fun `json comments are not supported comment tokens`() = assertNoTokenAt(
        language = "json",
        code = """{"name": "NoteKit"} // unsupported""",
        substring = "// unsupported",
        category = "Comment",
    )
}
