package com.gallatinapps.syntaxmp.demo.model.samples

internal val CssSample = """
    :root {
      color-scheme: light dark;
      --accent: #2563eb;
      --surface: #f8fafc;
      --rail-width: clamp(16rem, 22vw, 20rem);
    }

    @media (prefers-color-scheme: dark) {
      :root {
        --accent: oklch(0.78 0.16 72);
        --surface: #111827;
      }
    }

    .note-card,
    article[data-state="dirty"] {
      display: grid;
      gap: 0.75rem;
      border-left: 4px solid var(--accent);
      inline-size: min(100%, calc(var(--rail-width) * 2));
      padding: clamp(1rem, 2vw, 1.5rem) !important;
    }

    .note-card:has(.status[aria-live="polite"]) .status::before {
      content: attr(data-count) " changes";
      color: color-mix(in srgb, var(--accent), #111827 30%);
      font-weight: 650;
    }
""".trimIndent()
