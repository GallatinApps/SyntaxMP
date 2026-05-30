package com.gallatinapps.syntaxmp.demo.model.samples

internal val YamlSample = """
    # Local workspace settings.
    workspace: Research Notes
    owner: local
    defaults: &defaults
      sourceMode: rich
      theme: system
      description: |
        Search should stay responsive while large files are indexed.
    libraries:
      - name: Product
        path: ~/Notes/Product
        <<: *defaults
        filters:
          include:
            - "**/*.md"
            - "**/*.txt"
          exclude:
            - archive/**
            - .hashjot/**
        labels: [notes, docs, local]
    checks:
      autosaveSeconds: 4
      warnWhenLarge: true
""".trimIndent()
