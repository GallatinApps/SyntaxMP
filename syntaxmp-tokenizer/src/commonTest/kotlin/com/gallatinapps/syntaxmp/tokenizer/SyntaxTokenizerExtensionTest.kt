package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyntaxTokenizerExtensionTest {
    @Test
    fun extensionLanguageWinsBeforeBuiltInRoute() {
        val first = tokenizerWithRole(SyntaxRole.String)
        val engine = testEngine(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    tokenizer = first,
                ),
            ),
        )

        val result = engine.tokenize("value", "kotlin")

        assertEquals(SyntaxRole.String, result.single().role)
        assertEquals(LanguageId.Kotlin, result.single().languageId)
    }

    @Test
    fun extensionEmptyResultIsSticky() {
        val engine = testEngine(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    tokenizer = LanguageTokenizer { emptyList() },
                ),
            ),
        )

        val result = engine.tokenize("val answer = 42", "kotlin")

        assertTrue(result.isEmpty())
    }

    @Test
    fun aliasesCreateTopLevelRoutes() {
        val customLanguage = LanguageId.fromString("custom")
        val engine = testEngine(
            extensions = listOf(
                LanguageExtension(
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
    fun extensionIdCreatesTopLevelRouteWithoutAliases() {
        val customLanguage = LanguageId.fromString("custom")
        val engine = SyntaxTokenizer(
            builtInLanguages = emptySet(),
            extensions = listOf(
                LanguageExtension(
                    languageId = customLanguage,
                    tokenizer = tokenizerWithRole(SyntaxRole.Keyword),
                ),
            ),
        )

        assertEquals(customLanguage, engine.resolveLanguageId("custom"))
        assertTrue("custom" in engine.languageLabels)
        assertEquals(SyntaxRole.Keyword, engine.tokenize("select", "custom").single().role)
    }

    @Test
    fun builtInAliasesAreFilteredByEnabledLanguagesButExtensionsAreNot() {
        val engine = SyntaxTokenizer(
            builtInLanguages = setOf(LanguageId.Kotlin),
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Java,
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

    @Test
    fun disabledBuiltInOverrideKeepsOnlyExtensionLabels() {
        val engine = SyntaxTokenizer(
            builtInLanguages = LanguageId.BuiltIns - LanguageId.Kotlin,
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    aliases = setOf("kt"),
                    tokenizer = tokenizerWithRole(SyntaxRole.String),
                ),
            ),
        )

        assertEquals(LanguageId.Kotlin, engine.resolveLanguageId("kotlin"))
        assertEquals(LanguageId.Kotlin, engine.resolveLanguageId("kt"))
        assertNull(engine.resolveLanguageId("kts"))
        assertNull(engine.resolveLanguageId("gradle.kts"))
        assertTrue("kotlin" in engine.languageLabels)
        assertTrue("kt" in engine.languageLabels)
        assertFalse("kts" in engine.languageLabels)
        assertFalse("gradle.kts" in engine.languageLabels)
        listOf("kotlin", "kt").forEach { label ->
            assertEquals(SyntaxRole.String, engine.tokenize("value", label).single().role)
        }
    }

    @Test
    fun enabledBuiltInOverrideKeepsBuiltInAliases() {
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    tokenizer = tokenizerWithRole(SyntaxRole.String),
                ),
            ),
        )

        listOf("kotlin", "kt", "kts", "gradle.kts").forEach { label ->
            assertEquals(LanguageId.Kotlin, engine.resolveLanguageId(label))
            assertTrue(label in engine.languageLabels)
            assertEquals(SyntaxRole.String, engine.tokenize("value", label).single().role)
        }
    }

    @Test
    fun extensionAliasCanShadowBuiltInAliasWithoutTakingBuiltInCanonicalId() {
        val customLanguage = LanguageId.fromString("typed-query")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = customLanguage,
                    aliases = setOf("ts"),
                    tokenizer = tokenizerWithRole(SyntaxRole.String),
                ),
            ),
        )

        assertEquals(customLanguage, engine.resolveLanguageId("ts"))
        assertEquals(LanguageId.TypeScript, engine.resolveLanguageId("typescript"))
        assertEquals(SyntaxRole.String, engine.tokenize("select now()", "ts").single().role)
        assertTrue(engine.tokenize("type Answer = number", "typescript").isNotEmpty())
    }

}
