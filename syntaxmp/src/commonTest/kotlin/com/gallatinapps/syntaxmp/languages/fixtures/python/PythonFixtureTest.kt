package com.gallatinapps.syntaxmp.languages.fixtures.python

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class PythonFixtureTest {
    private val code = """
        # python scoring
        class Job:
            """ + "\"\"\"" + """Doc string""" + "\"\"\"" + """
            def score(self, value):
                if value is None:
                    return len(self.name) + 1
                return False
    """.trimIndent()

    @Test
    fun `python comments declarations docstrings functions constants properties and numbers`() {
        assertTokenAt("python", code, "# python scoring", "Comment")
        assertTokenAt("python", code, "class", "Keyword")
        assertTokenAt("python", code, "Job", "Type")
        assertTokenAt("python", code, "\"\"\"Doc string\"\"\"", "String")
        assertTokenAt("python", code, "def", "Keyword")
        assertTokenAt("python", code, "score", "Function")
        assertTokenAt("python", code, "if", "Keyword")
        assertTokenAt("python", code, "None", "Constant")
        assertTokenAt("python", code, "return", "Keyword")
        assertTokenAt("python", code, "len", "Function")
        assertTokenAt("python", code, "name", "Property")
        assertTokenAt("python", code, "1", "Number")
        assertTokenAt("python", code, "False", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "python",
        code = "label = \"return\"",
        substring = "return",
        category = "Keyword",
    )

    @Test
    fun `f strings and percent formats tokenize embedded syntax`() {
        val code = "label = f\"score {value + 1}\"; legacy = \"%03d\" % count"

        assertTokenAt("python", code, "f\"score ", "String")
        assertTokenAt("python", code, "{", "Escape")
        assertTokenAt("python", code, "value", "Variable")
        assertTokenAt("python", code, "1", "Number")
        assertTokenAt("python", code, "}", "Escape")
        assertTokenAt("python", code, "%03d", "Escape")
    }

    @Test
    fun `prefixed triple strings stay coherent`() {
        val code = "label = f\"\"\"score {value + 1}\"\"\""

        assertTokenAt("python", code, "f\"\"\"score ", "String")
        assertTokenAt("python", code, "{", "Escape")
        assertTokenAt("python", code, "value", "Variable")
        assertTokenAt("python", code, "1", "Number")
        assertTokenAt("python", code, "}", "Escape")
    }

    @Test
    fun `raw and raw f string prefixes preserve raw escapes`() {
        val code = "raw = r\"return\\n\"; combo = fr\"path {name}\\n\""

        assertNoTokenAt("python", code, "return", "Keyword")
        assertNoTokenAt("python", code, "\\n", "Escape")
        assertTokenAt("python", code, "fr\"path ", "String")
        assertTokenAt("python", code, "{", "Escape")
        assertTokenAt("python", code, "name", "Variable")
        assertTokenAt("python", code, "}", "Escape")
        assertNoTokenAt("python", code, "\\n", "Escape", occurrence = 1)
    }

    @Test
    fun `decorators are annotations but matrix operator remains operator`() {
        val code = """
            @dataclass(frozen=True)
            @property
            def title(self):
                return self.name

            result = left @ right
        """.trimIndent()

        assertTokenAt("python", code, "@dataclass", "Annotation")
        assertTokenAt("python", code, "@property", "Annotation")
        assertTokenAt("python", code, "True", "Constant")
        assertTokenAt("python", code, "@", "Operator", occurrence = 2)
    }
}
