package com.gallatinapps.syntaxmp.languages.fixtures.java

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class JavaFixtureTest {
    private val code = """
        // java scoring
        package demo;
        public class Job {
            @Override
            public String label(int count) {
                return Math.max(0, count) + " items";
            }
            static void reset() {}
        }
    """.trimIndent()

    @Test
    fun `java comments packages declarations annotations types functions and strings`() {
        assertTokenAt("java", code, "// java scoring", "Comment")
        assertTokenAt("java", code, "package", "Keyword")
        assertTokenAt("java", code, "public", "Keyword")
        assertTokenAt("java", code, "class", "Keyword")
        assertTokenAt("java", code, "Job", "Type")
        assertTokenAt("java", code, "@Override", "Annotation")
        assertTokenAt("java", code, "String", "Type")
        assertTokenAt("java", code, "int", "Type")
        assertTokenAt("java", code, "label", "Function")
        assertTokenAt("java", code, "return", "Keyword")
        assertTokenAt("java", code, "Math", "Variable", "variable.namespace")
        assertTokenAt("java", code, "max", "Function")
        assertTokenAt("java", code, "\" items\"", "String")
        assertTokenAt("java", code, "void", "Type")
        assertTokenAt("java", code, "reset", "Function")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "java",
        code = "String label = \"class\";",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `text blocks are strings`() {
        val block = "\"\"\"\nclass text\n\"\"\""
        val code = "String text = $block;"

        assertTokenAt("java", code, block, "String")
        assertNoTokenAt("java", code, "class", "Keyword")
    }

    @Test
    fun `java generics enum constants chars fields and comment contents are covered`() {
        val code = """
            enum Status { DRAFT, REVIEW; }
            class Box<T> {
                T value;
                char marker = 'x';
                java.util.List<String> names;
                void read() {
                    var item = names.get(0);
                }
            }
            String text = "@Override";
            // class "not string"
        """.trimIndent()

        assertTokenAt("java", code, "enum", "Keyword")
        assertTokenAt("java", code, "Status", "Type")
        assertTokenAt("java", code, "DRAFT", "Constant")
        assertTokenAt("java", code, "REVIEW", "Constant")
        assertTokenAt("java", code, "Box", "Type")
        assertTokenAt("java", code, "T", "Type", occurrence = 1)
        assertTokenAt("java", code, "char", "Type")
        assertTokenAt("java", code, "value", "Variable")
        assertTokenAt("java", code, "marker", "Variable")
        assertTokenAt("java", code, "names", "Variable")
        assertTokenAt("java", code, "'x'", "String")
        assertTokenAt("java", code, "String", "Type")
        assertTokenAt("java", code, "get", "Function")
        assertTokenAt("java", code, "\"@Override\"", "String")
        assertTokenAt("java", code, "// class \"not string\"", "Comment")
        assertNoTokenAt("java", code, "@Override", "Annotation")
        assertNoTokenAt("java", code, "class", "Keyword", occurrence = 1)
        assertNoTokenAt("java", code, "\"not string\"", "String")
    }
}
