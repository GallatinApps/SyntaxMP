package com.gallatinapps.syntaxmp.engine.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SyntaxLanguageIdTest {
    @Test
    fun builtInsContainV1LanguageSurface() {
        assertEquals(39, SyntaxLanguageId.BuiltIns.size)
    }

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
        assertEquals(SyntaxLanguageId.Properties, SyntaxLanguageId.resolve("properties"))
        assertEquals(SyntaxLanguageId.Dotenv, SyntaxLanguageId.resolve("env"))
        assertEquals(SyntaxLanguageId.Bash, SyntaxLanguageId.resolve("bash"))
        assertEquals(SyntaxLanguageId.Zsh, SyntaxLanguageId.resolve("zsh"))
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
