package com.gallatinapps.syntaxmp.demo.model.samples

internal val ShellSample = """
    #!/usr/bin/env bash
    set -euo pipefail

    workspace="${'$'}{WORKSPACE:-${'$'}HOME/Notes}"
    query="${'$'}{1:-syntax}"
    formats=(md txt markdown)

    printf 'Searching %q for "%s"\n' "${'$'}workspace" "${'$'}query"

    find "${'$'}workspace" \( -name '*.md' -o -name '*.txt' \) -print0 |
      while IFS= read -r -d '' file; do
        ext="${'$'}{file##*.}"
        case " ${'$'}{formats[*]} " in
          *" ${'$'}ext "*) ;;
          *) continue ;;
        esac

        if grep -Eqi "${'$'}query" "${'$'}file"; then
          words="${'$'}(wc -w < "${'$'}file" | tr -d ' ')"
          printf '%s\t%s words\t%s\n' "${'$'}{file#${'$'}workspace/}" "${'$'}words" "${'$'}(date +%F)"
        fi
      done
""".trimIndent()
