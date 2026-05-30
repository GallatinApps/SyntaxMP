package com.gallatinapps.syntaxmp.languages.fixtures.csharp

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class CSharpFixtureTest {
    private val code = """
        // scoring model
        public class Job {
            [Obsolete]
            public int Score(string name) => Math.Max(0, name.Length);
            public bool Enabled = true;
        }
    """.trimIndent()

    @Test
    fun `csharp comments keywords annotations types functions properties and constants`() {
        assertTokenAt("csharp", code, "// scoring model", "Comment")
        assertTokenAt("csharp", code, "public", "Keyword")
        assertTokenAt("csharp", code, "class", "Keyword")
        assertTokenAt("csharp", code, "Job", "Type")
        assertTokenAt("csharp", code, "[Obsolete]", "Annotation")
        assertTokenAt("csharp", code, "int", "Type")
        assertTokenAt("csharp", code, "Score", "Function")
        assertTokenAt("csharp", code, "string", "Type")
        assertTokenAt("csharp", code, "Math", "Variable", "variable.namespace")
        assertTokenAt("csharp", code, "Max", "Function")
        assertTokenAt("csharp", code, "0", "Number")
        assertTokenAt("csharp", code, "true", "Constant")
    }

    @Test
    fun `keyword-looking text inside comments is not a keyword`() = assertNoTokenAt(
        language = "csharp",
        code = "// class",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `interpolated strings tokenize braced expressions`() {
        val code = "var label = ${'$'}\"score {job.Count}\";"

        assertTokenAt("csharp", code, "\"score ", "String")
        assertTokenAt("csharp", code, "{", "Escape")
        assertTokenAt("csharp", code, "job", "Variable")
        assertTokenAt("csharp", code, "Count", "Property")
        assertTokenAt("csharp", code, "}", "Escape")
    }

    @Test
    fun `verbatim and raw strings tokenize interpolation conservatively`() {
        val verbatim = "var path = @\"C:\\class\";"
        val interpolatedVerbatim = "var label = ${'$'}@\"score {job.Count}\";"
        val raw = "var label = ${'$'}\"\"\"score {job.Count}\"\"\"; var plain = \"\"\"class\"\"\";"

        assertTokenAt("csharp", verbatim, "@\"C:\\class\"", "String")
        assertNoTokenAt("csharp", verbatim, "class", "Keyword")
        assertTokenAt("csharp", interpolatedVerbatim, "${'$'}@\"score ", "String")
        assertTokenAt("csharp", interpolatedVerbatim, "{", "Escape")
        assertTokenAt("csharp", interpolatedVerbatim, "job", "Variable")
        assertTokenAt("csharp", interpolatedVerbatim, "Count", "Property")
        assertTokenAt("csharp", raw, "${'$'}\"\"\"score ", "String")
        assertTokenAt("csharp", raw, "{", "Escape")
        assertNoTokenAt("csharp", raw, "class", "Keyword")
    }

    @Test
    fun `bracket attributes are annotations without breaking strings`() {
        val code = """
            [Serializable]
            [JsonPropertyName("title")]
            public sealed class Note {}
        """.trimIndent()

        assertTokenAt("csharp", code, "[Serializable]", "Annotation")
        assertTokenAt("csharp", code, "[JsonPropertyName(\"title\")]", "Annotation")
        assertNoTokenAt("csharp", code, "\"title\"", "String")
    }

    @Test
    fun `nullable coalescing and indexing do not become bracket attributes`() {
        val code = """
            public string? Name => values[0] ?? fallback?.ToString();
            var text = "[Obsolete]";
        """.trimIndent()

        assertTokenAt("csharp", code, "public", "Keyword")
        assertTokenAt("csharp", code, "string", "Type")
        assertTokenAt("csharp", code, "??", "Operator")
        assertTokenAt("csharp", code, "ToString", "Function")
        assertTokenAt("csharp", code, "\"[Obsolete]\"", "String")
        assertNoTokenAt("csharp", code, "[0]", "Annotation")
        assertNoTokenAt("csharp", code, "[Obsolete]", "Annotation")
    }
}
