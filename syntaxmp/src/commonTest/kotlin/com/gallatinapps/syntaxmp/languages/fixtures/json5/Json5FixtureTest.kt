package com.gallatinapps.syntaxmp.languages.fixtures.json5

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class Json5FixtureTest {
    private val code = """
        // relaxed object
        {
          unquoted: 'value',
          hex: 0x2A,
          trailing: true,
          special: NaN,
          also: Infinity,
          /* block note */
          dollar_key: "${'$'}value"
        }
    """.trimIndent()

    @Test
    fun `json5 comments properties numbers constants strings and punctuation`() {
        assertTokenAt("json5", code, "// relaxed object", "Comment")
        assertTokenAt("json5", code, "unquoted", "Property")
        assertTokenAt("json5", code, "'value'", "String")
        assertTokenAt("json5", code, "hex", "Property")
        assertTokenAt("json5", code, "0x2A", "Number")
        assertTokenAt("json5", code, "true", "Constant")
        assertTokenAt("json5", code, "NaN", "Constant")
        assertTokenAt("json5", code, "Infinity", "Constant")
        assertTokenAt("json5", code, "/* block note */", "Comment")
        assertTokenAt("json5", code, "\"${'$'}value\"", "String")
    }

    @Test
    fun `property-looking text inside strings is not a property`() = assertNoTokenAt(
        language = "json5",
        code = code,
        substring = "value",
        category = "Property",
    )

    @Test
    fun `json5 signed constants trailing commas and comment markers stay in policy`() {
        val code = """
            {
              values: [NaN, -Infinity, +Infinity, Infinity,],
              text: "/* not a comment */"
            }
        """.trimIndent()

        assertTokenAt("json5", code, "values", "Property")
        assertTokenAt("json5", code, "NaN", "Constant")
        assertTokenAt("json5", code, "-Infinity", "Constant")
        assertTokenAt("json5", code, "+Infinity", "Constant")
        assertTokenAt("json5", code, "Infinity", "Constant", occurrence = 2)
        assertTokenAt("json5", code, ",],", "Punctuation")
        assertTokenAt("json5", code, "\"/* not a comment */\"", "String")
        assertNoTokenAt("json5", code, "/* not a comment */", "Comment")
    }
}
