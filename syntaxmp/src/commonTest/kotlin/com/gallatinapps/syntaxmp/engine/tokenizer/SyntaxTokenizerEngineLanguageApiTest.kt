package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntaxTokenizerEngineLanguageApiTest {
    @Test
    fun nullAndBlankLanguagesReturnNoSpans() {
        assertTrue(SyntaxTokenizerEngine().tokenize("val answer = 42", null).isEmpty())
        assertTrue(SyntaxTokenizerEngine().tokenize("val answer = 42", "").isEmpty())
        assertTrue(SyntaxTokenizerEngine().tokenize("val answer = 42", "   ").isEmpty())
    }

    @Test
    fun builtInAliasesRouteThroughEngine() {
        val spans = SyntaxTokenizerEngine().tokenize("val answer = 42", "kt")

        assertTrue(spans.isNotEmpty())
        assertTrue(spans.all { it.languageId == SyntaxLanguageId.Kotlin })
    }

    @Test
    fun aliasKeysNormalizeAndCanTargetExtensionLanguages() {
        val childLanguage = SyntaxLanguageId.fromString("child")
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
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
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.TypeScript,
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

        assertEquals(SyntaxLanguageId.TypeScript, typeSpan.languageId)
    }

    @Test
    fun exactCustomExtensionIdsRouteAtTopLevel() {
        val customLanguage = SyntaxLanguageId.fromString("myql")
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
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
        val customLanguage = SyntaxLanguageId.fromString("typed-query")
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
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
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Kotlin,
                    aliases = setOf("kte"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Comment),
                ),
            ),
        )

        listOf("kotlin", "kt", "kts", "kte").forEach { label ->
            val span = engine.tokenize("val answer = 42", label).single()
            assertEquals(SyntaxRole.Comment, span.role)
            assertEquals(SyntaxLanguageId.Kotlin, span.languageId)
        }
    }

    @Test
    fun customLanguageAliasCanShadowBuiltInLabels() {
        val customLanguage = SyntaxLanguageId.fromString("kotlin-plus")
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
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
        val disabled = SyntaxTokenizerEngine(builtInLanguages = emptySet())
        val overridden = SyntaxTokenizerEngine(
            builtInLanguages = emptySet(),
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Kotlin,
                    tokenizer = tokenizerWithRole(SyntaxRole.Constant),
                ),
            ),
        )

        assertTrue(disabled.tokenize("val answer = 42", "kt").isEmpty())
        assertEquals(SyntaxRole.Constant, overridden.tokenize("val answer = 42", "kt").single().role)
    }

    @Test
    fun engineResolveUsesExtensionContext() {
        val customLanguage = SyntaxLanguageId.fromString("myql")
        val engine = SyntaxTokenizerEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = customLanguage,
                    aliases = setOf("mql"),
                    tokenizer = tokenizerWithRole(SyntaxRole.Function),
                ),
            ),
        )

        assertEquals(customLanguage, engine.resolveLanguageId("myql"))
        assertEquals(customLanguage, engine.resolveLanguageId("mql"))
        assertEquals(SyntaxLanguageId.Kotlin, engine.resolveLanguageId("kt"))
        assertEquals(SyntaxLanguageId.fromString("unknown"), engine.resolveLanguageId("unknown"))
        assertEquals(null, engine.resolveLanguageId(null))
        assertEquals(null, engine.resolveLanguageId(" "))
    }
}
