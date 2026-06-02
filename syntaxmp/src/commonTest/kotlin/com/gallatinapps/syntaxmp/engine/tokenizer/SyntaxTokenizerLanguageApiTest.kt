package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertEquals(SyntaxRole.Constant, overridden.tokenize("val answer = 42", "kt").single().role)
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
        assertEquals(LanguageId.fromString("unknown"), engine.resolveLanguageId("unknown"))
        assertEquals(null, engine.resolveLanguageId(null))
        assertEquals(null, engine.resolveLanguageId(" "))
    }
}
