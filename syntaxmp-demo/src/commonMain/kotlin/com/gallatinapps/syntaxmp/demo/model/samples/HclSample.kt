package com.gallatinapps.syntaxmp.demo.model.samples

internal val HclSample = """
    app "syntaxmp-demo" {
      runtime = "wasm"
      owner   = "docs"
      tags    = ["syntax", "compose", "local"]

      viewport {
        min_width = 360
        wide_at   = 920
      }

      feature "syntax-gallery" {
        enabled = true
        default_language = "kotlin"
      }

      route "language" {
        path    = "/#/lang/${'$'}{language}"
        preload = ["kotlin", "swift", "gradle"]
      }
    }
""".trimIndent()
