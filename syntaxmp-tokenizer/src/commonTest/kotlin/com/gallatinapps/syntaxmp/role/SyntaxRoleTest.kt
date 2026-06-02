package com.gallatinapps.syntaxmp.role

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class SyntaxRoleTest {
    @Test
    fun knownRolesCompareByValueWithFactoryRoles() {
        assertEquals(SyntaxRole.Keyword, SyntaxRole.of("keyword"))
        assertEquals(SyntaxRole.Keyword.Declaration, SyntaxRole.of("keyword.declaration"))
        assertEquals(SyntaxRole.Variable.Parameter, SyntaxRole.of("variable.parameter"))
    }

    @Test
    fun customRolesUseValueEqualityWithoutReferenceEquality() {
        val first = SyntaxRole.of("custom.path")
        val second = SyntaxRole.of("custom.path")

        assertEquals(first, second)
        assertNotSame(first, second)
    }

    @Test
    fun appendNormalizesOuterDotsAndSupportsNestedSuffixes() {
        val role = SyntaxRole.Keyword.append(".control.if.").append("branch")

        assertEquals("keyword.control.if.branch", role.value)
    }

    @Test
    fun roleFactoryAndAppendRejectInvalidPaths() {
        assertFailsWith<IllegalArgumentException> {
            SyntaxRole.of(" ")
        }
        assertFailsWith<IllegalArgumentException> {
            SyntaxRole.of("keyword..control")
        }
        assertFailsWith<IllegalArgumentException> {
            SyntaxRole.Keyword.append("control if")
        }
    }

    @Test
    fun parentsAreReturnedFromRootThroughExactRole() {
        assertEquals(
            listOf(
                SyntaxRole.Constant,
                SyntaxRole.Constant.Builtin,
                SyntaxRole.Constant.Builtin.append("true"),
            ),
            SyntaxRole.Constant.Builtin.append("true").rolesFromRoot(),
        )
    }
}
