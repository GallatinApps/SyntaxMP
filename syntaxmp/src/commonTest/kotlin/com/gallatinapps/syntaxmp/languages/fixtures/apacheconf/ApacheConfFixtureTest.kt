package com.gallatinapps.syntaxmp.languages.fixtures.apacheconf

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ApacheConfFixtureTest {
    private val code = """
        # apache scoring
        <VirtualHost *:80>
          ServerName "example.test"
          DocumentRoot /srv/www
          <Directory "/srv/www">
            Options Indexes FollowSymLinks
            Require all granted
          </Directory>
        </VirtualHost>
    """.trimIndent()

    @Test
    fun `apache config comments blocks directives strings and arguments`() {
        assertTokenAt("apacheconf", code, "# apache scoring", "Comment")
        assertTokenAt("apacheconf", code, "<", "Punctuation")
        assertTokenAt("apacheconf", code, "VirtualHost", "Tag")
        assertTokenAt("apacheconf", code, "*:80", "Variable")
        assertTokenAt("apacheconf", code, "ServerName", "Keyword")
        assertTokenAt("apacheconf", code, "\"example.test\"", "String")
        assertTokenAt("apacheconf", code, "DocumentRoot", "Keyword")
        assertTokenAt("apacheconf", code, "Directory", "Tag")
        assertTokenAt("apacheconf", code, "Options", "Keyword")
        assertTokenAt("apacheconf", code, "Indexes", "Variable")
        assertTokenAt("apacheconf", code, "Require", "Keyword")
    }

    @Test
    fun `directives inside strings are not keywords`() = assertNoTokenAt(
        language = "apacheconf",
        code = "ServerName \"DocumentRoot\"",
        substring = "DocumentRoot",
        category = "Keyword",
    )

    @Test
    fun `quoted comment markers paths and closing sections keep their roles`() {
        val code = """
            SetEnv NOTE "value # not a comment"
            Alias /assets "/srv/www#assets"
            <Directory "/srv/www#assets">
              Require all granted
            </Directory>
        """.trimIndent()

        assertTokenAt("apacheconf", code, "SetEnv", "Keyword")
        assertTokenAt("apacheconf", code, "\"value # not a comment\"", "String")
        assertNoTokenAt("apacheconf", code, "# not a comment", "Comment")
        assertTokenAt("apacheconf", code, "Alias", "Keyword")
        assertTokenAt("apacheconf", code, "/", "Operator")
        assertTokenAt("apacheconf", code, "assets", "Variable")
        assertTokenAt("apacheconf", code, "\"/srv/www#assets\"", "String")
        assertTokenAt("apacheconf", code, "Directory", "Tag")
        assertTokenAt("apacheconf", code, "</", "Punctuation")
    }
}
