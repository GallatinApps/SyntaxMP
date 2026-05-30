package com.gallatinapps.syntaxmp.benchmarks.fixtures.styles

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val CssRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "css",
    languageLabel = "css",
    displayName = "CSS",
    family = LanguageBenchmarkFamily.Stylesheet,
    targetBodyLines = 90,
    header = """
        :root {
          color-scheme: light dark;
          --accent: #2563eb;
          --surface: #f8fafc;
          --rail-width: clamp(16rem, 22vw, 20rem);
        }
    """,
    body = """
        @layer library-{{index}} {
          .workspace-shell-{{index}} {
            display: grid;
            grid-template-columns: minmax(16rem, var(--rail-width)) minmax(0, 1fr);
            min-block-size: 100vh;
            background: var(--surface);
          }

          .note-card-{{index}},
          article[data-state="dirty"][data-library="library-{{index}}"] {
            display: grid;
            gap: 0.75rem;
            border-left: 4px solid var(--accent);
            inline-size: min(100%, calc(var(--rail-width) * 2));
            padding: clamp(1rem, 2vw, 1.5rem) !important;
          }

          .note-card-{{index}}:has(.status[aria-live="polite"]) .status::before {
            content: attr(data-count) " changes";
            color: color-mix(in srgb, var(--accent), #111827 30%);
            font-weight: 650;
          }

          .note-card-{{index}} > header {
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            border-block-end: 1px solid color-mix(in srgb, currentColor, transparent 82%);
          }

          .note-card-{{index}} [data-priority="high"] {
            --accent: oklch(0.68 0.19 38);
            text-decoration: underline;
            text-decoration-thickness: 0.12em;
          }

          @media (prefers-color-scheme: dark) {
            .note-card-{{index}} {
              --surface: #111827;
              --accent: oklch(0.78 0.16 72);
              box-shadow: 0 0 0 1px rgb(255 255 255 / 0.08);
            }
          }

          @container notes (inline-size > 42rem) {
            .note-card-{{index}} {
              grid-template-columns: 1fr auto;
              align-items: start;
            }
          }

          @supports selector(:has(*)) {
            .note-card-{{index}}:has(input:focus-visible) {
              outline: 2px solid var(--accent);
              outline-offset: 3px;
            }
          }

          @keyframes pulse-{{index}} {
            from {
              opacity: 0.72;
              transform: translateY(0);
            }
            50% {
              opacity: 1;
              transform: translateY(-1px);
            }
            to {
              opacity: 0.72;
              transform: translateY(0);
            }
          }
        }
    """,
)

private val ScssRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "scss",
    languageLabel = "scss",
    displayName = "SCSS",
    family = LanguageBenchmarkFamily.Stylesheet,
    targetBodyLines = 90,
    body = """
        ${'$'}accent-{{index}}: #2563eb;
        ${'$'}surface-{{index}}: #f8fafc;
        ${'$'}spacing-{{index}}: 0.75rem;

        @mixin focus-ring-{{index}}(${'$'}color) {
          outline: 2px solid ${'$'}color;
          outline-offset: 3px;
        }

        @function card-shadow-{{index}}(${'$'}alpha) {
          @return 0 8px 24px rgb(15 23 42 / ${'$'}alpha);
        }

        .workspace-shell-{{index}} {
          display: grid;
          grid-template-columns: minmax(16rem, 22rem) minmax(0, 1fr);
          min-block-size: 100vh;
          background: ${'$'}surface-{{index}};

          &__rail {
            border-inline-end: 1px solid color-mix(in srgb, currentColor, transparent 86%);
            padding: ${'$'}spacing-{{index}};
          }

          &__content {
            container-type: inline-size;
            padding: clamp(1rem, 2vw, 2rem);
          }
        }

        .note-card-{{index}} {
          display: grid;
          gap: ${'$'}spacing-{{index}};
          border-left: 4px solid ${'$'}accent-{{index}};
          box-shadow: card-shadow-{{index}}(0.12);
          padding: clamp(1rem, 2vw, 1.5rem);

          &[data-state="dirty"] {
            border-color: oklch(0.68 0.19 38);
          }

          > header {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
          }

          .status {
            font-weight: 650;

            &::before {
              content: attr(data-count) " changes";
              color: color-mix(in srgb, ${'$'}accent-{{index}}, black 24%);
            }
          }

          &:has(input:focus-visible) {
            @include focus-ring-{{index}}(${'$'}accent-{{index}});
          }
        }

        @media (prefers-color-scheme: dark) {
          .workspace-shell-{{index}} {
            background: #111827;
          }

          .note-card-{{index}} {
            color: #e5e7eb;
            box-shadow: card-shadow-{{index}}(0.36);
          }
        }
    """,
)

private val LessRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "less",
    languageLabel = "less",
    displayName = "Less",
    family = LanguageBenchmarkFamily.Stylesheet,
    targetBodyLines = 90,
    body = """
        @accent-{{index}}: #2563eb;
        @surface-{{index}}: #f8fafc;
        @spacing-{{index}}: 0.75rem;

        .focus-ring-{{index}}(@color) {
          outline: 2px solid @color;
          outline-offset: 3px;
        }

        .card-shadow-{{index}}(@alpha) {
          box-shadow: 0 8px 24px rgb(15 23 42 / @alpha);
        }

        .workspace-shell-{{index}} {
          display: grid;
          grid-template-columns: minmax(16rem, 22rem) minmax(0, 1fr);
          min-block-size: 100vh;
          background: @surface-{{index}};

          &__rail {
            border-inline-end: 1px solid fade(#111827, 14%);
            padding: @spacing-{{index}};
          }

          &__content {
            container-type: inline-size;
            padding: clamp(1rem, 2vw, 2rem);
          }
        }

        .note-card-{{index}} {
          display: grid;
          gap: @spacing-{{index}};
          border-left: 4px solid @accent-{{index}};
          padding: clamp(1rem, 2vw, 1.5rem);
          .card-shadow-{{index}}(0.12);

          &[data-state="dirty"] {
            border-color: #b45309;
          }

          > header {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
          }

          .status {
            font-weight: 650;

            &::before {
              content: attr(data-count) " changes";
              color: mix(@accent-{{index}}, #111827, 70%);
            }
          }

          &:has(input:focus-visible) {
            .focus-ring-{{index}}(@accent-{{index}});
          }
        }

        @media (prefers-color-scheme: dark) {
          .workspace-shell-{{index}} {
            background: #111827;
          }

          .note-card-{{index}} {
            color: #e5e7eb;
            .card-shadow-{{index}}(0.36);
          }
        }
    """,
)

private val GlslRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "glsl",
    languageLabel = "glsl",
    displayName = "GLSL",
    family = LanguageBenchmarkFamily.Stylesheet,
    targetBodyLines = 90,
    body = """
        #version 300 es
        precision highp float;

        layout(location = 0) in vec3 a_position_{{index}};
        layout(location = 1) in vec2 a_uv_{{index}};

        uniform mat4 u_model_{{index}};
        uniform mat4 u_view_{{index}};
        uniform mat4 u_projection_{{index}};
        uniform float u_time_{{index}};
        uniform vec3 u_accent_{{index}};

        out vec2 v_uv_{{index}};
        out vec3 v_color_{{index}};

        struct Light{{index}} {
          vec3 direction;
          vec3 color;
          float intensity;
        };

        uniform Light{{index}} u_key_light_{{index}};

        float hash_{{index}}(vec2 value) {
          return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453123);
        }

        float noise_{{index}}(vec2 value) {
          vec2 integerPart = floor(value);
          vec2 fractionPart = fract(value);
          float a = hash_{{index}}(integerPart);
          float b = hash_{{index}}(integerPart + vec2(1.0, 0.0));
          float c = hash_{{index}}(integerPart + vec2(0.0, 1.0));
          float d = hash_{{index}}(integerPart + vec2(1.0, 1.0));
          vec2 smoothValue = fractionPart * fractionPart * (3.0 - 2.0 * fractionPart);
          return mix(a, b, smoothValue.x)
            + (c - a) * smoothValue.y * (1.0 - smoothValue.x)
            + (d - b) * smoothValue.x * smoothValue.y;
        }

        vec3 palette_{{index}}(float amount) {
          vec3 base = vec3(0.08, 0.12, 0.18);
          vec3 highlight = normalize(u_accent_{{index}} + vec3(0.1, 0.2, 0.3));
          return mix(base, highlight, smoothstep(0.2, 0.9, amount));
        }

        vec3 apply_light_{{index}}(vec3 normal, vec3 color) {
          float diffuse = max(dot(normalize(normal), normalize(u_key_light_{{index}}.direction)), 0.0);
          return color * (0.35 + diffuse * u_key_light_{{index}}.intensity) * u_key_light_{{index}}.color;
        }

        vec2 warp_uv_{{index}}(vec2 uv) {
          float ripple = sin((uv.x + uv.y + u_time_{{index}}) * 6.28318) * 0.01;
          return uv + vec2(ripple, -ripple);
        }

        void main() {
          vec3 position = a_position_{{index}};
          vec2 warped_uv = warp_uv_{{index}}(a_uv_{{index}});
          float wave = noise_{{index}}(warped_uv * 8.0 + u_time_{{index}} * 0.1);
          position.z += wave * 0.08;
          v_uv_{{index}} = a_uv_{{index}};
          v_color_{{index}} = apply_light_{{index}}(vec3(0.0, 0.0, 1.0), palette_{{index}}(wave));
          gl_Position = u_projection_{{index}} * u_view_{{index}} * u_model_{{index}} * vec4(position, 1.0);
        }
    """,
)

internal val StylesheetFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        CssRepresentativeFixture,
        ScssRepresentativeFixture,
        LessRepresentativeFixture,
        GlslRepresentativeFixture,
    )
