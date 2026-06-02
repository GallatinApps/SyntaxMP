package com.gallatinapps.syntaxmp.builtins.fixtures.html

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlFixtureTest {
    private val code = """
        <!doctype html>
        <!-- preview card -->
        <article class="note-card" data-count=4>
          <h1>NoteKit</h1>
          <a href="/docs" aria-label="Open docs">Docs</a>
          <script>const skipped = true;</script>
        </article>
    """.trimIndent()

    @Test
    fun `html tags attributes values comments and declarations`() {
        assertTokenAt("html", code, "<!doctype html>", "Annotation")
        assertTokenAt("html", code, "<!-- preview card -->", "Comment")
        assertTokenAt("html", code, "article", "Tag")
        assertTokenAt("html", code, "class", "Attribute")
        assertTokenAt("html", code, "\"note-card\"", "String")
        assertTokenAt("html", code, "data-count", "Attribute")
        assertTokenAt("html", code, "4", "String")
        assertTokenAt("html", code, "h1", "Tag")
        assertTokenAt("html", code, "href", "Attribute")
        assertTokenAt("html", code, "</", "Punctuation")
    }

    @Test
    fun `script body is tokenized as javascript`() {
        val code = "<script>let x = 1;</script>"

        assertTokenAt("html", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "x", "Variable")
        assertTokenAt("html", code, "=", "Operator")
        assertTokenAt("html", code, "1", "Number")
    }

    @Test
    fun `module script boolean attributes entities and css body keep boundary roles`() {
        val code = """
            <script type="module" defer>let ready = true;</script>
            <style>.note { color: red; }</style>
            <p>Fish &amp; Chips</p>
        """.trimIndent()

        assertTokenAt("html", code, "script", "Tag")
        assertTokenAt("html", code, "type", "Attribute")
        assertTokenAt("html", code, "\"module\"", "String")
        assertTokenAt("html", code, "defer", "Attribute")
        assertTokenAt("html", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "ready", "Variable")
        assertTokenAt("html", code, "style", "Tag")
        assertTokenAt("html", code, "color", "Property")
        assertTokenAt("html", code, "&amp;", "Escape")
    }

    @Test
    fun `style body is tokenized as css`() {
        val code = "<style>body { color: transparent; }</style>"

        assertTokenAt("html", code, "body", "Tag")
        assertTokenAt("html", code, "color", "Property")
        assertTokenAt("html", code, "transparent", "Constant", "constant.builtin.transparent")
    }

    @Test
    fun `script lang ts body is tokenized as typescript`() {
        val code = """<script lang="ts">type Props = { count: number };</script>"""

        assertTokenAt("html", code, "lang", "Attribute")
        assertTokenAt("html", code, "\"ts\"", "String")
        assertTokenAt("html", code, "type", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "Props", "Type")
        assertTokenAt("html", code, "number", "Variable")
    }

    @Test
    fun `script lang tsx body is tokenized as tsx`() {
        val code = """<script lang="tsx">const view = <Panel title="Hi" />;</script>"""

        assertTokenAt("html", code, "lang", "Attribute")
        assertTokenAt("html", code, "\"tsx\"", "String")
        assertTokenAt("html", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "view", "Variable")
        assertTokenAt("html", code, "Panel", "Tag")
        assertTokenAt("html", code, "title", "Attribute")
    }

    @Test
    fun `style lang css body is tokenized as css`() {
        val code = """<style lang="css">.card { color: red; }</style>"""

        assertTokenAt("html", code, "lang", "Attribute")
        assertTokenAt("html", code, "\"css\"", "String")
        assertTokenAt("html", code, ".card", "Attribute")
        assertTokenAt("html", code, "color", "Property")
    }

    @Test
    fun `style lang scss no longer routes to a built-in tokenizer`() {
        val code = """<style lang="scss">${'$'}primary: #fff; .card { color: ${'$'}primary; }</style>"""

        assertTokenAt("html", code, "lang", "Attribute")
        assertTokenAt("html", code, "\"scss\"", "String")
        assertNoTokenAt("html", code, "${'$'}primary", "Variable")
        assertNoTokenAt("html", code, ".card", "Attribute")
        assertNoTokenAt("html", code, "color", "Property")
    }

    @Test
    fun `raw text lang can route through extension aliases`() {
        val language = LanguageId.fromString("my-script")
        val code = """<script lang="myjs">customCall()</script>"""
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = language,
                    aliases = setOf("myjs"),
                    tokenizer = LanguageTokenizer { request ->
                        val start = request.code.indexOf("customCall")

                            listOf(
                                SyntaxTokenSpan(
                                    start = start,
                                    endExclusive = start + "customCall".length,
                                    role = SyntaxRole.Function,
                                    languageId = request.languageId,
                                ),
                            )
                    },
                ),
            ),
        )
        val start = code.indexOf("customCall")

        assertTrue(
            actual = engine.tokenize(code = code, languageLabel = LanguageId.Html.value).any { token ->
                token.start == start &&
                    token.endExclusive == start + "customCall".length &&
                    token.role == SyntaxRole.Function &&
                    token.languageId == language
            },
            message = "Expected script lang alias to route through the registered extension.",
        )
    }

    @Test
    fun `unknown raw text lang produces no embedded highlighting`() {
        val code = """
            <script lang="coffee">const value = 1;</script>
            <style lang="postcss">.card { color: red; }</style>
        """.trimIndent()

        assertNoTokenAt("html", code,"const", "Keyword")
        assertNoTokenAt("html", code,"value", "Variable")
        assertNoTokenAt("html", code,".card", "Attribute")
        assertNoTokenAt("html", code,"color", "Property")
    }

    @Test
    fun `mixed-case raw text tags delegate to embedded languages`() {
        val code = "<SCRIPT>let x = 1;</SCRIPT><STYLE>body { color: transparent; }</STYLE>"

        assertTokenAt("html", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "transparent", "Constant", "constant.builtin.transparent")
    }

    @Test
    fun `empty raw text bodies still tokenize surrounding markup`() {
        val code = "<script></script>"

        assertTokenAt("html", code, "<", "Punctuation")
        assertTokenAt("html", code, "script", "Tag")
        assertTokenAt("html", code, "script", "Tag", occurrence = 1)
    }

    @Test
    fun `unterminated script body is tokenized through eof`() {
        val code = "<script>let x = 1"

        assertTokenAt("html", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("html", code, "x", "Variable")
        assertTokenAt("html", code, "1", "Number")
    }

    @Test
    fun `inline event handler values are not delegated to javascript`() = assertNoTokenAt(
        language = "html",
        code = """<button onclick="doX()">Open</button>""",
        substring = "doX",
        category = "Function",
    )

    @Test
    fun `entities in text and attributes tokenize as escapes`() {
        val code = """<p title="Tom &amp; Jerry" data-copy=&#169;>A &amp; B &#x1F600;</p>"""

        assertTokenAt("html", code, "&amp;", "Escape")
        assertTokenAt("html", code, "&#169;", "Escape")
        assertTokenAt("html", code, "&amp;", "Escape", occurrence = 1)
        assertTokenAt("html", code, "&#x1F600;", "Escape")
    }

    @Test
    fun `entities inside comments and raw text are not markup escapes`() {
        val code = """<!-- &amp; --><script>const escaped = "&amp;";</script>"""

        assertNoTokenAt("html", code, "&amp;", "Escape")
        assertNoTokenAt("html", code, "&amp;", "Escape", occurrence = 1)
    }

    @Test
    fun `script body does not crash when javascript is disabled`() = assertNoTokenAt(
        language = "html",
        code = "<script>let x = 1;</script>",
        substring = "let",
        category = "Keyword",
        engine = SyntaxTokenizer(
            builtInLanguages = setOf(LanguageId.Html),
        ),
    )

    @Test
    fun `known raw text lang produces no embedded spans when that tokenizer is disabled`() = assertNoTokenAt(
        language = "html",
        code = """<script lang="ts">const value: number = 1;</script>""",
        substring = "const",
        category = "Keyword",
        engine = SyntaxTokenizer(
            builtInLanguages = setOf(LanguageId.Html, LanguageId.JavaScript),
        ),
    )

    @Test
    fun `raw text closing tags stop delegation before following markup text`() {
        val code = """
            <script>const inside = 1;</script>
            <p>const outside = 2;</p>
            <style>.card { color: red; }</style>
            <p>.card { color: blue; }</p>
        """.trimIndent()

        assertTokenAt("html", code, "inside", "Variable")
        assertTokenAt("html", code, "style", "Tag")
        assertTokenAt("html", code, ".card", "Attribute")
        assertTokenAt("html", code, "color", "Property")
        assertNoTokenAt("html", code, "outside", "Variable")
        assertNoTokenAt("html", code, "color", "Property", occurrence = 1)
        assertNoTokenAt("html", code, ".card", "Attribute", occurrence = 1)
    }
}
