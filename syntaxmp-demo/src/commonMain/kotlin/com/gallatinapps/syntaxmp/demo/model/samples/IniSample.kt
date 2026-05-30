package com.gallatinapps.syntaxmp.demo.model.samples

internal val IniSample = """
    ; Local workspace preferences.
    [workspace]
    name = Research Notes
    theme = system
    autosave_seconds = 4
    enabled = true

    [library "Product"]
    path = ~/Notes/Product
    include = **/*.md
    include = **/*.txt
    exclude = .hashjot/**

    [editor]
    source_mode = rich
    wrap_column = 88
    status_template = ${'$'}{title} - ${'$'}{words} words
""".trimIndent()
