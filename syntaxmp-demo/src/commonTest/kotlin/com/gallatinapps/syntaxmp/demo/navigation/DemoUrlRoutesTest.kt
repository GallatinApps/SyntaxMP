package com.gallatinapps.syntaxmp.demo.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class DemoUrlRoutesTest {
    @Test
    fun blankHashUsesIndexRoute() {
        assertNull(parseDemoHashRoute(""))
        assertNull(parseDemoHashRoute("#"))
        assertNull(parseDemoHashRoute("#/"))
    }

    @Test
    fun getStartedHashParsesToGetStartedRoute() {
        assertEquals(DemoRoute.GetStarted, parseDemoHashRoute("#/getting-started"))
        assertEquals(DemoRoute.GetStarted, parseDemoHashRoute("#/gettingstarted"))
    }

    @Test
    fun languageHashParsesCanonicalAndAliasSegments() {
        assertEquals(DemoRoute.LanguagePreview("swift"), parseDemoHashRoute("#/lang/swift"))
        assertEquals(DemoRoute.LanguagePreview("python"), parseDemoHashRoute("#/lang/py"))
        assertEquals(DemoRoute.LanguagePreview("csharp"), parseDemoHashRoute("#/lang/cs"))
        assertEquals(DemoRoute.LanguagePreview("groovy"), parseDemoHashRoute("#/lang/gradle"))
        assertEquals(DemoRoute.LanguagePreview("groovy"), parseDemoHashRoute("#/lang/gradle.groovy"))
    }

    @Test
    fun unknownHashParsesToNotFoundRoute() {
        val route = assertIs<DemoRoute.NotFound>(parseDemoHashRoute("#/lang/swifts"))

        assertEquals("/lang/swifts", route.originalPath)
    }

    @Test
    fun githubPagesFallbackHashPreservesOriginalPath() {
        val route = assertIs<DemoRoute.NotFound>(parseDemoHashRoute("#/404/lang/swifts"))

        assertEquals("/lang/swifts", route.originalPath)
    }

    @Test
    fun routesWriteCanonicalHashes() {
        assertNull(DemoRoute.LanguageIndex.toHashRoute())
        assertEquals("#/getting-started", DemoRoute.GetStarted.toHashRoute())
        assertEquals("#/lang/diff", DemoRoute.LanguagePreview("diff").toHashRoute())
        assertEquals("#/404/lang/swifts", DemoRoute.NotFound("/lang/swifts").toHashRoute())
        assertEquals("#/404", DemoRoute.NotFound("/").toHashRoute())
    }
}
