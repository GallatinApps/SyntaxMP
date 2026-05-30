package com.gallatinapps.syntaxmp.languages.fixtures.c

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class CFixtureTest {
    private val code = """
        #include <stdio.h>
        /* block comment */
        int score(int count) {
          printf("count\n");
          if (count > 0) return count + 1;
          return NULL;
        }
    """.trimIndent()

    @Test
    fun `c preprocessor comments types functions variables constants and operators`() {
        assertTokenAt("c", code, "#include <stdio.h>", "Annotation")
        assertTokenAt("c", code, "/* block comment */", "Comment")
        assertTokenAt("c", code, "int", "Type")
        assertTokenAt("c", code, "score", "Function")
        assertTokenAt("c", code, "count", "Variable")
        assertTokenAt("c", code, "printf", "Function")
        assertTokenAt("c", code, "\\n", "Escape")
        assertTokenAt("c", code, "if", "Keyword")
        assertTokenAt("c", code, ">", "Operator", occurrence = 1)
        assertTokenAt("c", code, "0", "Number")
        assertTokenAt("c", code, "return", "Keyword")
        assertTokenAt("c", code, "NULL", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "c",
        code = "char *label = \"return\";",
        substring = "return",
        category = "Keyword",
    )

    @Test
    fun `enum members are constants and struct fields are properties`() {
        val code = """
            typedef struct Note {
              int count;
              char *title;
            } Note;

            enum NoteStatus {
              NOTE_DRAFT = 0,
              NOTE_REVIEW = 1,
            };
        """.trimIndent()

        assertTokenAt("c", code, "count", "Property")
        assertTokenAt("c", code, "title", "Property")
        assertTokenAt("c", code, "NOTE_DRAFT", "Constant")
        assertTokenAt("c", code, "NOTE_REVIEW", "Constant")
        assertTokenAt("c", code, "0", "Number")
    }

    @Test
    fun `c line comments character literals macros and comment strings stay scoped`() {
        val code = """
            #define MAX(a, b) ((a) > (b) ? (a) : (b))
            // return "not a string"
            char marker = 'x';
            int value = MAX(count, 1);
        """.trimIndent()

        assertTokenAt("c", code, "#define MAX(a, b) ((a) > (b) ? (a) : (b))", "Annotation")
        assertTokenAt("c", code, "// return \"not a string\"", "Comment")
        assertTokenAt("c", code, "char", "Type")
        assertTokenAt("c", code, "'x'", "String")
        assertTokenAt("c", code, "MAX", "Function", occurrence = 1)
        assertTokenAt("c", code, "1", "Number")
        assertNoTokenAt("c", code, "return", "Keyword")
        assertNoTokenAt("c", code, "\"not a string\"", "String")
    }
}
