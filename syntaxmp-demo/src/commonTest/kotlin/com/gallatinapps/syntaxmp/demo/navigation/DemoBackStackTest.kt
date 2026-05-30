package com.gallatinapps.syntaxmp.demo.navigation

import androidx.navigation3.runtime.NavKey
import com.gallatinapps.syntaxmp.demo.model.DemoLanguageCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class DemoBackStackTest {
    @Test
    fun indexOnlyBackStackDefaultsToGetStartedDetail() {
        val backStack = listOf<NavKey>(DemoRoute.LanguageIndex)

        assertEquals(DemoRoute.GetStarted, backStack.lastDetailRoute())
        assertNull(backStack.selectedDetailRouteOrNull())
        assertNull(backStack.selectedLanguageOrNull())
    }

    @Test
    fun missingPreviewRouteIsReplacedWithDefaultLanguage() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.LanguagePreview("missing-language"),
        )

        backStack.replaceMissingLanguageRoute(defaultLanguage)

        assertEquals(DemoRoute.LanguagePreview(defaultLanguage.id.value), backStack.last())
    }

    @Test
    fun missingPreviewRouteHasNoSelectedLanguageBeforeRepair() {
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.LanguagePreview("missing-language"),
        )

        assertNull(backStack.selectedLanguageOrNull())
    }

    @Test
    fun navigatingFromIndexAddsGetStartedRoute() {
        val backStack = mutableListOf<NavKey>(DemoRoute.LanguageIndex)

        backStack.navigateToGetStarted()

        assertEquals(listOf<NavKey>(DemoRoute.LanguageIndex, DemoRoute.GetStarted), backStack)
    }

    @Test
    fun notFoundRouteIsADetailRoute() {
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.NotFound("/lang/swifts"),
        )

        assertEquals(DemoRoute.NotFound("/lang/swifts"), backStack.lastDetailRoute())
    }

    @Test
    fun replacingDetailWithNullReturnsToIndexOnlyBackStack() {
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.NotFound("/lang/swifts"),
        )

        backStack.replaceDetailRoute(null)

        assertEquals(listOf<NavKey>(DemoRoute.LanguageIndex), backStack)
    }

    @Test
    fun selectingCurrentGetStartedRouteDoesNotDuplicateBackStackEntry() {
        val backStack = mutableListOf<NavKey>(DemoRoute.LanguageIndex, DemoRoute.GetStarted)

        backStack.navigateToGetStarted()

        assertEquals(listOf<NavKey>(DemoRoute.LanguageIndex, DemoRoute.GetStarted), backStack)
    }

    @Test
    fun selectingCurrentPreviewRouteDoesNotDuplicateBackStackEntry() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.LanguagePreview(defaultLanguage.id.value),
        )

        backStack.navigateToLanguage(defaultLanguage.id.value)

        assertEquals(2, backStack.size)
        assertEquals(DemoRoute.LanguagePreview(defaultLanguage.id.value), backStack.last())
    }

    @Test
    fun selectingLanguageReplacesVisibleGetStartedRoute() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val backStack = mutableListOf<NavKey>(DemoRoute.LanguageIndex, DemoRoute.GetStarted)

        backStack.navigateToLanguage(defaultLanguage.id.value)

        assertEquals(2, backStack.size)
        assertEquals(DemoRoute.LanguagePreview(defaultLanguage.id.value), backStack.last())
    }

    @Test
    fun selectingLanguageReplacesVisibleNotFoundRoute() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.NotFound("/lang/swifts"),
        )

        backStack.navigateToLanguage(defaultLanguage.id.value)

        assertEquals(2, backStack.size)
        assertEquals(DemoRoute.LanguagePreview(defaultLanguage.id.value), backStack.last())
    }

    @Test
    fun selectingDifferentLanguageReplacesVisiblePreviewRoute() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val nextLanguage = requireNotNull(
            DemoLanguageCatalog.Languages.firstOrNull { language ->
                language.id != defaultLanguage.id
            },
        )
        assertNotEquals(defaultLanguage.id, nextLanguage.id)
        val backStack = mutableListOf<NavKey>(
            DemoRoute.LanguageIndex,
            DemoRoute.LanguagePreview(defaultLanguage.id.value),
        )

        backStack.navigateToLanguage(nextLanguage.id.value)

        assertEquals(2, backStack.size)
        val route = assertIs<DemoRoute.LanguagePreview>(backStack.last())
        assertEquals(nextLanguage.routeSegment, route.routeSegment)
    }

    @Test
    fun navigatingFromIndexAddsPreviewRoute() {
        val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
        val backStack = mutableListOf<NavKey>(DemoRoute.LanguageIndex)

        backStack.navigateToLanguage(defaultLanguage.id.value)

        assertEquals(
            listOf<NavKey>(DemoRoute.LanguageIndex, DemoRoute.LanguagePreview(defaultLanguage.id.value)),
            backStack,
        )
    }
}
