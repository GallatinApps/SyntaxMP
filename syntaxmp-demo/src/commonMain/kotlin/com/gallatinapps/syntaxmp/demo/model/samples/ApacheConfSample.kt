package com.gallatinapps.syntaxmp.demo.model.samples

internal val ApacheConfSample = """
    <VirtualHost *:443>
        ServerName demo.syntaxmp.local
        ServerAlias www.demo.syntaxmp.local
        DocumentRoot "/var/www/syntaxmp"

        SSLEngine on
        Header always set X-Frame-Options "DENY"
        Header set Content-Security-Policy "default-src 'self'; script-src 'self' 'wasm-unsafe-eval'"

        SetEnvIf Request_URI "^/#/lang/" syntax_route=1

        <Directory "/var/www/syntaxmp">
            Options -Indexes +FollowSymLinks
            AllowOverride None
            Require all granted
        </Directory>

        ErrorDocument 404 /404.html
        CustomLog logs/syntaxmp_access.log combined env=syntax_route
    </VirtualHost>
""".trimIndent()
