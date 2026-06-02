package com.gallatinapps.syntaxmp.engine.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LanguageIdTest {
    @Test
    fun builtInsContainV1LanguageSurface() {
        assertEquals(39, LanguageId.BuiltIns.size)
    }

    @Test
    fun resolveReturnsNullForNullAndBlankLabels() {
        assertNull(LanguageId.resolve(null))
        assertNull(LanguageId.resolve(""))
        assertNull(LanguageId.resolve("   "))
    }

    @Test
    fun resolveNormalizesBuiltInAliasesAndFallsBackToCustomLanguages() {
        assertEquals(LanguageId.Kotlin, LanguageId.resolve(" KT "))
        assertEquals(LanguageId.TypeScript, LanguageId.resolve("Ts"))
        assertEquals(LanguageId.Properties, LanguageId.resolve("properties"))
        assertEquals(LanguageId.Dotenv, LanguageId.resolve("env"))
        assertEquals(LanguageId.Bash, LanguageId.resolve("bash"))
        assertEquals(LanguageId.Zsh, LanguageId.resolve("zsh"))
        assertEquals(LanguageId.fromString("myql"), LanguageId.resolve(" MyQL "))
    }

    @Test
    fun fromStringNormalizesExactCustomLanguagesAndRejectsBlankValues() {
        assertEquals("myql", LanguageId.fromString(" MyQL ").value)
        assertFailsWith<IllegalArgumentException> {
            LanguageId.fromString(" ")
        }
    }
}
