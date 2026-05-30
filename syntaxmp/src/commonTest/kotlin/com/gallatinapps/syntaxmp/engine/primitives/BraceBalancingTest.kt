package com.gallatinapps.syntaxmp.engine.primitives

import kotlin.test.Test
import kotlin.test.assertEquals

class BraceBalancingTest {
    @Test
    fun `brace-balanced end skips quoted and commented braces`() {
        val code = """${'$'}{foo({ bar: "}" /* } */ })}"""

        val end = findBraceBalancedEnd(code = code, openIndex = code.indexOf('{'))

        assertEquals(expected = code.lastIndexOf('}'), actual = end)
    }

    @Test
    fun `brace-balanced end skips line comments`() {
        val code = """
            ${'$'}{foo({
              bar // }
            })}
        """.trimIndent()

        val end = findBraceBalancedEnd(code = code, openIndex = code.indexOf('{'))

        assertEquals(expected = code.lastIndexOf('}'), actual = end)
    }

    @Test
    fun `brace-balanced end supports custom delimiters`() {
        val code = "\\(foo(bar(\" ) \")))"

        val end = findBraceBalancedEnd(
            code = code,
            openIndex = code.indexOf('('),
            openBrace = '(',
            closeBrace = ')',
        )

        assertEquals(expected = code.lastIndexOf(')'), actual = end)
    }

    @Test
    fun `brace-balanced end skips escaped closing braces`() {
        val code = "${'$'}{value:-\\}}"

        val end = findBraceBalancedEnd(code = code, openIndex = code.indexOf('{'))

        assertEquals(expected = code.lastIndexOf('}'), actual = end)
    }
}
