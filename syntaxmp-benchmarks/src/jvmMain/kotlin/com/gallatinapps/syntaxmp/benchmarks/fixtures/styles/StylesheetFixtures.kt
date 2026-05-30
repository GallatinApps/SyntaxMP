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




internal val StylesheetFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        CssRepresentativeFixture,
    )
