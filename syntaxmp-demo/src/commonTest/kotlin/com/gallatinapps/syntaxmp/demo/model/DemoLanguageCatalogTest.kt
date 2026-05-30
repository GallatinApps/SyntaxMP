package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoLanguageCatalogTest {
    @Test
    fun catalogCoversDefaultLanguageSetInStableOrder() {
        val defaultLanguageIds = SyntaxLanguageId.BuiltIns.toList()
        val catalogLanguageIds = DemoLanguageCatalog.Languages
            .map { language -> language.id }
            .distinct()

        assertEquals(
            expected = defaultLanguageIds,
            actual = catalogLanguageIds,
            message = "Demo catalog should cover SyntaxLanguageId.BuiltIns order.",
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
                actual = language.id in SyntaxLanguageId.BuiltIns,
                message = "Expected ${language.id.value} to be enabled by SyntaxLanguageId.BuiltIns.",
            )
        }
    }

    @Test
    fun defaultLanguagePrefersKotlin() {
        val defaultLanguage = assertNotNull(DemoLanguageCatalog.defaultLanguage())

        assertEquals(SyntaxLanguageId.Kotlin, defaultLanguage.id)
    }

    @Test
    fun defaultLanguageFallsBackToFirstCatalogEntryWhenKotlinIsUnavailable() {
        val withoutKotlin = DemoLanguageCatalog.Languages
            .filterNot { it.id == SyntaxLanguageId.Kotlin }

        val defaultLanguage = assertNotNull(DemoLanguageCatalog.defaultLanguage(withoutKotlin))

        assertEquals(withoutKotlin.first().id, defaultLanguage.id)
    }

    @Test
    fun languageLookupUsesNormalizedIds() {
        val kotlin = assertNotNull(DemoLanguageCatalog.languageById("  KOTLIN "))

        assertEquals(SyntaxLanguageId.Kotlin, kotlin.id)
    }

    @Test
    fun gradleAliasesResolveToGroovy() {
        val groovy = assertNotNull(DemoLanguageCatalog.languageByRouteSegment("groovy"))

        assertEquals(SyntaxLanguageId.Groovy, groovy.id)
        assertEquals("Groovy", groovy.displayName)
        assertEquals("groovy", groovy.routeSegment)
        assertEquals(groovy, DemoLanguageCatalog.languageByRouteSegment("gradle"))
        assertEquals(groovy, DemoLanguageCatalog.languageByRouteSegment("gradle.groovy"))
        assertEquals(groovy, DemoLanguageCatalog.languageByRouteSegment("build.gradle"))
    }

    @Test
    fun everySampleTokenizesIntoValidSpans() {
        val engine = SyntaxTokenizerEngine()

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
                        span.languageId in SyntaxLanguageId.BuiltIns
                },
                message = "Expected ${language.id.value} spans to stay within sample bounds.",
            )
        }
    }
}
