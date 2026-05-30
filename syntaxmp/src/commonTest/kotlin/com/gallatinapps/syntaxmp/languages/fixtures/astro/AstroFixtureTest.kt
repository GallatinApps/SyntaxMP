package com.gallatinapps.syntaxmp.languages.fixtures.astro

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class AstroFixtureTest {
    private val code = """
        ---
        const title: string = "Hello";
        ---
        <section class="card">
          <h1>{title}</h1>
          <script>const answer = 42;</script>
        </section>
    """.trimIndent()

    @Test
    fun `astro frontmatter markup expressions and script`() {
        assertTokenAt("astro", code, "---", "Markup", "markup.frontmatter")
        assertTokenAt("astro", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("astro", code, "title", "Variable")
        assertTokenAt("astro", code, "string", "Variable")
        assertTokenAt("astro", code, "\"Hello\"", "String")
        assertTokenAt("astro", code, "section", "Tag")
        assertTokenAt("astro", code, "class", "Attribute")
        assertTokenAt("astro", code, "\"card\"", "String")
        assertTokenAt("astro", code, "h1", "Tag")
        assertTokenAt("astro", code, "title", "Variable", occurrence = 1)
        assertTokenAt("astro", code, "script", "Tag")
        assertTokenAt("astro", code, "const", "Keyword", "keyword.declaration", occurrence = 1)
        assertTokenAt("astro", code, "answer", "Variable")
        assertTokenAt("astro", code, "42", "Number")
    }

    @Test
    fun `astro mixed frontmatter component markup expressions entities script and style`() {
        val code = """
            ---
            const title: string = `Hello ${'$'}{name}`;
            ---
            <Layout title={title} data-copy="A &amp; B">
              <Card>{title}</Card>
              <script>const message = `Count ${'$'}{title}`;</script>
              <style lang="scss">.card-#{${'$'}state} { color: red; }</style>
            </Layout>
        """.trimIndent()

        assertTokenAt("astro", code, "---", "Markup", "markup.frontmatter")
        assertTokenAt("astro", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("astro", code, "title", "Variable")
        assertTokenAt("astro", code, "string", "Variable")
        assertTokenAt("astro", code, "`Hello ", "String")
        assertTokenAt("astro", code, "${'$'}{", "Escape")
        assertTokenAt("astro", code, "name", "Variable")
        assertTokenAt("astro", code, "Layout", "Tag")
        assertTokenAt("astro", code, "title", "Attribute", occurrence = 1)
        assertTokenAt("astro", code, "title", "Variable", occurrence = 2)
        assertTokenAt("astro", code, "data-copy", "Attribute")
        assertTokenAt("astro", code, "&amp;", "Escape")
        assertTokenAt("astro", code, "Card", "Tag")
        assertTokenAt("astro", code, "title", "Variable", occurrence = 3)
        assertTokenAt("astro", code, "script", "Tag")
        assertTokenAt("astro", code, "message", "Variable")
        assertTokenAt("astro", code, "`Count ", "String")
        assertTokenAt("astro", code, "${'$'}{", "Escape", occurrence = 1)
        assertTokenAt("astro", code, "style", "Tag")
        assertTokenAt("astro", code, "\"scss\"", "String")
        assertTokenAt("astro", code, "#{", "Escape")
        assertTokenAt("astro", code, "${'$'}state", "Variable")
    }

    @Test
    fun `astro ts frontmatter ts script plain script and style keep boundary roles`() {
        val code = """
            ---
            interface Props { title: string }
            const title = "Hello";
            ---
            <Layout title={title}>
              <script lang="ts">
              interface Client { id: string }
              const client: Client = { id: "1" };
              </script>
              <script>const plain = 1;</script>
              <style>.card { color: red; }</style>
            </Layout>
        """.trimIndent()

        assertTokenAt("astro", code, "---", "Markup", "markup.frontmatter")
        assertTokenAt("astro", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("astro", code, "Props", "Type")
        assertTokenAt("astro", code, "Layout", "Tag")
        assertTokenAt("astro", code, "title", "Attribute", occurrence = 2)
        assertTokenAt("astro", code, "title", "Variable", occurrence = 3)
        assertTokenAt("astro", code, "script", "Tag")
        assertTokenAt("astro", code, "\"ts\"", "String")
        assertTokenAt("astro", code, "interface", "Keyword", "keyword.declaration", occurrence = 1)
        assertTokenAt("astro", code, "Client", "Type")
        assertTokenAt("astro", code, "plain", "Variable")
        assertTokenAt("astro", code, "style", "Tag")
        assertTokenAt("astro", code, "color", "Property")
    }

    @Test
    fun `astro raw text boundaries and routing do not leak into markup text`() {
        val code = """
            <Layout>
              <script>const plain = 1;</script>
              <script lang="ts">interface Client { id: string }</script>
              <style>.plain { color: red; }</style>
              <style lang="scss">${'$'}accent: red; .typed { color: ${'$'}accent; }</style>
              <p>const outside = #not-markdown</p>
            </Layout>
        """.trimIndent()

        assertTokenAt("astro", code, "Layout", "Tag")
        assertTokenAt("astro", code, "plain", "Variable")
        assertTokenAt("astro", code, "\"ts\"", "String")
        assertTokenAt("astro", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("astro", code, "Client", "Type")
        assertTokenAt("astro", code, ".plain", "Attribute")
        assertTokenAt("astro", code, "color", "Property")
        assertTokenAt("astro", code, "\"scss\"", "String")
        assertTokenAt("astro", code, "${'$'}accent", "Variable")
        assertTokenAt("astro", code, "${'$'}accent", "Variable", occurrence = 1)
        assertNoTokenAt("astro", code, "outside", "Variable")
        assertNoTokenAt("astro", code, "#", "Markup")
    }

    @Test
    fun `astro directive attributes use directive scope`() {
        val code = """
            ---
            const content = "<strong>Hello</strong>";
            ---
            <Counter client:load set:html={content} is:raw data-id="counter" />
        """.trimIndent()

        assertTokenAt("astro", code, "client:load", "Attribute", "attribute.directive")
        assertTokenAt("astro", code, "set:html", "Attribute", "attribute.directive")
        assertTokenAt("astro", code, "is:raw", "Attribute", "attribute.directive")
        assertTokenAt("astro", code, "data-id", "Attribute")
        assertNoTokenAt("astro", code, "data-id", "Attribute", "attribute.directive")
        assertTokenAt("astro", code, "content", "Variable", occurrence = 1)
    }

    @Test
    fun `astro directive-looking text in comments and raw text is not an attribute`() {
        val code = """
            <!-- client:load set:html -->
            <script>const note = "client:load";</script>
        """.trimIndent()

        assertTokenAt("astro", code, "<!-- client:load set:html -->", "Comment")
        assertTokenAt("astro", code, "\"client:load\"", "String")
        assertNoTokenAt("astro", code, "client:load", "Attribute", "attribute.directive")
        assertNoTokenAt("astro", code, "set:html", "Attribute", "attribute.directive")
        assertNoTokenAt("astro", code, "client:load", "Attribute", "attribute.directive", occurrence = 1)
    }
}
