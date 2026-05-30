package com.gallatinapps.syntaxmp.languages.fixtures.scss

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ScssFixtureTest {
    private val code = """
        /* scss scoring */
        ${'$'}primary: #38a3ff;
        @mixin button(${'$'}color) {
          &:hover {
            color: ${'$'}color;
          }
        }
        .card {
          @include button(${'$'}primary);
        }
    """.trimIndent()

    @Test
    fun `scss variables mixins nesting parent selector and properties`() {
        assertTokenAt("scss", code, "/* scss scoring */", "Comment")
        assertTokenAt("scss", code, "${'$'}primary", "Variable")
        assertTokenAt("scss", code, "@mixin", "Keyword")
        assertTokenAt("scss", code, "${'$'}color", "Variable")
        assertTokenAt("scss", code, "&", "Attribute")
        assertTokenAt("scss", code, "hover", "Attribute")
        assertTokenAt("scss", code, "color", "Property", occurrence = 1)
        assertTokenAt("scss", code, ".card", "Attribute")
        assertTokenAt("scss", code, "@include", "Keyword")
    }

    @Test
    fun `css at rules are not scss variables`() = assertNoTokenAt(
        language = "scss",
        code = "@media (min-width: 30rem) {}",
        substring = "@media",
        category = "Variable",
    )

    @Test
    fun `scss interpolation tokenizes selector value and string expressions`() {
        val code = """
            .icon-#{${'$'}name} {
              width: #{${'$'}size}px;
              content: "icon-#{${'$'}name}";
            }
        """.trimIndent()

        assertTokenAt("scss", code, "#{", "Escape")
        assertTokenAt("scss", code, "${'$'}name", "Variable")
        assertTokenAt("scss", code, "}", "Escape")
        assertTokenAt("scss", code, "#{", "Escape", occurrence = 1)
        assertTokenAt("scss", code, "${'$'}size", "Variable")
        assertTokenAt("scss", code, "#{", "Escape", occurrence = 2)
        assertTokenAt("scss", code, "${'$'}name", "Variable", occurrence = 1)
    }

    @Test
    fun `scss strings and comments do not emit variables or comments from their contents`() {
        val code = """
            ${'$'}accent: red;
            .content {
              content: "${'$'}accent /* not comment */";
              /* ${'$'}accent should stay comment text */
              color: ${'$'}accent;
            }
        """.trimIndent()

        assertTokenAt("scss", code, "${'$'}accent", "Variable")
        assertTokenAt("scss", code, "\"${'$'}accent /* not comment */\"", "String")
        assertTokenAt("scss", code, "/* ${'$'}accent should stay comment text */", "Comment")
        assertTokenAt("scss", code, "${'$'}accent", "Variable", occurrence = 3)
        assertNoTokenAt("scss", code, "${'$'}accent", "Variable", occurrence = 1)
        assertNoTokenAt("scss", code, "/* not comment */", "Comment")
        assertNoTokenAt("scss", code, "${'$'}accent", "Variable", occurrence = 2)
    }
}
