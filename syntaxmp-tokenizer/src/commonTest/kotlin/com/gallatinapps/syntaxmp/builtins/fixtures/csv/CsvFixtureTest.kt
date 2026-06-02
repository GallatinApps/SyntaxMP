package com.gallatinapps.syntaxmp.builtins.fixtures.csv

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokens
import com.gallatinapps.syntaxmp.builtins.fixtures.tk
import kotlin.test.Test

class CsvFixtureTest {
    @Test
    fun `commas inside quoted fields are not delimiters`() {
        val code = "a,b,\"c,d\""

        assertTokens(
            language = "csv",
            code = code,
            expected = listOf(
                tk(1, 2, "Punctuation", "punctuation"),
                tk(3, 4, "Punctuation", "punctuation"),
            ),
            onlyAssertedCategories = setOf("Punctuation"),
        )
        assertNoTokenAt("csv", code, ",", "Punctuation", occurrence = 2)
        assertNoTokenAt("csv", code, "\"c,d\"", "String")
    }

    @Test
    fun `escaped quotes do not end quoted fields`() {
        val code = "a,\"b \"\"c,d\"\" e\",f"

        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 0)
        assertNoTokenAt("csv", code, ",", "Punctuation", occurrence = 1)
        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 2)
        assertNoTokenAt("csv", code, "\"b \"\"c,d\"\" e\"", "String")
    }

    @Test
    fun `quoted fields can span lines`() {
        val code = "id,\"first line\nsecond, line\",done"

        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 0)
        assertNoTokenAt("csv", code, ",", "Punctuation", occurrence = 1)
        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 2)
        assertNoTokenAt("csv", code, "\"first line\nsecond, line\"", "String")
    }

    @Test
    fun `stray quotes in unquoted fields do not hide delimiters`() {
        val code = "a,b\"c,d\",e"

        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 0)
        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 1)
        assertTokenAt("csv", code, ",", "Punctuation", occurrence = 2)
        assertNoTokenAt("csv", code, "\"c,d\"", "String")
    }
}
