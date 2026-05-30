package com.gallatinapps.syntaxmp.demo.model.samples

internal val DotenvSample = """
    # Local development environment
    export API_URL=https://api.example.test
    SYNTAXMP_DEMO=true
    PREVIEW_PORT=8080
    CACHE_DIR="${'$'}HOME/.cache/syntaxmp"
""".trimIndent()
