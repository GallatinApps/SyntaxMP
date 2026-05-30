package com.gallatinapps.syntaxmp.engine.primitives

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexemeRoleMapTest {
    @Test
    fun rejectsDuplicateLexemesInSingleMap() {
        assertFailsWith<IllegalArgumentException> {
            lexemeRoleMap(
                SyntaxRole.Keyword to setOf("when"),
                SyntaxRole.Keyword.Control to setOf("when"),
            )
        }
    }

    @Test
    fun composedMapsUseRightHandOverride() {
        val base = lexemeRoleMap(SyntaxRole.Keyword to setOf("type"))
        val override = lexemeRoleMap(SyntaxRole.Keyword.Declaration to setOf("type"))

        assertEquals(SyntaxRole.Keyword.Declaration, (base + override).getValue("type"))
    }
}
