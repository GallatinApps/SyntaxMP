package com.gallatinapps.syntaxmp.languages.fixtures.dnszone

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class DnsZoneFixtureTest {
    private val code = """
        ${'$'}ORIGIN example.test.
        ${'$'}TTL 3600
        @ IN SOA ns1.example.test. hostmaster.example.test. 2026052001 7200 3600 1209600 3600
        @ IN A 192.0.2.10
        www IN AAAA 2001:db8::10
        @ IN MX 10 mail.example.test.
        @ IN TXT "v=spf1 ~all"
        ; MX in comment
    """.trimIndent()

    @Test
    fun `dns zone directives records classes types strings and comments`() {
        assertTokenAt("dns", code, "${'$'}ORIGIN", "Keyword")
        assertTokenAt("dns", code, "${'$'}TTL", "Keyword")
        assertTokenAt("dns", code, "3600", "Number")
        assertTokenAt("dns", code, "@", "Property")
        assertTokenAt("dns", code, "IN", "Keyword", occurrence = 1)
        assertTokenAt("dns", code, "SOA", "Type")
        assertTokenAt("dns", code, "A", "Type", occurrence = 1)
        assertTokenAt("dns", code, "AAAA", "Type")
        assertTokenAt("dns", code, "MX", "Type")
        assertTokenAt("dns", code, "TXT", "Type")
        assertTokenAt("dns", code, "\"v=spf1 ~all\"", "String")
        assertTokenAt("dns", code, "; MX in comment", "Comment")
    }

    @Test
    fun `record types inside comments are not highlighted`() = assertNoTokenAt(
        language = "dns",
        code = code,
        substring = "MX",
        category = "Type",
        occurrence = 1,
    )

    @Test
    fun `semicolons inside quoted record values are not comments`() {
        val code = """
            ${'$'}ORIGIN example.test.
            @ IN TXT "v=spf1; still text" ; real comment
        """.trimIndent()

        assertTokenAt("dns", code, "${'$'}ORIGIN", "Keyword")
        assertTokenAt("dns", code, "example.test.", "Variable")
        assertTokenAt("dns", code, "TXT", "Type")
        assertTokenAt("dns", code, "\"v=spf1; still text\"", "String")
        assertNoTokenAt("dns", code, "; still text", "Comment")
        assertTokenAt("dns", code, "; real comment", "Comment")
    }

    @Test
    fun `multiline parenthesized records keep field context until closed`() {
        val code = """
            ${'$'}ORIGIN example.com.
            @ IN SOA ns.example.com. hostmaster.example.com. (
              2025010101 ; serial
              3600       ; refresh
              900        ; retry
              1209600    ; expire
              300        ; minimum
            )
            www IN A 192.0.2.1
            @ IN TXT "semicolon; still string" ; trailing comment
        """.trimIndent()

        assertTokenAt("dns", code, "${'$'}ORIGIN", "Keyword")
        assertTokenAt("dns", code, "@", "Property")
        assertTokenAt("dns", code, "IN", "Keyword", occurrence = 1)
        assertTokenAt("dns", code, "SOA", "Type")
        assertTokenAt("dns", code, "2025010101", "Number")
        assertTokenAt("dns", code, "; serial", "Comment")
        assertTokenAt("dns", code, "3600", "Number")
        assertTokenAt("dns", code, "; refresh", "Comment")
        assertTokenAt("dns", code, "900", "Number")
        assertTokenAt("dns", code, "; retry", "Comment")
        assertTokenAt("dns", code, "1209600", "Number")
        assertTokenAt("dns", code, "; expire", "Comment")
        assertTokenAt("dns", code, "300", "Number")
        assertTokenAt("dns", code, "; minimum", "Comment")
        assertTokenAt("dns", code, "www", "Property")
        assertTokenAt("dns", code, "IN", "Keyword", occurrence = 2)
        assertTokenAt("dns", code, "A", "Type", occurrence = 1)
        assertTokenAt("dns", code, "TXT", "Type")
        assertTokenAt("dns", code, "\"semicolon; still string\"", "String")
        assertNoTokenAt("dns", code, "; still string", "Comment")
        assertTokenAt("dns", code, "; trailing comment", "Comment")
    }
}
