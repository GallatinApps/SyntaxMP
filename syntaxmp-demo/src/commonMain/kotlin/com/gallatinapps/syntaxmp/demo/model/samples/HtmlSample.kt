package com.gallatinapps.syntaxmp.demo.model.samples

internal val HtmlSample = """
    <!doctype html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <title>Library Overview</title>
      <style>
        :root { --accent: #2563eb; }
        main { max-width: 72rem; margin: auto; }
        article[data-state="dirty"] { border-color: var(--accent); }
      </style>
    </head>
    <body>
      <!-- Server-rendered note summary. -->
      <main>
        <article class="note" data-state="dirty" aria-live="polite">
          <h1>Launch Plan</h1>
          <p>Three sections changed since the last save &amp; index pass.</p>
          <input name="query" value="syntax" autocomplete="off">
          <button type="button">Review changes</button>
        </article>
      </main>
      <script>
        const button = document.querySelector("button");
        button?.addEventListener("click", () => {
          const count = document.querySelectorAll("[data-state='dirty']").length;
          console.log(`open ${'$'}{count} review items`);
        });
      </script>
    </body>
    </html>
""".trimIndent()
