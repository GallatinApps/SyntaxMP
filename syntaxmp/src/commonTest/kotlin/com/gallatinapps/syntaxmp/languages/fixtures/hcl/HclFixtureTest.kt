package com.gallatinapps.syntaxmp.languages.fixtures.hcl

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class HclFixtureTest {
    private val code = """
        # plain hcl
        server "web" {
          host = "localhost"
          port = 8080
          enabled = true
          resource = "plain value"
        }
    """.trimIndent()

    @Test
    fun `plain hcl properties blocks strings constants and numbers`() {
        assertTokenAt("hcl", code, "# plain hcl", "Comment")
        assertTokenAt("hcl", code, "server", "Type")
        assertTokenAt("hcl", code, "\"web\"", "String")
        assertTokenAt("hcl", code, "host", "Property")
        assertTokenAt("hcl", code, "\"localhost\"", "String")
        assertTokenAt("hcl", code, "8080", "Number")
        assertTokenAt("hcl", code, "true", "Constant")
        assertTokenAt("hcl", code, "resource", "Property")
    }

    @Test
    fun `plain hcl terraform shaped blocks are typed but not keyword declarations`() {
        val blockCode = """
            locals {
              name = "hashjot"
            }

            terraform {
              required_version = ">= 1.5"
            }
        """.trimIndent()

        assertTokenAt("hcl", blockCode, "locals", "Type")
        assertTokenAt("hcl", blockCode, "terraform", "Type")
        assertNoTokenAt("hcl", blockCode, "locals", "Keyword")
        assertNoTokenAt("hcl", blockCode, "terraform", "Keyword")
    }

    @Test
    fun `terraform declarations are not special in plain hcl`() = assertNoTokenAt(
        language = "hcl",
        code = code,
        substring = "resource",
        category = "Keyword",
    )

    @Test
    fun `hcl interpolation tokenizes expression body and inner strings`() {
        val code = """
            path = "${'$'}{var.root}/notes/${'$'}{local.name}.md"
            label = "${'$'}{foo("bar")}"
            name = upper(var.environment)
        """.trimIndent()

        assertTokenAt("hcl", code, "${'$'}{", "Escape", occurrence = 0)
        assertTokenAt("hcl", code, "var", "Variable")
        assertTokenAt("hcl", code, "root", "Property")
        assertTokenAt("hcl", code, "}", "Escape", occurrence = 0)
        assertTokenAt("hcl", code, "local", "Variable")
        assertTokenAt("hcl", code, "name", "Property")
        assertTokenAt("hcl", code, "foo", "Function")
        assertTokenAt("hcl", code, "\"bar\"", "String")
        assertTokenAt("hcl", code, "upper", "Function")
        assertTokenAt("hcl", code, "environment", "Property")
    }
}
