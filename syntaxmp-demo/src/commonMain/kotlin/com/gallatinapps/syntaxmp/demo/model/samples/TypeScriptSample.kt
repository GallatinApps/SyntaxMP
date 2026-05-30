package com.gallatinapps.syntaxmp.demo.model.samples

internal val TypeScriptSample = """
    type NoteStatus = "draft" | "review" | "published";

    interface NoteSummary<TMeta extends Record<string, unknown> = Record<string, never>> {
      id: string;
      title: string;
      words: number;
      status: NoteStatus;
      pinned?: boolean;
      metadata: TMeta;
    }

    export function readingMinutes(note: Pick<NoteSummary, "words">): number {
      return Math.max(1, Math.ceil(note.words / 220));
    }

    export function statusLabel(note: NoteSummary<{ owner?: string }>): string {
      const pin = note.pinned ? "Pinned" : "Normal";
      const owner = note.metadata.owner ?? "local";
      return `${'$'}{pin} • ${'$'}{note.status.toUpperCase()} • ${'$'}{owner} • ${'$'}{readingMinutes(note)} min`;
    }

    const defaultNote = {
      id: "launch",
      title: "Launch plan",
      words: 1240,
      status: "review",
      metadata: { owner: "docs" },
    } satisfies NoteSummary<{ owner: string }>;
""".trimIndent()
