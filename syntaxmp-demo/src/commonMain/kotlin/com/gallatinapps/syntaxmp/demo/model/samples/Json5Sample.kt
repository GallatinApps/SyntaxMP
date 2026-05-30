package com.gallatinapps.syntaxmp.demo.model.samples

internal val Json5Sample = """
    {
      // Local demo flags are intentionally not synced.
      title: 'SyntaxMP demo',
      theme: 'system',
      samples: ['kotlin', 'swift', 'astro'],
      maxBundleBytes: 0x2_0000,
      trailingCommas: true,
      viewport: {
        widePaneMinWidth: 920,
        showAliasChips: false,
        breakpoints: {
          compact: 600,
          wide: 900,
        },
      },
    }
""".trimIndent()
