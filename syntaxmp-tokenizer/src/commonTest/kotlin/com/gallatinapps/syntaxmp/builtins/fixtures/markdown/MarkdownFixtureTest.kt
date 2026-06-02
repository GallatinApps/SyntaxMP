package com.gallatinapps.syntaxmp.builtins.fixtures.markdown

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class MarkdownFixtureTest {
    private val code = """
        # Heading

        > Quote with [link](https://example.com)
        - item with **strong** text and `inlineCode`
        1. numbered item
        ![alt](image.png)

        ```kotlin
        val nested = "base only"
        ```
    """.trimIndent()

    @Test
    fun `markdown structural markers and fence language`() {
        assertTokenAt("markdown", code, "#", "Markup")
        assertTokenAt("markdown", code, ">", "Markup")
        assertTokenAt("markdown", code, "[", "Markup")
        assertTokenAt("markdown", code, "](", "Markup")
        assertTokenAt("markdown", code, "-", "Markup")
        assertTokenAt("markdown", code, "**", "Markup")
        assertTokenAt("markdown", code, "`", "Markup")
        assertTokenAt("markdown", code, "1.", "Markup")
        assertTokenAt("markdown", code, "![", "Markup")
        assertTokenAt("markdown", code, "kotlin", "String")
    }

    @Test
    fun `fenced code body is tokenized as kotlin`() {
        assertTokenAt("markdown", code, "val", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "nested", "Variable")
    }

    @Test
    fun `fenced code info string uses first token as language`() {
        val code = """
            ```kotlin title="x"
            fun x() = 1
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "fun", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "x", "Function", occurrence = 1)
        assertTokenAt("markdown", code, "1", "Number")
    }

    @Test
    fun `class-style fenced code info string selects language class`() {
        val code = """
            ```{#sample .kotlin .numberLines}
            fun x() = 1
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "fun", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "x", "Function")
    }

    @Test
    fun `fenced code language aliases route through the engine`() {
        val code = """
            ```kt
            fun x() = 1
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "kt", "String", "string.language")
        assertTokenAt("markdown", code, "fun", "Keyword", "keyword.declaration")
    }

    @Test
    fun `fenced code can delegate to extension languages`() {
        val childLanguage = LanguageId.fromString("mydsl")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = childLanguage,
                    tokenizer = LanguageTokenizer { request ->
                        val start = request.code.indexOf("special")

                            listOf(
                                SyntaxTokenSpan(
                                    start = start,
                                    endExclusive = start + "special".length,
                                    role = SyntaxRole.Function,
                                    languageId = request.languageId,
                                ),
                            )
                    },
                ),
            ),
        )
        val code = """
            ```mydsl
            special value
            ```
        """.trimIndent()

        assertTokenAt(
            language = "markdown",
            code = code,
            substring = "special",
            category = "Function",
            engine = engine,
        )
    }

    @Test
    fun `fenced code without language does not tokenize body as an embedded language`() = assertNoTokenAt(
        language = "markdown",
        code = """
            ```
            val plain = 1
            ```
        """.trimIndent(),
        substring = "val",
        category = "Keyword",
    )

    @Test
    fun `markdown markers inside code spans and unlabeled fences stay plain`() {
        val code = """
            `# not heading *not emphasis* [not](link)`

            ```
            # not heading
            - not list
            [not](link)
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "`", "Markup", "markup.code")
        assertTokenAt("markdown", code, "`", "Markup", "markup.code", occurrence = 1)
        assertTokenAt("markdown", code, "```", "Markup", "markup.fence")
        assertTokenAt("markdown", code, "```", "Markup", "markup.fence", occurrence = 1)
        assertNoTokenAt("markdown", code, "#", "Markup")
        assertNoTokenAt("markdown", code, "*", "Markup")
        assertNoTokenAt("markdown", code, "[", "Markup")
        assertNoTokenAt("markdown", code, "#", "Markup", occurrence = 1)
        assertNoTokenAt("markdown", code, "-", "Markup")
        assertNoTokenAt("markdown", code, "[", "Markup", occurrence = 1)
    }

    @Test
    fun `unknown fenced code language keeps label but emits no body spans`() {
        val code = """
            ```madeuplang
            stuff
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "madeuplang", "String", "string.language")
        assertNoTokenAt("markdown", code, "stuff", "Variable")
    }

    @Test
    fun `tilde fences delegate to embedded language`() {
        val code = """
            ~~~js
            let x = 1;
            ~~~
        """.trimIndent()

        assertTokenAt("markdown", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "x", "Variable")
    }

    @Test
    fun `longer closing fence limits embedded language body`() {
        val code = """
            ```js
            let inside = 1;
            ````
            let outside = 2;
        """.trimIndent()

        assertTokenAt("markdown", code, "let", "Keyword", "keyword.declaration")
        assertNoTokenAt("markdown", code, "let", "Keyword", occurrence = 1)
    }

    @Test
    fun `unterminated fenced block delegates body through eof`() {
        val code = """
            ```js
            let x = 1;
        """.trimIndent()

        assertTokenAt("markdown", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "1", "Number")
    }

    @Test
    fun `fenced markdown content ignores same length nested fence info lines until bare closing fence`() {
        val code = """
            ```markdown
            ```kotlin
            val nested = 1
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "markdown", "String", "string.language")
        assertTokenAt("markdown", code, "kotlin", "String", "string.language")
        assertTokenAt("markdown", code, "val", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "nested", "Variable")
    }

    @Test
    fun `nested markdown html script delegation stays within depth limit`() {
        val code = """
            ```html
            <script>const answer = 42;</script>
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "script", "Tag")
        assertTokenAt("markdown", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "42", "Number")
    }

    @Test
    fun `inline html is tokenized as html`() {
        val code = "Some text <strong>bold</strong> here"

        assertTokenAt("markdown", code, "strong", "Tag")
        assertTokenAt("markdown", code, "strong", "Tag", occurrence = 1)
    }

    @Test
    fun `block html is tokenized as html`() {
        val code = """<div class="note">Hello <span>world</span></div>"""

        assertTokenAt("markdown", code, "div", "Tag")
        assertTokenAt("markdown", code, "class", "Attribute")
        assertTokenAt("markdown", code, "\"note\"", "String")
        assertTokenAt("markdown", code, "span", "Tag")
    }

    @Test
    fun `setext headings and thematic breaks are highlighted`() {
        val code = """
            Title
            ===

            ---
        """.trimIndent()

        assertTokenAt("markdown", code, "===", "Markup", "markup.heading.h1")
        assertTokenAt("markdown", code, "---", "Markup", "markup.thematic-break")
    }

    @Test
    fun `atx headings emit level-specific roles`() {
        val code = """
            # H1
            ## H2
            ### H3
            #### H4
            ##### H5
            ###### H6
        """.trimIndent()

        assertTokenAt("markdown", code, "#", "Markup", "markup.heading.h1")
        assertTokenAt("markdown", code, "##", "Markup", "markup.heading.h2")
        assertTokenAt("markdown", code, "###", "Markup", "markup.heading.h3")
        assertTokenAt("markdown", code, "####", "Markup", "markup.heading.h4")
        assertTokenAt("markdown", code, "#####", "Markup", "markup.heading.h5")
        assertTokenAt("markdown", code, "######", "Markup", "markup.heading.h6")
    }

    @Test
    fun `setext headings emit level-specific roles`() {
        val code = """
            Heading level 1
            ===============

            Heading level 2
            ---------------
        """.trimIndent()

        assertTokenAt("markdown", code, "===============", "Markup", "markup.heading.h1")
        assertTokenAt("markdown", code, "---------------", "Markup", "markup.heading.h2")
    }

    @Test
    fun `markdown escapes and entities are highlighted`() {
        val code = """Escaped \* and &amp; and &#123;"""

        assertTokenAt("markdown", code, """\*""", "Escape")
        assertTokenAt("markdown", code, "&amp;", "Escape")
        assertTokenAt("markdown", code, "&#123;", "Escape")
    }

    @Test
    fun `link reference definitions highlight label destination and title`() {
        val code = "[docs]: https://example.com \"Docs\""

        assertTokenAt("markdown", code, "[docs]:", "Markup", "markup.reference")
        assertTokenAt("markdown", code, "https://example.com", "String", "string.url")
        assertTokenAt("markdown", code, "\"Docs\"", "String")
    }

    @Test
    fun `gfm task list markers are highlighted`() {
        val code = """
            - [x] done
            - [X] also done
            - [ ] todo
        """.trimIndent()

        assertTokenAt("markdown", code, "[x]", "Markup", "markup.task.checked")
        assertTokenAt("markdown", code, "[X]", "Markup", "markup.task.checked")
        assertTokenAt("markdown", code, "[ ]", "Markup", "markup.task.unchecked")
    }

    @Test
    fun `gfm strikethrough markers are highlighted`() {
        val code = """~~gone~~ and ~~done~~"""

        assertTokenAt("markdown", code, "~~", "Markup", "markup.strikethrough")
        assertTokenAt("markdown", code, "~~", "Markup", "markup.strikethrough", occurrence = 1)
        assertTokenAt("markdown", code, "~~", "Markup", "markup.strikethrough", occurrence = 2)
        assertTokenAt("markdown", code, "~~", "Markup", "markup.strikethrough", occurrence = 3)
    }

    @Test
    fun `gfm table pipes and delimiter row are highlighted`() {
        val code = """
            | Name | Done |
            | ---: | :--- |
            | A | yes |
        """.trimIndent()

        assertTokenAt("markdown", code, "|", "Markup", "markup.table")
        assertTokenAt("markdown", code, "---:", "Markup", "markup.table")
        assertTokenAt("markdown", code, ":---", "Markup", "markup.table")
        assertTokenAt("markdown", code, "|", "Markup", "markup.table", occurrence = 8)
    }

    @Test
    fun `multiline block html delegates nested script body`() {
        val code = """
            <body>
            <main>
            <h1>Title</h1>
            <p>Copy</p>
            <script>
            const message = "hello";
            </script>
            </main>
            </body>
        """.trimIndent()

        assertTokenAt("markdown", code, "body", "Tag")
        assertTokenAt("markdown", code, "main", "Tag")
        assertTokenAt("markdown", code, "script", "Tag")
        assertTokenAt("markdown", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("markdown", code, "message", "Variable")
        assertTokenAt("markdown", code, "\"hello\"", "String")
        assertTokenAt("markdown", code, "script", "Tag", occurrence = 1)
        assertTokenAt("markdown", code, "main", "Tag", occurrence = 1)
        assertTokenAt("markdown", code, "body", "Tag", occurrence = 1)
    }

    @Test
    fun `normal html block ends at blank line and resumes markdown`() {
        val code = """
            <div>
            *raw*

            *markdown*

            </div>
        """.trimIndent()

        assertTokenAt("markdown", code, "div", "Tag")
        assertNoTokenAt("markdown", code, "*", "Markup")
        assertTokenAt("markdown", code, "*", "Markup", occurrence = 2)
        assertTokenAt("markdown", code, "div", "Tag", occurrence = 1)
    }

    @Test
    fun `script html block continues through blank lines until closing tag`() {
        val code = """
            <script>
            const first = 1;

            const second = 2;
            </script>
        """.trimIndent()

        assertTokenAt("markdown", code, "script", "Tag")
        assertTokenAt("markdown", code, "first", "Variable")
        assertTokenAt("markdown", code, "second", "Variable")
        assertTokenAt("markdown", code, "script", "Tag", occurrence = 1)
    }

    @Test
    fun `indented code html is not delegated to html`() = assertNoTokenAt(
        language = "markdown",
        code = "    <div>",
        substring = "div",
        category = "Tag",
    )

    @Test
    fun `complete tag block does not interrupt paragraph`() {
        val code = """
            paragraph
            <a href="https://example.com">
            *markdown*
        """.trimIndent()

        assertTokenAt("markdown", code, "href", "Attribute")
        assertTokenAt("markdown", code, "*", "Markup")
    }

    @Test
    fun `html inside inline code is not tokenized as html`() {
        val code = "`<div>` here"

        assertTokenAt("markdown", code, "`", "Markup")
        assertNoTokenAt("markdown", code, "div", "Tag")
    }

    @Test
    fun `html inside fenced code is tokenized through fence delegation`() {
        val code = """
            ```html
            <div class="note">Inside</div>
            ```
        """.trimIndent()

        assertTokenAt("markdown", code, "html", "String", "string.language")
        assertTokenAt("markdown", code, "div", "Tag")
        assertTokenAt("markdown", code, "class", "Attribute")
    }

    @Test
    fun `inline code html stays plain before real html block`() {
        val code = """
            `escaped <tag>`

            <div class="note">Body</div>
        """.trimIndent()

        assertNoTokenAt("markdown", code, "tag", "Tag")
        assertTokenAt("markdown", code, "div", "Tag")
        assertTokenAt("markdown", code, "class", "Attribute")
    }

    @Test
    fun `sample markdown scanner coverage block highlights expected scopes`() {
        val code = """
            # ATX Heading Level 1
            ### ATX Heading Level 3

            Setext Heading Level 1
            ======================

            Setext Heading Level 2
            ----------------------

            Paragraph text with **bold**, _italic_, ***strong emphasis***, ~~strikethrough~~,
            inline `code`, a [direct link](https://example.test/docs), an image ![Alt text](image.png),
            a reference link [NoteKit][notekit], and escaped punctuation \* \[ \] \( \) \#.

            HTML entities should highlight too: &amp; &#169; &#x1F4DD; &unknown;

            [notekit]: https://example.test/notekit "Reference title"

            ---
            * * *
            _ _ _

            > Blockquote marker with **inline emphasis** and `inline code`.

            - Unordered list item
            - [ ] Open task marker
            - [x] Completed task marker
            - [X] Uppercase completed task marker
            1. Ordered list item
            2. Ordered item with [nested link](https://example.test/nested)

            | Feature | CommonMark/GFM marker | Highlight scope |
            | :------ | :-------------------- | --------------: |
            | Table pipes | `|` | markup.table |
            | Alignment row | `:---` and `---:` | markup.table |
            | Task list | `[x]` | markup.task.checked |
        """.trimIndent()

        assertTokenAt("markdown", code, "#", "Markup", "markup.heading.h1")
        assertTokenAt("markdown", code, "======================", "Markup", "markup.heading.h1")
        assertTokenAt("markdown", code, "----------------------", "Markup", "markup.heading.h2")
        assertTokenAt("markdown", code, "~~", "Markup", "markup.strikethrough")
        assertTokenAt("markdown", code, """\*""", "Escape")
        assertTokenAt("markdown", code, "&amp;", "Escape")
        assertTokenAt("markdown", code, "[notekit]:", "Markup", "markup.reference")
        assertTokenAt("markdown", code, "https://example.test/notekit", "String", "string.url")
        assertTokenAt("markdown", code, "* * *", "Markup", "markup.thematic-break")
        assertTokenAt("markdown", code, ">", "Markup", "markup.quote")
        assertTokenAt("markdown", code, "[ ]", "Markup", "markup.task.unchecked")
        assertTokenAt("markdown", code, "[x]", "Markup", "markup.task.checked")
        assertTokenAt("markdown", code, "[X]", "Markup", "markup.task.checked")
        assertTokenAt("markdown", code, "|", "Markup", "markup.table")
        assertTokenAt("markdown", code, ":------", "Markup", "markup.table")
        assertTokenAt("markdown", code, "--------------:", "Markup", "markup.table")
    }

    @Test
    fun `indented code blocks are not delegated to embedded language`() = assertNoTokenAt(
        language = "markdown",
        code = "    let x = 1",
        substring = "let",
        category = "Keyword",
    )
}
