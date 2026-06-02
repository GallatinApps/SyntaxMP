package com.gallatinapps.syntaxmp.builtins.fixtures.ruby

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokens
import com.gallatinapps.syntaxmp.builtins.fixtures.tk
import kotlin.test.Test

class RubyFixtureTest {
    private val code = """
        # ruby scoring
        class Job
          def score(count)
            @name = "NoteKit"
            puts @name if count > 0
            return true
          end
        end
    """.trimIndent()

    @Test
    fun `ruby comments declarations sigil variables builtins constants and numbers`() {
        assertTokenAt("ruby", code, "# ruby scoring", "Comment")
        assertTokenAt("ruby", code, "class", "Keyword")
        assertTokenAt("ruby", code, "Job", "Type")
        assertTokenAt("ruby", code, "def", "Keyword")
        assertTokenAt("ruby", code, "score", "Function")
        assertTokenAt("ruby", code, "@name", "Variable")
        assertTokenAt("ruby", code, "\"NoteKit\"", "String")
        assertTokenAt("ruby", code, "puts", "Function")
        assertTokenAt("ruby", code, "if", "Keyword")
        assertTokenAt("ruby", code, "0", "Number")
        assertTokenAt("ruby", code, "return", "Keyword")
        assertTokenAt("ruby", code, "true", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "ruby",
        code = "label = \"class\"",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `hash brace interpolation tokenizes expression`() {
        val code = "message = \"hello #{name}!\""

        assertTokenAt("ruby", code, "#{", "Escape")
        assertTokenAt("ruby", code, "name", "Variable")
        assertTokenAt("ruby", code, "}", "Escape")
    }

    @Test
    fun `hash brace interpolation preserves exact string expression alternation`() {
        val code = "\"hello #{name}!\""

        assertTokens(
            language = "ruby",
            code = code,
            expected = listOf(
                tk(0, 7, "String", "string"),
                tk(7, 9, "Escape", "escape"),
                tk(9, 13, "Variable", "variable"),
                tk(13, 14, "Escape", "escape"),
                tk(14, 16, "String", "string"),
            ),
            onlyAssertedCategories = setOf("String", "Escape", "Variable"),
        )
    }

    @Test
    fun `heredocs and percent literals tokenize interpolation conservatively`() {
        val code = """
            message = <<~TEXT
            hello #{name}
            TEXT
            literal = <<'TEXT'
            class #{name}
            TEXT
            percent = %Q(job #{name})
            plain = %q(class #{name})
        """.trimIndent()

        assertTokenAt("ruby", code, "<<~TEXT\nhello ", "String")
        assertTokenAt("ruby", code, "#{", "Escape")
        assertTokenAt("ruby", code, "name", "Variable")
        assertTokenAt("ruby", code, "}", "Escape")
        assertTokenAt("ruby", code, "%Q(job ", "String")
        assertTokenAt("ruby", code, "#{", "Escape", occurrence = 2)
        assertNoTokenAt("ruby", code, "class", "Keyword")
        assertNoTokenAt("ruby", code, "#{", "Escape", occurrence = 3)
    }

    @Test
    fun `symbol percent literals tokenize interpolation only for uppercase variant`() {
        val code = """
            symbols = %i(alpha beta)
            labels = %I(item #{name})
        """.trimIndent()

        assertTokenAt("ruby", code, "%i(alpha beta)", "String")
        assertNoTokenAt("ruby", code, "alpha", "Variable")
        assertTokenAt("ruby", code, "%I(item ", "String")
        assertTokenAt("ruby", code, "#{", "Escape")
        assertTokenAt("ruby", code, "name", "Variable")
        assertTokenAt("ruby", code, "}", "Escape")
    }

    @Test
    fun `operator looking literals do not shadow modulo or shift operators`() {
        val code = """
            modulo = 10 % 3
            shifted = left << right
        """.trimIndent()

        assertTokenAt("ruby", code, "%", "Operator")
        assertTokenAt("ruby", code, "<<", "Operator")
    }
}
