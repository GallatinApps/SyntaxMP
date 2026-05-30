package com.gallatinapps.syntaxmp.demo.model.samples

internal val JsonSample = """
    {
      "${'$'}schema": "https://example.test/hashjot.workspace.schema.json",
      "workspace": "Research Notes",
      "version": 1,
      "sync": {
        "enabled": false,
        "lastIndexedAt": "2026-05-23T09:42:00Z",
        "remote": null
      },
      "libraries": [
        {
          "name": "Product",
          "path": "~/Notes/Product",
          "include": ["**/*.md", "**/*.txt"],
          "exclude": ["archive/**", ".hashjot/**"],
          "limits": { "readableBytes": 2000000, "enhancedEditBytes": 256000 }
        }
      ]
    }
""".trimIndent()
