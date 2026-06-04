package com.gallatinapps.syntaxmp.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LanguageIdTest {
    @Test
    fun builtInsContainV1LanguageSurface() {
        assertEquals(39, LanguageId.BuiltIns.size)
    }

    @Test
    fun fromStringNormalizesExactCustomLanguagesAndRejectsBlankValues() {
        assertEquals("myql", LanguageId.fromString(" MyQL ").value)
        assertFailsWith<IllegalArgumentException> {
            LanguageId.fromString(" ")
        }
    }
}
