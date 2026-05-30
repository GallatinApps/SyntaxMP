package com.gallatinapps.syntaxmp.demo.model.samples

internal val ZshSample = """
    #!/usr/bin/env zsh
    autoload -Uz compinit
    compinit

    notes_dir="${'$'}{NOTES_DIR:-${'$'}HOME/Notes}"
    if [[ -d "${'$'}notes_dir" ]]; then
      print -r -- "Notes: ${'$'}notes_dir"
    fi
""".trimIndent()
