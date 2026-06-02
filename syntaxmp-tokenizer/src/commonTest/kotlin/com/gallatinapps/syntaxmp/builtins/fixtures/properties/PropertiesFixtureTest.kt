package com.gallatinapps.syntaxmp.builtins.fixtures.properties

import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class PropertiesFixtureTest {
    @Test
    fun `properties uses key value scanner under properties identity`() {
        val code = """
            # editor settings
            app.name = SyntaxMP
            app.enabled = true
            app.retries: 3
        """.trimIndent()

        assertTokenAt("properties", code, "# editor settings", "Comment")
        assertTokenAt("properties", code, "app.name", "Property")
        assertTokenAt("properties", code, "=", "Operator")
        assertTokenAt("properties", code, "true", "Constant")
        assertTokenAt("properties", code, "3", "Number")
    }
}
