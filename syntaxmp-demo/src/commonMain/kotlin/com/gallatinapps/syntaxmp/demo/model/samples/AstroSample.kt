package com.gallatinapps.syntaxmp.demo.model.samples

internal val AstroSample = """
    ---
    import Layout from "../layouts/Layout.astro";
    import NoteBadge from "../components/NoteBadge.astro";

    type NoteSummary = {
      title: string;
      words: number;
      status: "draft" | "review" | "published";
      pinned?: boolean;
    };

    const notes: NoteSummary[] = [
      { title: "Launch plan", words: 1240, status: "review", pinned: true },
      { title: "Browser indexing", words: 870, status: "draft" },
      { title: "Wasm demo", words: 540, status: "published" },
    ];

    const readingMinutes = (words: number) => Math.max(1, Math.ceil(words / 220));
    const pageTitle = `SyntaxMP Demo (${'$'}{notes.length})`;
    ---

    <Layout title={pageTitle}>
      <main class="note-grid">
        {notes.map((note) => (
          <article data-pinned={note.pinned === true} data-status={note.status}>
            <h2>{note.title}</h2>
            <NoteBadge status={note.status} />
            <p>{readingMinutes(note.words)} min read</p>
          </article>
        ))}
      </main>

      <script>
        document.querySelectorAll("[data-pinned='true']").forEach((node) => {
          node.setAttribute("aria-label", "Pinned note");
        });
      </script>

      <style>
        .note-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(16rem, 1fr));
          gap: 1rem;
        }
        article[data-pinned="true"] {
          border-left: 4px solid #2563eb;
        }
      </style>
    </Layout>
""".trimIndent()
