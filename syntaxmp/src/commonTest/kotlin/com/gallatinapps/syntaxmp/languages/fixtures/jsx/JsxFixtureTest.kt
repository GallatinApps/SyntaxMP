package com.gallatinapps.syntaxmp.languages.fixtures.jsx

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class JsxFixtureTest {
    private val code = """
        import React from "react";

        export function Card({ title }) {
          const count = 1;
          return <section className="card" data-count={count}><h1>{title}</h1></section>;
        }
    """.trimIndent()

    @Test
    fun `jsx script markup attributes and expressions`() {
        assertTokenAt("jsx", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("jsx", code, "React", "Type")
        assertTokenAt("jsx", code, "\"react\"", "String")
        assertTokenAt("jsx", code, "export", "Keyword", "keyword.declaration")
        assertTokenAt("jsx", code, "Card", "Function")
        assertTokenAt("jsx", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("jsx", code, "1", "Number")
        assertTokenAt("jsx", code, "section", "Tag")
        assertTokenAt("jsx", code, "className", "Attribute")
        assertTokenAt("jsx", code, "\"card\"", "String")
        assertTokenAt("jsx", code, "data-count", "Attribute")
        assertTokenAt("jsx", code, "count", "Variable", occurrence = 2)
        assertTokenAt("jsx", code, "h1", "Tag")
        assertTokenAt("jsx", code, "title", "Variable", occurrence = 1)
    }

    @Test
    fun `jsx entities tokenize only in markup text and attributes`() {
        val code = """
            const escaped = "&amp;";
            export const View = () => <p title="A &amp; B">Fish &amp; Chips</p>;
        """.trimIndent()

        assertNoTokenAt("jsx", code, "&amp;", "Escape")
        assertTokenAt("jsx", code, "&amp;", "Escape", occurrence = 1)
        assertTokenAt("jsx", code, "&amp;", "Escape", occurrence = 2)
    }

    @Test
    fun `jsx mixed script fragments markup expressions and entities`() {
        val code = """
            const label = `Hello ${'$'}{user.name}`;
            export const View = ({ user }) => (
              <>
                <Card title={label} data-copy="A &amp; B">
                  {user.enabled ? label : null}
                </Card>
              </>
            );
        """.trimIndent()

        assertTokenAt("jsx", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("jsx", code, "label", "Variable")
        assertTokenAt("jsx", code, "`Hello ", "String")
        assertTokenAt("jsx", code, "${'$'}{", "Escape")
        assertTokenAt("jsx", code, "user", "Variable")
        assertTokenAt("jsx", code, "name", "Property")
        assertTokenAt("jsx", code, "View", "Type")
        assertTokenAt("jsx", code, "Card", "Tag")
        assertTokenAt("jsx", code, "title", "Attribute")
        assertTokenAt("jsx", code, "label", "Variable", occurrence = 1)
        assertTokenAt("jsx", code, "data-copy", "Attribute")
        assertTokenAt("jsx", code, "&amp;", "Escape")
        assertTokenAt("jsx", code, "enabled", "Property")
        assertTokenAt("jsx", code, "null", "Constant")
        assertTokenAt("jsx", code, "</", "Punctuation")
    }

    @Test
    fun `jsx fragments event handlers expression props and property access stay in markup boundary`() {
        val code = """
            const view = (
              <>
                <PanelView onClick={() => handle(user.enabled)} data-copy="A &amp; B" />
              </>
            );
        """.trimIndent()

        assertTokenAt("jsx", code, "<>", "Punctuation")
        assertTokenAt("jsx", code, "PanelView", "Tag")
        assertTokenAt("jsx", code, "onClick", "Attribute")
        assertTokenAt("jsx", code, "{", "Punctuation")
        assertTokenAt("jsx", code, "handle", "Function")
        assertTokenAt("jsx", code, "user", "Variable")
        assertTokenAt("jsx", code, "enabled", "Property")
        assertTokenAt("jsx", code, "data-copy", "Attribute")
        assertTokenAt("jsx", code, "&amp;", "Escape")
        assertTokenAt("jsx", code, "</>", "Punctuation")
    }

    @Test
    fun `jsx raw text closes and markdown looking text stays markup text`() {
        val code = """
            const view = (
              <>
                <script>const inside = 1;</script>
                <p># not markdown **strong** &amp;</p>
              </>
            );
            const after = 2;
        """.trimIndent()

        assertTokenAt("jsx", code, "inside", "Variable")
        assertTokenAt("jsx", code, "p", "Tag", occurrence = 2)
        assertTokenAt("jsx", code, "&amp;", "Escape")
        assertTokenAt("jsx", code, "after", "Variable")
        assertNoTokenAt("jsx", code, "#", "Markup")
        assertNoTokenAt("jsx", code, "**", "Markup")
    }
}
