package com.gallatinapps.syntaxmp.builtins.fixtures.php

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class PhpFixtureTest {
    private val code = """
        <?php
        // php scoring
        function score(array ${'$'}job): int {
            /* block */
            if (${ '$' }job["enabled"] === true) {
                return count(${ '$' }job) + 1;
            }
            return null;
        }
    """.trimIndent()

    @Test
    fun `php comments declarations types sigil variables functions constants and numbers`() {
        assertTokenAt("php", code, "// php scoring", "Comment")
        assertTokenAt("php", code, "function", "Keyword")
        assertTokenAt("php", code, "score", "Function")
        assertTokenAt("php", code, "array", "Type")
        assertTokenAt("php", code, "${'$'}job", "Variable")
        assertTokenAt("php", code, "int", "Type")
        assertTokenAt("php", code, "/* block */", "Comment")
        assertTokenAt("php", code, "if", "Keyword")
        assertTokenAt("php", code, "\"enabled\"", "String")
        assertTokenAt("php", code, "true", "Constant")
        assertTokenAt("php", code, "count", "Function")
        assertTokenAt("php", code, "1", "Number")
        assertTokenAt("php", code, "null", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "php",
        code = "<?php ${'$'}label = \"function\";",
        substring = "function",
        category = "Keyword",
    )

    @Test
    fun `double quoted strings tokenize variables and format verbs`() {
        val code = "<?php ${'$'}label = \"job ${'$'}name %02d\";"

        assertTokenAt("php", code, "\"job ", "String")
        assertTokenAt("php", code, "${'$'}name", "Variable")
        assertTokenAt("php", code, "%02d", "Escape")
    }

    @Test
    fun `heredoc interpolates variables and nowdoc stays literal`() {
        val code = """
            <?php
            ${'$'}doc = <<<HTML
            hello ${'$'}name
            HTML;
            ${'$'}plain = <<<'TEXT'
            function ${'$'}name
            TEXT;
        """.trimIndent()

        assertTokenAt("php", code, "<<<HTML\nhello ", "String")
        assertTokenAt("php", code, "${'$'}name", "Variable")
        assertNoTokenAt("php", code, "function", "Keyword")
        assertNoTokenAt("php", code, "${'$'}name", "Variable", occurrence = 1)
    }

    @Test
    fun `php attributes are annotations and hash comments remain comments`() {
        val code = """
            <?php
            #[Attribute]
            #[Route('/notes/{id<\d+>}', methods: ['GET', 'POST'])]
            final class NoteController {}
            # ordinary comment
        """.trimIndent()

        assertTokenAt("php", code, "#[Attribute]", "Annotation")
        assertTokenAt("php", code, "#[Route", "Annotation")
        assertTokenAt("php", code, "'/notes/{id<", "String")
        assertTokenAt("php", code, "\\d", "Escape")
        assertTokenAt("php", code, "+>}'", "String")
        assertTokenAt("php", code, "'GET'", "String")
        assertTokenAt("php", code, "'POST'", "String")
        assertTokenAt("php", code, "# ordinary comment", "Comment")
    }
}
