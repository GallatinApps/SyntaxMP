package com.gallatinapps.syntaxmp.demo.model.samples

internal val DnsZoneSample = """
    ${'$'}ORIGIN syntaxmp.local.
    ${'$'}TTL 300
    @       IN SOA  ns1.syntaxmp.local. hostmaster.syntaxmp.local. (
                2026052301 ; serial
                3600       ; refresh
                600        ; retry
                604800     ; expire
                300 )      ; minimum
    @       IN NS   ns1.syntaxmp.local.
    @       IN MX   10 mail.syntaxmp.local.
    ns1     IN A    192.0.2.10
    ns1     IN AAAA 2001:db8::10
    mail    IN A    192.0.2.25
    demo    IN CNAME app.syntaxmp.local.
    app     IN A    192.0.2.42
    _docs   IN TXT  "hash-route=/#/getting-started"
""".trimIndent()
