package com.gallatinapps.syntaxmp.demo.model.samples

internal val JsxSample = """
    import { useMemo, useState } from "react";

    const tone = {
      draft: "warning",
      review: "accent",
      published: "muted",
    };

    export function NoteQueue({ notes, onOpen }) {
      const [query, setQuery] = useState("");
      const visibleNotes = useMemo(
        () => notes.filter((note) =>
          note.title?.toLowerCase().includes(query.toLowerCase())
        ),
        [notes, query],
      );

      return (
        <>
          <section className={`note-queue note-queue--${'$'}{query ? "filtered" : "all"}`}>
            <label>
              Search
              <input value={query} onChange={(event) => setQuery(event.target.value)} />
            </label>
            {visibleNotes.map((note) => (
              <article
                key={note.id}
                data-tone={tone[note.status] ?? "default"}
                data-pinned={note.pinned === true}
                onClick={() => onOpen?.(note.id)}
              >
                <h2>{note.title}</h2>
                <p>{Math.max(1, Math.ceil(note.words / 220))} min read</p>
              </article>
            ))}
          </section>
        </>
      );
    }
""".trimIndent()
