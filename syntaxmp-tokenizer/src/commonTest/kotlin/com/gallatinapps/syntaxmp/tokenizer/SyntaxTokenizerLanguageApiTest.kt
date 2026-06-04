package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyntaxTokenizerLanguageApiTest {
    @Test
    fun nullAndBlankLanguagesReturnNoSpans() {
        assertTrue(SyntaxTokenizer().tokenize("val answer = 42", null).isEmpty())
        assertTrue(SyntaxTokenizer().tokenize("val answer = 42", "").isEmpty())
        assertTrue(SyntaxTokenizer().tokenize("val answer = 42", "   ").isEmpty())
    }

    @Test
    fun builtInAliasesRouteThroughEngine() {
        val spans = SyntaxTokenizer().tokenize("val answer = 42", "kt")

        assertTrue(spans.isNotEmpty())
        assertTrue(spans.all { it.languageId == LanguageId.Kotlin })
    }

    @Test
    fun rejectsCustomIdsInBuiltInLanguages() {
        val failure = assertFailsWith<IllegalArgumentException> {
            SyntaxTokenizer(
                builtInLanguages = setOf(LanguageId.fromString("astro")),
            )
        }
        val message = failure.message.orEmpty()

        assertTrue(message.contains("builtInLanguages must be a subset of LanguageId.BuiltIns"))
        assertTrue(message.contains("Custom languages must be registered through LanguageExtension"))
        assertTrue(message.contains("astro"))
    }

    @Test
    fun languageCatalogReflectsActiveBuiltInsAndExtensions() {
        val extensionLanguage = LanguageId.fromString("astro")
        val engine = SyntaxTokenizer(
            builtInLanguages = setOf(LanguageId.Kotlin),
            extensions = listOf(
                LanguageExtension(
                    languageId = extensionLanguage,
                    aliases = setOf(" Astro Component "),
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )

        assertEquals(setOf(LanguageId.Kotlin, extensionLanguage), engine.languageIds)
        assertTrue("kotlin" in engine.languageLabels)
        assertTrue("kt" in engine.languageLabels)
        assertTrue("kts" in engine.languageLabels)
        assertTrue("gradle.kts" in engine.languageLabels)
        assertTrue("astro" in engine.languageLabels)
        assertTrue("astro component" in engine.languageLabels)
        assertFalse("javascript" in engine.languageLabels)
        assertFalse("js" in engine.languageLabels)
    }

    @Test
    fun disabledBuiltInLanguagesAreRemovedFromCatalogAndResolution() {
        val engine = SyntaxTokenizer(
            builtInLanguages = LanguageId.BuiltIns - LanguageId.Kotlin,
        )

        assertFalse(LanguageId.Kotlin in engine.languageIds)
        listOf("kotlin", "kt", "kts", "gradle.kts").forEach { label ->
            assertFalse(label in engine.languageLabels)
            assertNull(engine.resolveLanguageId(label))
        }
    }

    @Test
    fun aliasKeysNormalizeAndCanTargetExtensionLanguages() {
        val childLanguage = LanguageId.fromString("child")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = childLanguage,
                    aliases = setOf(" Child "),
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )
        val code = """
            ```CHILD
            special
            ```
        """.trimIndent()

        val childSpan = engine.tokenize(code, "markdown")
            .first { it.role == SyntaxRole.Function }

        assertEquals(childLanguage, childSpan.languageId)
    }

    @Test
    fun builtInAliasesRouteToOverriddenBuiltInLanguages() {
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.TypeScript,
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )
        val code = """
            ```ts
            type Job = { enabled: boolean }
            ```
        """.trimIndent()

        val typeSpan = engine.tokenize(code, "markdown")
            .first { span -> span.role == SyntaxRole.Function }

        assertEquals(LanguageId.TypeScript, typeSpan.languageId)
    }

    @Test
    fun exactCustomExtensionIdsRouteAtTopLevel() {
        val customLanguage = LanguageId.fromString("myql")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = customLanguage,
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )

        val span = engine.tokenize("select now()", "myql").single()

        assertEquals(SyntaxRole.Function, span.role)
        assertEquals(customLanguage, span.languageId)
    }

    @Test
    fun extensionAliasesBeatBuiltInAliases() {
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

        val span = engine.tokenize("select now()", "ts").single()

        assertEquals(SyntaxRole.String, span.role)
        assertEquals(customLanguage, span.languageId)
    }

    @Test
    fun builtInOverrideReceivesBuiltInAliases() {
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    aliases = setOf("kte"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Comment),
                ),
            ),
        )

        listOf("kotlin", "kt", "kts", "kte").forEach { label ->
            val span = engine.tokenize("val answer = 42", label).single()
            assertEquals(SyntaxRole.Comment, span.role)
            assertEquals(LanguageId.Kotlin, span.languageId)
        }
    }

    @Test
    fun customLanguageAliasCanShadowBuiltInLabels() {
        val customLanguage = LanguageId.fromString("kotlin-plus")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = customLanguage,
                    aliases = setOf("kotlin", "kt"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Property),
                ),
            ),
        )

        val span = engine.tokenize("val answer = 42", "kt").single()

        assertEquals(SyntaxRole.Property, span.role)
        assertEquals(customLanguage, span.languageId)
    }

    @Test
    fun disabledBuiltInAliasesDoNotTokenizeUnlessExtensionHandlesLanguage() {
        val disabled = SyntaxTokenizer(builtInLanguages = emptySet())
        val overridden = SyntaxTokenizer(
            builtInLanguages = emptySet(),
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    tokenizer = tokenizerWithRole(SyntaxRole.Constant),
                ),
            ),
        )

        assertTrue(disabled.tokenize("val answer = 42", "kt").isEmpty())
        assertTrue(overridden.tokenize("val answer = 42", "kt").isEmpty())
        assertEquals(SyntaxRole.Constant, overridden.tokenize("val answer = 42", "kotlin").single().role)
    }

    @Test
    fun engineResolveUsesExtensionContext() {
        val customLanguage = LanguageId.fromString("myql")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = customLanguage,
                    aliases = setOf("mql"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )

        assertEquals(customLanguage, engine.resolveLanguageId("myql"))
        assertEquals(customLanguage, engine.resolveLanguageId("mql"))
        assertEquals(LanguageId.Kotlin, engine.resolveLanguageId("kt"))
        assertNull(engine.resolveLanguageId("unknown"))
        assertNull(engine.resolveLanguageId(null))
        assertNull(engine.resolveLanguageId(" "))
    }
}
