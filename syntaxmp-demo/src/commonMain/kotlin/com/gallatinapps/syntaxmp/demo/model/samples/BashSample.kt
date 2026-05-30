package com.gallatinapps.syntaxmp.demo.model.samples

internal val BashSample = """
    #!/usr/bin/env bash
    set -euo pipefail

    workspace="${'$'}{WORKSPACE:-${'$'}HOME/Notes}"
    for file in "${'$'}workspace"/*.md; do
      [[ -e "${'$'}file" ]] || continue
      printf 'Indexing %s\n' "${'$'}{file##*/}"
    done
""".trimIndent()
