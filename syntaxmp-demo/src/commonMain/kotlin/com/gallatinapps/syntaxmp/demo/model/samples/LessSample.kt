package com.gallatinapps.syntaxmp.demo.model.samples

internal val LessSample = """
    @accent: #2563eb;
    @surface: #f8fafc;
    @state: active;

    .focus-ring(@color) {
      outline: 2px solid fade(@color, 55%);
      outline-offset: 3px;
    }

    .tone(@name) when (@name = active) {
      color: mix(@accent, #111827, 72%);
    }

    .language-row {
      background: @surface;

      &--@{state} {
        border-left: 3px solid @accent;
      }

      &[aria-selected="true"],
      &:focus-visible {
        .tone(@state);
        .focus-ring(@accent);
      }
    }
""".trimIndent()
