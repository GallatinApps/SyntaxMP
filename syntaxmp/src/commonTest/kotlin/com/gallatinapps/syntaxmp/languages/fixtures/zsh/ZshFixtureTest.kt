package com.gallatinapps.syntaxmp.languages.fixtures.zsh

import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ZshFixtureTest {
    @Test
    fun `zsh uses shell scanner under zsh identity`() {
        val code = """
            # zsh setup
            autoload -Uz compinit
            if [[ -n ${'$'}ZSH_VERSION ]]; then
              echo "${'$'}ZSH_VERSION"
            fi
        """.trimIndent()

        assertTokenAt("zsh", code, "# zsh setup", "Comment")
        assertTokenAt("zsh", code, "autoload", "Variable")
        assertTokenAt("zsh", code, "-", "Operator")
        assertTokenAt("zsh", code, "if", "Keyword")
        assertTokenAt("zsh", code, "${'$'}ZSH_VERSION", "Variable")
    }
}
