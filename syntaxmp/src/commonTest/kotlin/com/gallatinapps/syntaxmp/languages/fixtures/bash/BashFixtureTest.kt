package com.gallatinapps.syntaxmp.languages.fixtures.bash

import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class BashFixtureTest {
    @Test
    fun `bash uses shell scanner under bash identity`() {
        val code = """
            # bash scoring
            if [[ -n ${'$'}HOME ]]; then
              echo "${'$'}HOME"
            fi
        """.trimIndent()

        assertTokenAt("bash", code, "# bash scoring", "Comment")
        assertTokenAt("bash", code, "if", "Keyword")
        assertTokenAt("bash", code, "${'$'}HOME", "Variable")
        assertTokenAt("bash", code, "then", "Keyword")
        assertTokenAt("bash", code, "echo", "Function")
    }
}
