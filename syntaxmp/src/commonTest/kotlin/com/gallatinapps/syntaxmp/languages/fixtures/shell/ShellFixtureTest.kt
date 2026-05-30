package com.gallatinapps.syntaxmp.languages.fixtures.shell

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ShellFixtureTest {
    private val code = """
        # shell scoring
        export APP_HOME=/app
        if grep -q needle file; then
          echo "home ${'$'}HOME"
          mkdir -p "${'$'}{APP_HOME}/out"
        fi
    """.trimIndent()

    @Test
    fun `shell comments builtins keywords variables strings and operators`() {
        assertTokenAt("shell", code, "# shell scoring", "Comment")
        assertTokenAt("shell", code, "export", "Function")
        assertTokenAt("shell", code, "APP_HOME", "Variable")
        assertTokenAt("shell", code, "=/", "Operator")
        assertTokenAt("shell", code, "if", "Keyword")
        assertTokenAt("shell", code, "grep", "Function")
        assertTokenAt("shell", code, "-", "Operator")
        assertTokenAt("shell", code, "then", "Keyword")
        assertTokenAt("shell", code, "echo", "Function")
        assertTokenAt("shell", code, "\"home ", "String")
        assertTokenAt("shell", code, "${'$'}HOME", "Variable")
        assertTokenAt("shell", code, "mkdir", "Function")
        assertTokenAt("shell", code, "${'$'}{", "Escape")
        assertTokenAt("shell", code, "APP_HOME", "Variable", occurrence = 1)
        assertTokenAt("shell", code, "/out\"", "String")
        assertTokenAt("shell", code, "fi", "Keyword", occurrence = 1)
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "shell",
        code = "echo \"if\"",
        substring = "if",
        category = "Keyword",
    )

    @Test
    fun `single quoted variables and quoted comments stay literal`() {
        val code = """
            echo '${'$'}HOME # literal'
            echo "# still string" # real comment
        """.trimIndent()

        assertTokenAt("shell", code, "'${'$'}HOME # literal'", "String")
        assertNoTokenAt("shell", code, "${'$'}HOME", "Variable")
        assertNoTokenAt("shell", code, "# literal", "Comment")
        assertNoTokenAt("shell", code, "# still string", "Comment")
        assertTokenAt("shell", code, "# real comment", "Comment")
    }

    @Test
    fun `braced expansions tokenize interiors and nested variables`() {
        val code = """
            echo ${'$'}{VAR:-default}
            echo ${'$'}{array[${'$'}i]}
            echo ${'$'}{outer:-${'$'}{inner}}
            echo ${'$'}{quoted:-"}"}
        """.trimIndent()

        assertTokenAt("shell", code, "${'$'}{", "Escape")
        assertTokenAt("shell", code, "VAR", "Variable")
        assertTokenAt("shell", code, "default", "Variable")
        assertTokenAt("shell", code, "array", "Variable")
        assertTokenAt("shell", code, "${'$'}i", "Variable")
        assertTokenAt("shell", code, "outer", "Variable")
        assertTokenAt("shell", code, "${'$'}{", "Escape", occurrence = 3)
        assertTokenAt("shell", code, "inner", "Variable")
        assertTokenAt("shell", code, "quoted", "Variable")
        assertTokenAt("shell", code, "\"}\"", "String")
    }

    @Test
    fun `command substitutions arithmetic substitutions and heredocs are tokenized`() {
        val code = """
            echo "cwd ${'$'}(pwd) math ${'$'}((count + 1)) old `pwd`"
            cat <<EOF
            if ${'$'}HOME
            EOF
            cat <<'EOF'
            then ${'$'}HOME
            EOF
        """.trimIndent()

        assertTokenAt("shell", code, "${'$'}(", "Escape")
        assertTokenAt("shell", code, "pwd", "Function")
        assertTokenAt("shell", code, "${'$'}((", "Escape")
        assertTokenAt("shell", code, "count", "Variable")
        assertTokenAt("shell", code, "1", "Number")
        assertTokenAt("shell", code, "`", "Escape")
        assertTokenAt("shell", code, "pwd", "Function", occurrence = 1)
        assertTokenAt("shell", code, "`", "Escape", occurrence = 1)
        assertTokenAt("shell", code, "<<EOF\nif ${'$'}HOME\nEOF", "String")
        assertNoTokenAt("shell", code, "if", "Keyword")
        assertNoTokenAt("shell", code, "${'$'}HOME", "Variable")
        assertTokenAt("shell", code, "<<'EOF'\nthen ${'$'}HOME\nEOF", "String")
        assertNoTokenAt("shell", code, "then", "Keyword")
    }
}
