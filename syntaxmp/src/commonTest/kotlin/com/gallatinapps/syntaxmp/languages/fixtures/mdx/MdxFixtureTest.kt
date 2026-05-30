package com.gallatinapps.syntaxmp.languages.fixtures.mdx

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class MdxFixtureTest {
    private val code = """
        import { Chart } from "./Chart";

        # Metrics

        <Chart title="Daily" value={count} />

        ```ts
        const sample: number = 1;
        ```
    """.trimIndent()

    @Test
    fun `mdx markdown esm component markup and fenced code`() {
        assertTokenAt("mdx", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "Chart", "Type")
        assertTokenAt("mdx", code, "#", "Markup")
        assertTokenAt("mdx", code, "Chart", "Tag", occurrence = 2)
        assertTokenAt("mdx", code, "title", "Attribute")
        assertTokenAt("mdx", code, "\"Daily\"", "String")
        assertTokenAt("mdx", code, "value", "Attribute")
        assertTokenAt("mdx", code, "count", "Variable")
        assertTokenAt("mdx", code, "ts", "String", "string.language")
        assertTokenAt("mdx", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "sample", "Variable")
        assertTokenAt("mdx", code, "1", "Number")
    }

    @Test
    fun `mdx mixed markdown esm jsx entities and fenced component code`() {
        val code = """
            import Alert from "./Alert";
            export const message = `Count ${'$'}{count}`;

            ## **Status** [docs](https://example.com)

            <Alert title={message} note="Fish &amp; Chips">
              Body &amp; copy
            </Alert>

            ```tsx
            const view = <span>{message}</span>;
            ```
        """.trimIndent()

        assertTokenAt("mdx", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "Alert", "Type")
        assertTokenAt("mdx", code, "export", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "message", "Variable")
        assertTokenAt("mdx", code, "`Count ", "String")
        assertTokenAt("mdx", code, "${'$'}{", "Escape")
        assertTokenAt("mdx", code, "count", "Variable")
        assertTokenAt("mdx", code, "##", "Markup")
        assertTokenAt("mdx", code, "**", "Markup")
        assertTokenAt("mdx", code, "](", "Markup")
        assertTokenAt("mdx", code, "Alert", "Tag", occurrence = 2)
        assertTokenAt("mdx", code, "title", "Attribute")
        assertTokenAt("mdx", code, "message", "Variable", occurrence = 1)
        assertTokenAt("mdx", code, "note", "Attribute")
        assertTokenAt("mdx", code, "&amp;", "Escape")
        assertTokenAt("mdx", code, "&amp;", "Escape", occurrence = 1)
        assertTokenAt("mdx", code, "tsx", "String", "string.language")
        assertTokenAt("mdx", code, "view", "Variable")
        assertTokenAt("mdx", code, "span", "Tag")
    }

    @Test
    fun `component tag can be immediately followed by markdown paragraph`() {
        val code = """
            <Chart value={count} />
            Next paragraph with **strong** text.
        """.trimIndent()

        assertTokenAt("mdx", code, "Chart", "Tag")
        assertTokenAt("mdx", code, "value", "Attribute")
        assertTokenAt("mdx", code, "count", "Variable")
        assertTokenAt("mdx", code, "**", "Markup")
    }

    @Test
    fun `esm line between markdown paragraphs is tokenized as javascript`() {
        val code = """
            Before paragraph.
            export const answer = 42;
            After paragraph with **strong** text.
        """.trimIndent()

        assertTokenAt("mdx", code, "export", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "answer", "Variable")
        assertTokenAt("mdx", code, "42", "Number")
        assertTokenAt("mdx", code, "**", "Markup")
    }

    @Test
    fun `top level expression between paragraphs is tokenized as javascript`() {
        val code = """
            Before paragraph.

            {count + 1}

            After paragraph.
        """.trimIndent()

        assertTokenAt("mdx", code, "{", "Markup")
        assertTokenAt("mdx", code, "count", "Variable")
        assertTokenAt("mdx", code, "1", "Number")
    }

    @Test
    fun `expression inside jsx tag stays in the markup partition`() {
        val code = """<Panel value={count + 1} />"""

        assertTokenAt("mdx", code, "Panel", "Tag")
        assertTokenAt("mdx", code, "value", "Attribute")
        assertTokenAt("mdx", code, "count", "Variable")
        assertTokenAt("mdx", code, "1", "Number")
    }

    @Test
    fun `nested jsx tags keep one component partition`() {
        val code = """
            <Stack>
              <Card title={name}>Markdown content between tags</Card>
            </Stack>
        """.trimIndent()

        assertTokenAt("mdx", code, "Stack", "Tag")
        assertTokenAt("mdx", code, "Card", "Tag")
        assertTokenAt("mdx", code, "title", "Attribute")
        assertTokenAt("mdx", code, "name", "Variable")
        assertTokenAt("mdx", code, "Stack", "Tag", occurrence = 1)
    }

    @Test
    fun `html in markdown region routes through markdown embedded html`() {
        val code = """
            Before paragraph.

            <div class="note">HTML body</div>

            After paragraph.
        """.trimIndent()

        assertTokenAt("mdx", code, "div", "Tag")
        assertTokenAt("mdx", code, "class", "Attribute")
        assertTokenAt("mdx", code, "\"note\"", "String")
    }

    @Test
    fun `html looking content inside jsx uses markup scanner rules`() {
        val code = """<Panel><div on:click={handle}>Body</div></Panel>"""

        assertTokenAt("mdx", code, "Panel", "Tag")
        assertTokenAt("mdx", code, "div", "Tag")
        assertTokenAt("mdx", code, "on:click", "Attribute", "attribute.directive")
        assertTokenAt("mdx", code, "handle", "Variable")
    }

    @Test
    fun `expression props tokenize object and array bodies as javascript`() {
        val code = """
            import { Callout } from "./Callout";

            # Status

            <Callout tone="info" config={{ theme: options.theme, items: [total] }}>
              Total is {total}
            </Callout>
        """.trimIndent()

        assertTokenAt("mdx", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("mdx", code, "#", "Markup", "markup.heading.h1")
        assertTokenAt("mdx", code, "Callout", "Tag", occurrence = 2)
        assertTokenAt("mdx", code, "tone", "Attribute")
        assertTokenAt("mdx", code, "config", "Attribute")
        assertTokenAt("mdx", code, "theme", "Property")
        assertTokenAt("mdx", code, "options", "Variable")
        assertTokenAt("mdx", code, "theme", "Property", occurrence = 1)
        assertTokenAt("mdx", code, "items", "Property")
        assertTokenAt("mdx", code, "total", "Variable")
        assertTokenAt("mdx", code, "total", "Variable", occurrence = 1)
    }

    @Test
    fun `markdown markers inside jsx expression strings stay plain`() {
        val code = """<Panel label={condition ? "# not heading" : "*not emphasis*"} />"""

        assertTokenAt("mdx", code, "Panel", "Tag")
        assertTokenAt("mdx", code, "label", "Attribute")
        assertTokenAt("mdx", code, "condition", "Variable")
        assertTokenAt("mdx", code, "\"# not heading\"", "String")
        assertTokenAt("mdx", code, "\"*not emphasis*\"", "String")
        assertNoTokenAt("mdx", code, "#", "Markup")
        assertNoTokenAt("mdx", code, "*", "Markup")
    }

    @Test
    fun `mdx html raw text closes before markdown resumes`() {
        val code = """
            <script>const inside = 1;</script>

            After **markdown**
        """.trimIndent()

        assertTokenAt("mdx", code, "inside", "Variable")
        assertTokenAt("mdx", code, "**", "Markup")
    }
}
