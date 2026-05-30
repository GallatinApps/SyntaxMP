package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntaxTokenizerEngineExtensionTest {
    @Test
    fun extensionLanguageWinsBeforeBuiltInRoute() {
        val first = tokenizerWithRole(SyntaxRole.String)
        val engine = testEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Kotlin,
                    tokenizer = first,
                ),
            ),
        )

        val result = engine.tokenize("value", "kotlin")

        assertEquals(SyntaxRole.String, result.single().role)
        assertEquals(SyntaxLanguageId.Kotlin, result.single().languageId)
    }

    @Test
    fun extensionEmptyResultIsSticky() {
        val engine = testEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Kotlin,
                    tokenizer = SyntaxTokenizer { SyntaxTokenizeResult() },
                ),
            ),
        )

        val result = engine.tokenize("val answer = 42", "kotlin")

        assertTrue(result.isEmpty())
    }

    @Test
    fun aliasesCreateTopLevelRoutes() {
        val customLanguage = SyntaxLanguageId.fromString("custom")
        val engine = testEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = customLanguage,
                    aliases = setOf("query"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Keyword),
                ),
            ),
        )

        val result = engine.tokenize("select", "query")

        assertEquals(SyntaxRole.Keyword, result.single().role)
        assertEquals(customLanguage, result.single().languageId)
    }

    @Test
    fun builtInAliasesAreFilteredByEnabledLanguagesButExtensionsAreNot() {
        val engine = SyntaxTokenizerEngine(
            builtInLanguages = setOf(SyntaxLanguageId.Kotlin),
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Java,
                    tokenizer = tokenizerWithRole(SyntaxRole.String),
                ),
            ),
        )

        assertTrue(engine.tokenize("const answer = 42", "js").isEmpty())
        assertEquals(
            SyntaxRole.String,
            engine.tokenize("class Demo {}", "java").single().role,
        )
    }

}
