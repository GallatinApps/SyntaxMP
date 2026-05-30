package com.gallatinapps.syntaxmp.languages.fixtures.makefile

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class MakefileFixtureTest {
    private val code = listOf(
        "# Build shortcuts",
        "OUT := build/app",
        "export APP_ENV := dev",
        "include config.mk",
        "",
        "build: ${'$'}(OUT)",
        "\tgradle :sharedApp:compileKotlinJvm",
        "",
        "${'$'}(OUT):",
        "\tmkdir -p ${'$'}@",
    ).joinToString("\n")

    @Test
    fun `makefile comments directives targets variables and recipes`() {
        assertTokenAt("makefile", code, "# Build shortcuts", "Comment")
        assertTokenAt("makefile", code, "OUT", "Property")
        assertTokenAt("makefile", code, ":=", "Operator")
        assertTokenAt("makefile", code, "export", "Keyword")
        assertTokenAt("makefile", code, "include", "Keyword")
        assertTokenAt("makefile", code, "build", "Function", occurrence = 1)
        assertTokenAt("makefile", code, "${'$'}(OUT)", "Variable")
        assertTokenAt("makefile", code, "gradle", "Function")
        assertTokenAt("makefile", code, "mkdir", "Function")
        assertTokenAt("makefile", code, "${'$'}", "Variable", occurrence = 2)
    }

    @Test
    fun `recipe command names are not targets`() = assertNoTokenAt(
        language = "makefile",
        code = code,
        substring = "gradle",
        category = "Property",
    )

    @Test
    fun `recipe strings comments variables and silent prefix keep line boundaries`() {
        val code = listOf(
            "OUT := build/app",
            "run: ${'$'}(OUT)",
            "\tprintf \"target: # not comment\" ${'$'}(OUT)",
            "\t@echo done # real comment",
        ).joinToString("\n")

        assertTokenAt("makefile", code, "OUT", "Property")
        assertTokenAt("makefile", code, "run", "Function")
        assertTokenAt("makefile", code, "${'$'}(OUT)", "Variable")
        assertTokenAt("makefile", code, "printf", "Function")
        assertTokenAt("makefile", code, "\"target: # not comment\"", "String")
        assertNoTokenAt("makefile", code, "# not comment", "Comment")
        assertTokenAt("makefile", code, "${'$'}(OUT)", "Variable", occurrence = 1)
        assertTokenAt("makefile", code, "@", "Operator")
        assertTokenAt("makefile", code, "echo", "Function")
        assertTokenAt("makefile", code, "# real comment", "Comment")
    }
}
