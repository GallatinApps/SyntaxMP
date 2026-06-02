package com.gallatinapps.syntaxmp.builtins.fixtures.javascript

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class JavaScriptFixtureTest {
    private val code = """
        // js scoring
        const regex = /note-\d+/g;
        async function score(item) {
          if (item.enabled === true) return console.log(`count ${'$'}{item.count}`) ?? 0;
          return undefined;
        }
    """.trimIndent()

    @Test
    fun `javascript comments keywords regex functions properties constants and template strings`() {
        assertTokenAt("javascript", code, "// js scoring", "Comment")
        assertTokenAt("javascript", code, "const", "Keyword")
        assertTokenAt("javascript", code, "/note-\\d+/g", "String")
        assertTokenAt("javascript", code, "async", "Keyword")
        assertTokenAt("javascript", code, "function", "Keyword")
        assertTokenAt("javascript", code, "score", "Function")
        assertTokenAt("javascript", code, "if", "Keyword")
        assertTokenAt("javascript", code, "enabled", "Property")
        assertTokenAt("javascript", code, "true", "Constant")
        assertTokenAt("javascript", code, "console", "Variable")
        assertTokenAt("javascript", code, "log", "Function")
        assertTokenAt("javascript", code, "`count ", "String")
        assertTokenAt("javascript", code, "${'$'}{", "Escape")
        assertTokenAt("javascript", code, "item", "Variable", occurrence = 2)
        assertTokenAt("javascript", code, "count", "Property", occurrence = 1)
        assertTokenAt("javascript", code, "undefined", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "javascript",
        code = "const label = \"function\";",
        substring = "function",
        category = "Keyword",
    )

    @Test
    fun `javascript imports classes arrows destructuring spread generators and division are covered`() {
        val code = """
            import helper, { format as fmt } from "./fmt";
            export class Job extends Base {}
            const score = async ({ count = 0, ...rest }) => await helper(rest?.name ?? "none");
            const view = { label: fmt(rest.name) };
            function* ids() { yield* [1]; }
            const divided = total / count;
            const pattern = /count\/\d+/g;
        """.trimIndent()

        assertTokenAt("javascript", code, "import", "Keyword")
        assertTokenAt("javascript", code, "from", "Keyword")
        assertTokenAt("javascript", code, "\"./fmt\"", "String")
        assertTokenAt("javascript", code, "export", "Keyword")
        assertTokenAt("javascript", code, "class", "Keyword")
        assertTokenAt("javascript", code, "Job", "Type")
        assertTokenAt("javascript", code, "extends", "Keyword")
        assertTokenAt("javascript", code, "Base", "Type")
        assertTokenAt("javascript", code, "async", "Keyword")
        assertTokenAt("javascript", code, "...", "Punctuation")
        assertTokenAt("javascript", code, "await", "Keyword")
        assertTokenAt("javascript", code, "helper", "Function", occurrence = 1)
        assertTokenAt("javascript", code, "name", "Property")
        assertTokenAt("javascript", code, "??", "Operator")
        assertTokenAt("javascript", code, "label", "Property")
        assertTokenAt("javascript", code, "fmt", "Function", occurrence = 2)
        assertTokenAt("javascript", code, "function", "Keyword")
        assertTokenAt("javascript", code, "ids", "Function")
        assertTokenAt("javascript", code, "yield", "Keyword")
        assertTokenAt("javascript", code, "/", "Operator", occurrence = 1)
        assertTokenAt("javascript", code, "/count\\/\\d+/g", "String", "string.regex")
        assertNoTokenAt("javascript", code, "/", "String", occurrence = 1)
    }
}
