package com.gallatinapps.syntaxmp.demo.model.samples

internal val TsxSample = """
    import { useMemo, useState } from "react";

    type Status = "draft" | "review" | "published";

    type NoteSummary = {
      id: string;
      title: string;
      words: number;
      status: Status;
      pinned?: boolean;
    };

    type NoteQueueProps<T extends NoteSummary> = {
      notes: readonly T[];
      onOpen?: (id: T["id"]) => void;
    };

    export function NoteQueue<T extends NoteSummary>({ notes, onOpen }: NoteQueueProps<T>) {
      const [query, setQuery] = useState("");
      const visibleNotes = useMemo(
        () => notes.filter((note) =>
          note.title.toLowerCase().includes(query.toLowerCase())
        ),
        [notes, query],
      );

      return (
        <section className="note-queue">
          <input
            aria-label="Search notes"
            value={query}
            onChange={(event) => setQuery(event.currentTarget.value)}
          />
          {visibleNotes.map((note) => (
            <article
              key={note.id}
              data-status={note.status}
              data-pinned={note.pinned ?? false}
              onClick={() => onOpen?.(note.id)}
            >
              <h2>{note.title}</h2>
              <p>{`${'$'}{Math.max(1, Math.ceil(note.words / 220))} min read`}</p>
            </article>
          ))}
        </section>
      );
    }
""".trimIndent()
