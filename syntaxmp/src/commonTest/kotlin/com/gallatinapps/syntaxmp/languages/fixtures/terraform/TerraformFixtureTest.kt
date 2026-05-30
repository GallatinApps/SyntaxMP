package com.gallatinapps.syntaxmp.languages.fixtures.terraform

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class TerraformFixtureTest {
    private val code = """
        # local note artifact
        resource "local_file" "note" {
          filename = "${'$'}{path.module}/note.md"
          label    = "${'$'}{format("note-%s", local.name)}"
          content  = jsonencode({ enabled = true, count = 2 })
          lifecycle {
            prevent_destroy = false
          }
        }
    """.trimIndent()

    @Test
    fun `terraform declarations properties interpolation functions constants and numbers`() {
        assertTokenAt("terraform", code, "# local note artifact", "Comment")
        assertTokenAt("terraform", code, "resource", "Keyword")
        assertTokenAt("terraform", code, "\"local_file\"", "String")
        assertTokenAt("terraform", code, "filename", "Property")
        assertTokenAt("terraform", code, "${'$'}{", "Escape")
        assertTokenAt("terraform", code, "path", "Variable")
        assertTokenAt("terraform", code, "module", "Property")
        assertTokenAt("terraform", code, "}", "Escape", occurrence = 0)
        assertTokenAt("terraform", code, "/note.md\"", "String")
        assertTokenAt("terraform", code, "format", "Function")
        assertTokenAt("terraform", code, "\"note-%s\"", "String")
        assertTokenAt("terraform", code, "local", "Variable", occurrence = 2)
        assertTokenAt("terraform", code, "name", "Property", occurrence = 1)
        assertTokenAt("terraform", code, "jsonencode", "Function")
        assertTokenAt("terraform", code, "enabled", "Property")
        assertTokenAt("terraform", code, "true", "Constant")
        assertTokenAt("terraform", code, "2", "Number")
        assertTokenAt("terraform", code, "lifecycle", "Type")
        assertTokenAt("terraform", code, "prevent_destroy", "Property")
        assertTokenAt("terraform", code, "false", "Constant")
    }

    @Test
    fun `terraform declaration keywords cover built in block types`() {
        val declarationCode = """
            data "aws_region" "current" {}
            module "site" {}
            variable "name" {}
            output "url" {}
            provider "aws" {}
        """.trimIndent()

        assertTokenAt("terraform", declarationCode, "data", "Keyword", "keyword")
        assertTokenAt("terraform", declarationCode, "module", "Keyword", "keyword")
        assertTokenAt("terraform", declarationCode, "variable", "Keyword", "keyword")
        assertTokenAt("terraform", declarationCode, "output", "Keyword", "keyword")
        assertTokenAt("terraform", declarationCode, "provider", "Keyword", "keyword")
    }

    @Test
    fun `comment contents are not declarations`() = assertNoTokenAt(
        language = "terraform",
        code = "# resource",
        substring = "resource",
        category = "Keyword",
    )
}
