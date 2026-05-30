package com.gallatinapps.syntaxmp.languages.fixtures.less

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class LessFixtureTest {
    private val code = """
        /* less scoring */
        @brand: #38a3ff;
        .rounded() {
          border-radius: 4px;
        }
        .card {
          color: @brand;
          .rounded();
        }
    """.trimIndent()

    @Test
    fun `less variables mixins selectors properties and numbers`() {
        assertTokenAt("less", code, "/* less scoring */", "Comment")
        assertTokenAt("less", code, "@brand", "Variable")
        assertTokenAt("less", code, ".rounded", "Attribute")
        assertTokenAt("less", code, "border-radius", "Property")
        assertTokenAt("less", code, "4px", "Number")
        assertTokenAt("less", code, ".card", "Attribute")
        assertTokenAt("less", code, "color", "Property")
        assertTokenAt("less", code, "@brand", "Variable", occurrence = 1)
    }

    @Test
    fun `css at rules are not less variables`() = assertNoTokenAt(
        language = "less",
        code = "@media (min-width: 30rem) {}",
        substring = "@media",
        category = "Variable",
    )

    @Test
    fun `less variables in values stay variables while css at rules stay keywords`() {
        val code = """
            @media (min-width: 30rem) {
              .card { color: @brand; }
            }
        """.trimIndent()

        assertTokenAt("less", code, "@media", "Keyword")
        assertTokenAt("less", code, "@brand", "Variable")
    }

    @Test
    fun `less nested selectors and string comment boundaries stay scoped`() {
        val code = """
            @brand: #fff;
            .card {
              &.active, .child:hover { color: @brand; }
              content: ".fake { color: @brand; }";
              /* .fake { color: @brand; } */
            }
        """.trimIndent()

        assertTokenAt("less", code, "@brand", "Variable")
        assertTokenAt("less", code, ".card", "Attribute")
        assertTokenAt("less", code, ".active", "Attribute")
        assertTokenAt("less", code, ".child", "Attribute")
        assertTokenAt("less", code, "hover", "Attribute", "attribute.pseudo")
        assertTokenAt("less", code, "color", "Property")
        assertTokenAt("less", code, "@brand", "Variable", occurrence = 1)
        assertTokenAt("less", code, "content", "Property")
        assertTokenAt("less", code, "\".fake { color: @brand; }\"", "String")
        assertTokenAt("less", code, "/* .fake { color: @brand; } */", "Comment")
        assertNoTokenAt("less", code, ".fake", "Attribute")
        assertNoTokenAt("less", code, "@brand", "Variable", occurrence = 2)
        assertNoTokenAt("less", code, ".fake", "Attribute", occurrence = 1)
        assertNoTokenAt("less", code, "@brand", "Variable", occurrence = 3)
    }
}
