package com.gallatinapps.syntaxmp.demo.model.samples

internal val ScssSample = """
    ${'$'}accent: #2563eb;
    ${'$'}surface: #f8fafc;
    ${'$'}states: (
      draft: #f59e0b,
      review: #7c3aed,
      active: #16a34a,
    );

    @mixin focus-ring(${'$'}color) {
      outline: 2px solid rgba(${'$'}color, 0.55);
      outline-offset: 3px;
    }

    @function tone(${'$'}state) {
      @return map-get(${'$'}states, ${'$'}state);
    }

    .language-row {
      background: ${'$'}surface;

      @each ${'$'}name, ${'$'}color in ${'$'}states {
        &--#{${'$'}name} {
          border-left: 3px solid ${'$'}color;
        }
      }

      &[aria-selected="true"],
      &:focus-visible {
        color: tone(active);
        @include focus-ring(${'$'}accent);
      }
    }
""".trimIndent()
