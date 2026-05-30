package com.gallatinapps.syntaxmp.languages.fixtures.powershell

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

// PowerShell is the first SyntaxMP v1 language where we expect real-world bug reports after launch.
class PowerShellFixtureTest {
    private val code = """
        # powershell scoring
        <# block note #>
        function Invoke-Score {
          param(${'$'}Name, @Options)
          ${'$'}global:count = 1
          if (${'$'}env:USER -like "d*") {
            Write-Host "hello ${'$'}Name"
          }
          ${'$'}literal = '${'$'}Name'
          ${'$'}here = @"
        hello ${'$'}Name
        "@
          Get-Process @Options
        }
    """.trimIndent()

    @Test
    fun `powershell comments variables strings cmdlets operators and splatting`() {
        assertTokenAt("powershell", code, "# powershell scoring", "Comment")
        assertTokenAt("powershell", code, "<# block note #>", "Comment")
        assertTokenAt("powershell", code, "function", "Keyword")
        assertTokenAt("powershell", code, "param", "Keyword")
        assertTokenAt("powershell", code, "${'$'}Name", "Variable")
        assertTokenAt("powershell", code, "@Options", "Variable")
        assertTokenAt("powershell", code, "${'$'}global:count", "Variable")
        assertTokenAt("powershell", code, "if", "Keyword")
        assertTokenAt("powershell", code, "${'$'}env:USER", "Variable")
        assertTokenAt("powershell", code, "-like", "Operator")
        assertTokenAt("powershell", code, "Write-Host", "Function")
        assertTokenAt("powershell", code, "${'$'}Name", "Variable", occurrence = 1)
        assertTokenAt("powershell", code, "'${'$'}Name'", "String")
        assertTokenAt("powershell", code, "@\"\nhello ", "String")
        assertTokenAt("powershell", code, "${'$'}Name", "Variable", occurrence = 3)
        assertTokenAt("powershell", code, "Get-Process", "Function")
        assertTokenAt("powershell", code, "@Options", "Variable", occurrence = 1)
    }

    @Test
    fun `single quoted variables are not interpolated`() = assertNoTokenAt(
        language = "powershell",
        code = "'${'$'}Name'",
        substring = "${'$'}Name",
        category = "Variable",
    )

    @Test
    fun `expandable strings and here strings tokenize braced variables and subexpressions`() {
        val code = """
            ${'$'}text = "hello ${'$'}{Name} ${'$'}(Get-Process)"
            ${'$'}here = @"
            value ${'$'}{Name} ${'$'}(Get-Date)
            "@
            ${'$'}literal = @'
            class ${'$'}Name
            '@
        """.trimIndent()

        assertTokenAt("powershell", code, "${'$'}{Name}", "Variable")
        assertTokenAt("powershell", code, "${'$'}(", "Escape")
        assertTokenAt("powershell", code, "Get-Process", "Function")
        assertTokenAt("powershell", code, "@\"\nvalue ", "String")
        assertTokenAt("powershell", code, "${'$'}{Name}", "Variable", occurrence = 1)
        assertTokenAt("powershell", code, "${'$'}(", "Escape", occurrence = 1)
        assertNoTokenAt("powershell", code, "class", "Keyword")
        assertNoTokenAt("powershell", code, "${'$'}Name", "Variable")
    }

    @Test
    fun `command parameters and quoted comments stay boundary scoped`() {
        val code = """
            Get-Process -Name ${'$'}HOME -Verbose
            Write-Host "# still string" # real comment
        """.trimIndent()

        assertTokenAt("powershell", code, "Get-Process", "Function")
        assertTokenAt("powershell", code, "-Name", "Variable")
        assertTokenAt("powershell", code, "${'$'}HOME", "Variable")
        assertTokenAt("powershell", code, "-Verbose", "Variable")
        assertNoTokenAt("powershell", code, "# still string", "Comment")
        assertTokenAt("powershell", code, "# real comment", "Comment")
    }
}
