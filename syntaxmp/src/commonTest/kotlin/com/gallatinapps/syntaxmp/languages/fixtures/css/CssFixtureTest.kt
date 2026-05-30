package com.gallatinapps.syntaxmp.languages.fixtures.css

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class CssFixtureTest {
    private val code = """
        /* Card shell */
        :root {
          --accent: #38a3ff;
        }

        .note-card, article[data-kind="status"] > a:hover {
          display: grid;
          grid-template-columns: minmax(0, 1fr);
          color: var(--accent);
          background: linear-gradient(180deg, #111827, transparent);
          margin: 0.5rem auto !important;
        }

        @media (max-width: 700px) {
          .note-card { display: block; }
        }
    """.trimIndent()

    @Test
    fun `css comments selectors declarations values functions and at-rules`() {
        assertTokenAt("css", code, "/* Card shell */", "Comment")
        assertTokenAt("css", code, "root", "Attribute")
        assertTokenAt("css", code, "--accent", "Property")
        assertTokenAt("css", code, "#38a3ff", "Constant")
        assertTokenAt("css", code, ".note-card", "Attribute")
        assertTokenAt("css", code, "data-kind", "Attribute")
        assertTokenAt("css", code, "\"status\"", "String")
        assertTokenAt("css", code, "display", "Property")
        assertTokenAt("css", code, "grid", "Constant")
        assertTokenAt("css", code, "minmax", "Function")
        assertTokenAt("css", code, "0.5rem", "Number")
        assertTokenAt("css", code, "!important", "Constant")
        assertTokenAt("css", code, "@media", "Keyword")
    }

    @Test
    fun `comment contents are not selectors`() = assertNoTokenAt(
        language = "css",
        code = code,
        substring = "Card",
        category = "Attribute",
    )

    @Test
    fun `css strings comments pseudos and selector functions stay scoped`() {
        val code = """
            .card::before {
              content: ".ghost { color: red; }";
            }
            /* .ghost { color: red; } */
            @supports selector(:has(.card)) {
              .card { color: blue; }
            }
        """.trimIndent()

        assertTokenAt("css", code, ".card", "Attribute")
        assertTokenAt("css", code, "before", "Attribute", "attribute.pseudo")
        assertTokenAt("css", code, "content", "Property")
        assertTokenAt("css", code, "\".ghost { color: red; }\"", "String")
        assertTokenAt("css", code, "/* .ghost { color: red; } */", "Comment")
        assertTokenAt("css", code, "@supports", "Keyword")
        assertTokenAt("css", code, "selector", "Function")
        assertTokenAt("css", code, "has", "Attribute", "attribute.pseudo")
        assertNoTokenAt("css", code, ".ghost", "Attribute")
        assertNoTokenAt("css", code, "color", "Property")
        assertNoTokenAt("css", code, ".ghost", "Attribute", occurrence = 1)
        assertNoTokenAt("css", code, "color", "Property", occurrence = 1)
    }
}
