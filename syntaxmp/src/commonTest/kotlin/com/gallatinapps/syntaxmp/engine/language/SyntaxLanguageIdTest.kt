package com.gallatinapps.syntaxmp.engine.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SyntaxLanguageIdTest {
    @Test
    fun resolveReturnsNullForNullAndBlankLabels() {
        assertNull(SyntaxLanguageId.resolve(null))
        assertNull(SyntaxLanguageId.resolve(""))
        assertNull(SyntaxLanguageId.resolve("   "))
    }

    @Test
    fun resolveNormalizesBuiltInAliasesAndFallsBackToCustomLanguages() {
        assertEquals(SyntaxLanguageId.Kotlin, SyntaxLanguageId.resolve(" KT "))
        assertEquals(SyntaxLanguageId.TypeScript, SyntaxLanguageId.resolve("Ts"))
        assertEquals(SyntaxLanguageId.DnsZone, SyntaxLanguageId.resolve("dns"))
        assertEquals(SyntaxLanguageId.Scss, SyntaxLanguageId.resolve("sass"))
        assertEquals(SyntaxLanguageId.fromString("myql"), SyntaxLanguageId.resolve(" MyQL "))
    }

    @Test
    fun fromStringNormalizesExactCustomLanguagesAndRejectsBlankValues() {
        assertEquals("myql", SyntaxLanguageId.fromString(" MyQL ").value)
        assertFailsWith<IllegalArgumentException> {
            SyntaxLanguageId.fromString(" ")
        }
    }
}
