package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoLanguageCatalogTest {
    @Test
    fun catalogCoversDefaultLanguageSetInStableOrder() {
        val defaultLanguageIds = LanguageId.BuiltIns.toList()
        val catalogLanguageIds = DemoLanguageCatalog.Languages
            .map { language -> language.id }
            .distinct()

        assertEquals(
            expected = defaultLanguageIds,
            actual = catalogLanguageIds,
            message = "Demo catalog should cover LanguageId.BuiltIns order.",
        )
    }

    @Test
    fun everyCatalogRouteSegmentIsUnique() {
        val routeSegments = DemoLanguageCatalog.Languages.map { language -> language.routeSegment }

        assertEquals(
            expected = routeSegments.size,
            actual = routeSegments.toSet().size,
            message = "Demo catalog route segments should be unique.",
        )
    }

    @Test
    fun everyCatalogLanguageIsEnabledByDefault() {
        DemoLanguageCatalog.Languages.forEach { language ->
            assertTrue(
                actual = language.id in LanguageId.BuiltIns,
                message = "Expected ${language.id.value} to be enabled by LanguageId.BuiltIns.",
            )
        }
    }

    @Test
    fun defaultLanguagePrefersKotlin() {
        val defaultLanguage = assertNotNull(DemoLanguageCatalog.defaultLanguage())

        assertEquals(LanguageId.Kotlin, defaultLanguage.id)
    }

    @Test
    fun defaultLanguageFallsBackToFirstCatalogEntryWhenKotlinIsUnavailable() {
        val withoutKotlin = DemoLanguageCatalog.Languages
            .filterNot { it.id == LanguageId.Kotlin }

        val defaultLanguage = assertNotNull(DemoLanguageCatalog.defaultLanguage(withoutKotlin))

        assertEquals(withoutKotlin.first().id, defaultLanguage.id)
    }

    @Test
    fun languageLookupUsesNormalizedIds() {
        val kotlin = assertNotNull(DemoLanguageCatalog.languageById("  KOTLIN "))

        assertEquals(LanguageId.Kotlin, kotlin.id)
    }

    @Test
    fun splitIdentityAliasesResolveToTheirOwnCatalogEntries() {
        val properties = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("properties"))
        val dotenv = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("env"))
        val shell = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("sh"))
        val bash = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("bash"))
        val zsh = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("zsh"))

        assertEquals(LanguageId.Properties, properties.id)
        assertEquals("properties", properties.routeSegment)
        assertEquals(LanguageId.Dotenv, dotenv.id)
        assertEquals("dotenv", dotenv.routeSegment)
        assertEquals(LanguageId.Shell, shell.id)
        assertEquals("shell", shell.routeSegment)
        assertEquals(LanguageId.Bash, bash.id)
        assertEquals(LanguageId.Zsh, zsh.id)
    }

    @Test
    fun routeSegmentsAndAliasesResolveThroughSyntaxTokenizer() {
        val engine = SyntaxTokenizer()

        DemoLanguageCatalog.Languages.forEach { language ->
            val labels = listOf(language.routeSegment) + language.aliases

            labels.forEach { label ->
                assertEquals(
                    expected = language.id,
                    actual = engine.resolveLanguageId(label),
                    message = "Expected demo label $label to resolve to ${language.id.value}.",
                )
            }
        }
    }

    @Test
    fun everySampleTokenizesIntoValidSpans() {
        val engine = SyntaxTokenizer()

        DemoLanguageCatalog.Languages.forEach { language ->
            assertTrue(
                actual = language.sample.isNotBlank(),
                message = "Expected ${language.id.value} to have a non-empty demo sample.",
            )

            val spans = engine.tokenize(
                code = language.sample,
                languageLabel = language.id.value,
            )
            assertTrue(
                actual = spans.isNotEmpty(),
                message = "Expected ${language.displayName} sample to produce at least one span.",
            )
            assertTrue(
                actual = spans.all { span ->
                    span.start in language.sample.indices &&
                        span.endExclusive in 1..language.sample.length &&
                        span.start < span.endExclusive &&
                        span.languageId in LanguageId.BuiltIns
                },
                message = "Expected ${language.id.value} spans to stay within sample bounds.",
            )
        }
    }
}
