package com.gallatinapps.syntaxmp.demo.model.samples

internal val TomlSample = """
    # SyntaxMP demo workspace.
    [workspace]
    name = "Research Notes"
    theme = "system"
    autosave_seconds = 4
    created_at = 2026-05-23T09:42:00Z

    [[libraries]]
    name = "Product"
    path = "~/Notes/Product"
    include = ["**/*.md", "**/*.txt"]
    exclude = ["archive/**", ".hashjot/**"]
    labels = { owner = "docs", local = true }

    [editor]
    default_source_mode = "rich"
    wrap_column = 88
    rulers = [80, 100]
""".trimIndent()
