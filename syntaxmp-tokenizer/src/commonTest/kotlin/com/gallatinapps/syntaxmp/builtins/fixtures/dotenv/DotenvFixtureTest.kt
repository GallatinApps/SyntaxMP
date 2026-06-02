package com.gallatinapps.syntaxmp.builtins.fixtures.dotenv

import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class DotenvFixtureTest {
    @Test
    fun `dotenv uses key value scanner under dotenv identity`() {
        val code = """
            # local environment
            export API_URL=https://example.com
            ENABLED=true
            RETRIES=3
        """.trimIndent()

        assertTokenAt("dotenv", code, "# local environment", "Comment")
        assertTokenAt("dotenv", code, "export", "Keyword")
        assertTokenAt("dotenv", code, "API_URL", "Property")
        assertTokenAt("dotenv", code, "true", "Constant")
        assertTokenAt("env", code, "3", "Number")
    }
}
