package com.gallatinapps.syntaxmp.builtins.fixtures.cpp

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class CppFixtureTest {
    private val code = """
        #include <memory>
        // scoring class
        class Job {
        public:
          int score() const { return std::move(value) + 1; }
          bool enabled = true;
        };
    """.trimIndent()

    @Test
    fun `cpp preprocessor comments declarations types builtins constants and operators`() {
        assertTokenAt("cpp", code, "#include <memory>", "Annotation")
        assertTokenAt("cpp", code, "// scoring class", "Comment")
        assertTokenAt("cpp", code, "class", "Keyword", occurrence = 1)
        assertTokenAt("cpp", code, "Job", "Type")
        assertTokenAt("cpp", code, "public", "Keyword")
        assertTokenAt("cpp", code, "int", "Type")
        assertTokenAt("cpp", code, "score", "Function")
        assertTokenAt("cpp", code, "const", "Keyword")
        assertTokenAt("cpp", code, "std", "Variable")
        assertTokenAt("cpp", code, "move", "Function")
        assertTokenAt("cpp", code, "1", "Number")
        assertTokenAt("cpp", code, "true", "Constant")
    }

    @Test
    fun `comments do not emit declarations`() = assertNoTokenAt(
        language = "cpp",
        code = "// class",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `raw and prefixed strings stay coherent`() {
        val code = "auto text = R\"tag(class { value })tag\"; auto label = u8\"hello %d\";"

        assertTokenAt("cpp", code, "R\"tag(class { value })tag\"", "String")
        assertNoTokenAt("cpp", code, "class", "Keyword")
        assertNoTokenAt("cpp", code, "value", "Variable")
        assertTokenAt("cpp", code, "u8\"hello ", "String")
        assertTokenAt("cpp", code, "%d", "Escape")
    }

    @Test
    fun `standard attributes and enum members are recognized`() {
        val code = """
            [[nodiscard]]
            int score();

            enum class NoteStatus {
              Draft,
              Review = 2,
            };
        """.trimIndent()

        assertTokenAt("cpp", code, "[[nodiscard]]", "Annotation")
        assertTokenAt("cpp", code, "Draft", "Constant")
        assertTokenAt("cpp", code, "Review", "Constant")
        assertTokenAt("cpp", code, "2", "Number")
    }

    @Test
    fun `cpp namespaces templates chars and indexing keep roles`() {
        val code = """
            namespace demo::notes {}
            using namespace std::chrono;
            template <typename T>
            class Box {
              T value;
              char marker = 'x';
              void pick() { auto first = items[0]; }
            };
        """.trimIndent()

        assertTokenAt("cpp", code, "namespace", "Keyword")
        assertTokenAt("cpp", code, "demo::notes", "Variable", "variable.namespace")
        assertTokenAt("cpp", code, "std::chrono", "Variable", "variable.namespace")
        assertTokenAt("cpp", code, "template", "Keyword")
        assertTokenAt("cpp", code, "typename", "Keyword")
        assertTokenAt("cpp", code, "T", "Type")
        assertTokenAt("cpp", code, "Box", "Type")
        assertTokenAt("cpp", code, "char", "Type")
        assertTokenAt("cpp", code, "'x'", "String")
        assertTokenAt("cpp", code, "items", "Variable")
        assertTokenAt("cpp", code, "0", "Number")
        assertNoTokenAt("cpp", code, "[0]", "Annotation")
    }
}
