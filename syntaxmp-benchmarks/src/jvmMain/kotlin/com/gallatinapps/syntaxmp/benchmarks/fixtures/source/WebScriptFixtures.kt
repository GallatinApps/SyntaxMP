package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val TypeScriptRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "typescript",
    languageLabel = "typescript",
    displayName = "TypeScript",
    family = LanguageBenchmarkFamily.WebScriptSource,
    targetBodyLines = 100,
    header = """
        import { createSignal, type Accessor } from "solid-js";
        import { z } from "zod";

        type QueueMode = "draft" | "review" | "published";

        interface NoteIdentity {
          id: string;
          title: string;
          path: string;
        }
    """,
    body = """
        type NoteStatus{{index}} = "draft" | "review" | "published";

        interface NoteSummary{{index}}<TMeta extends Record<string, unknown> = Record<string, never>>
          extends NoteIdentity {
          words: number;
          status: NoteStatus{{index}};
          pinned?: boolean;
          metadata: TMeta;
          tags: readonly string[];
        }

        type NoteAction{{index}} =
          | { type: "pin"; id: string; pinned: boolean }
          | { type: "rename"; id: string; title: string }
          | { type: "archive"; id: string; reason?: string };

        const NoteMetaSchema{{index}} = z.object({
          owner: z.string().optional(),
          priority: z.number().int().min(0).max(5).default(0),
          labels: z.array(z.string()).default([]),
        });

        export function readingMinutes{{index}}(note: Pick<NoteSummary{{index}}, "words">): number {
          return Math.max(1, Math.ceil(note.words / 220));
        }

        export function statusLabel{{index}}(
          note: NoteSummary{{index}}<{ owner?: string; priority?: number }>,
          mode: QueueMode = "review",
        ): string {
          const pin = note.pinned ? "Pinned" : "Normal";
          const owner = note.metadata.owner ?? "local";
          const priority = note.metadata.priority ?? 0;
          return `${'$'}{pin} / ${'$'}{mode} / ${'$'}{note.status.toUpperCase()} / ${'$'}{owner} / p${'$'}{priority}`;
        }

        export function applyAction{{index}}(
          note: NoteSummary{{index}}<{ owner?: string }>,
          action: NoteAction{{index}},
        ): NoteSummary{{index}}<{ owner?: string }> {
          switch (action.type) {
            case "pin":
              return { ...note, pinned: action.pinned };
            case "rename":
              return { ...note, title: action.title.trim() };
            case "archive":
              return { ...note, status: "published", tags: [...note.tags, action.reason ?? "archive"] };
            default:
              return action satisfies never;
          }
        }

        export class NoteQueue{{index}}<TMeta extends Record<string, unknown>> {
          readonly #notes: NoteSummary{{index}}<TMeta>[];

          constructor(notes: Iterable<NoteSummary{{index}}<TMeta>>) {
            this.#notes = Array.from(notes);
          }

          get count(): number {
            return this.#notes.length;
          }

          select(predicate: (note: NoteSummary{{index}}<TMeta>) => boolean): NoteSummary{{index}}<TMeta>[] {
            return this.#notes
              .filter(predicate)
              .sort((left, right) => Number(Boolean(right.pinned)) - Number(Boolean(left.pinned)));
          }

          summarize(accessor: Accessor<QueueMode>): string[] {
            return this.select((note) => note.status === accessor()).map((note) =>
              `${'$'}{note.title}: ${'$'}{readingMinutes{{index}}(note)} min`,
            );
          }
        }

        const defaultNote{{index}} = {
          id: "launch-{{index}}",
          title: "Launch plan {{index}}",
          path: "/notes/launch-{{index}}.md",
          words: 1240 + {{index}},
          status: "review",
          pinned: {{index}} % 2 === 0,
          metadata: { owner: "docs", priority: 2, labels: ["release", "review"] },
          tags: ["#release", "#review"],
        } satisfies NoteSummary{{index}}<z.infer<typeof NoteMetaSchema{{index}}>>;

        export function createQueueStore{{index}}() {
          const [mode, setMode] = createSignal<QueueMode>("review");
          const queue = new NoteQueue{{index}}([defaultNote{{index}}]);
          return { mode, setMode, queue };
        }
    """,
)

private val JavaScriptRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "javascript",
    languageLabel = "javascript",
    displayName = "JavaScript",
    family = LanguageBenchmarkFamily.WebScriptSource,
    targetBodyLines = 100,
    body = """
        const noteStatus{{index}} = Object.freeze({
          draft: "draft",
          review: "review",
          published: "published",
        });

        const defaultNote{{index}} = {
          id: "launch-{{index}}",
          title: "Launch plan {{index}}",
          path: "/notes/launch-{{index}}.md",
          words: 1240 + {{index}},
          status: noteStatus{{index}}.review,
          pinned: {{index}} % 2 === 0,
          metadata: { owner: "docs", priority: 2 },
          tags: ["release", "review", "benchmark"],
        };

        export function readingMinutes{{index}}(note) {
          return Math.max(1, Math.ceil((note.words ?? 0) / 220));
        }

        export function statusLabel{{index}}(note, mode = "review") {
          const pin = note.pinned ? "Pinned" : "Normal";
          const owner = note.metadata?.owner ?? "local";
          return `${'$'}{pin} / ${'$'}{mode} / ${'$'}{note.status.toUpperCase()} / ${'$'}{owner}`;
        }

        export function applyAction{{index}}(note, action) {
          switch (action.type) {
            case "pin":
              return { ...note, pinned: Boolean(action.pinned) };
            case "rename":
              return { ...note, title: action.title.trim() };
            case "archive":
              return {
                ...note,
                status: noteStatus{{index}}.published,
                tags: [...note.tags, action.reason ?? "archive"],
              };
            default:
              throw new Error(`Unknown action ${'$'}{action.type}`);
          }
        }

        export class NoteQueue{{index}} {
          #notes;

          constructor(notes = [defaultNote{{index}}]) {
            this.#notes = Array.from(notes);
          }

          get count() {
            return this.#notes.length;
          }

          select(predicate) {
            return this.#notes
              .filter(predicate)
              .sort((left, right) => Number(Boolean(right.pinned)) - Number(Boolean(left.pinned)));
          }

          summarize(mode = "review") {
            return this.select((note) => note.status === mode).map((note) => ({
              id: note.id,
              label: `${'$'}{note.title}: ${'$'}{readingMinutes{{index}}(note)} min`,
              owner: note.metadata?.owner ?? "local",
            }));
          }
        }

        export async function loadQueue{{index}}(fetchJson) {
          const response = await fetchJson(`/api/library/{{index}}/notes`);
          const queue = new NoteQueue{{index}}(response.notes ?? [defaultNote{{index}}]);
          return queue.summarize("review");
        }
    """,
)

private val JsxRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "jsx",
    languageLabel = "jsx",
    displayName = "JSX",
    family = LanguageBenchmarkFamily.WebScriptSource,
    targetBodyLines = 100,
    header = """
        import React, { useMemo, useState } from "react";
    """,
    body = """
        const notes{{index}} = [
          { id: "launch-{{index}}", title: "Launch plan", words: 1240, pinned: true, status: "review" },
          { id: "sync-{{index}}", title: "Sync notes", words: 820, pinned: false, status: "draft" },
        ];

        function readingMinutes{{index}}(note) {
          return Math.max(1, Math.ceil(note.words / 220));
        }

        function NoteCard{{index}}({ note, onOpen, onPin }) {
          const label = `${'$'}{note.title} / ${'$'}{readingMinutes{{index}}(note)} min`;

          return (
            <article className={note.pinned ? "note-card pinned" : "note-card"} data-state={note.status}>
              <header>
                <h2>{note.title}</h2>
                <button type="button" onClick={() => onPin(note.id, !note.pinned)}>
                  {note.pinned ? "Pinned" : "Pin"}
                </button>
              </header>
              <p>{label}</p>
              <ul>
                {note.tags?.map((tag) => (
                  <li key={tag}>#{tag}</li>
                ))}
              </ul>
              <button type="button" onClick={() => onOpen(note.id)}>
                Open
              </button>
            </article>
          );
        }

        export default function ReviewQueue{{index}}() {
          const [mode, setMode] = useState("review");
          const [notes, setNotes] = useState(notes{{index}});
          const visibleNotes = useMemo(
            () => notes
              .filter((note) => note.status === mode)
              .sort((left, right) => Number(right.pinned) - Number(left.pinned)),
            [mode, notes],
          );

          function pinNote(id, pinned) {
            setNotes((current) =>
              current.map((note) => note.id === id ? { ...note, pinned } : note),
            );
          }

          return (
            <section className="review-queue" data-mode={mode}>
              <header>
                <p className="eyebrow">Library {{index}}</p>
                <h1>Review queue</h1>
                <select value={mode} onChange={(event) => setMode(event.target.value)}>
                  <option value="draft">Draft</option>
                  <option value="review">Review</option>
                  <option value="published">Published</option>
                </select>
              </header>
              {visibleNotes.map((note) => (
                <NoteCard{{index}}
                  key={note.id}
                  note={note}
                  onOpen={(id) => console.log("open", id)}
                  onPin={pinNote}
                />
              ))}
              <footer>
                <small>{visibleNotes.length} notes shown</small>
              </footer>
            </section>
          );
        }
    """,
)

private val TsxRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "tsx",
    languageLabel = "tsx",
    displayName = "TSX",
    family = LanguageBenchmarkFamily.WebScriptSource,
    targetBodyLines = 100,
    header = """
        import React, { useMemo, useState } from "react";
    """,
    body = """
        type NoteStatus{{index}} = "draft" | "review" | "published";

        interface Note{{index}} {
          id: string;
          title: string;
          words: number;
          pinned: boolean;
          status: NoteStatus{{index}};
          tags: readonly string[];
        }

        const notes{{index}}: Note{{index}}[] = [
          { id: "launch-{{index}}", title: "Launch plan", words: 1240, pinned: true, status: "review", tags: ["release"] },
          { id: "sync-{{index}}", title: "Sync notes", words: 820, pinned: false, status: "draft", tags: ["sync"] },
        ];

        function readingMinutes{{index}}(note: Pick<Note{{index}}, "words">): number {
          return Math.max(1, Math.ceil(note.words / 220));
        }

        function NoteCard{{index}}(
          props: {
            note: Note{{index}};
            onOpen: (id: string) => void;
            onPin: (id: string, pinned: boolean) => void;
          },
        ) {
          const { note, onOpen, onPin } = props;
          const label = `${'$'}{note.title} / ${'$'}{readingMinutes{{index}}(note)} min`;

          return (
            <article className={note.pinned ? "note-card pinned" : "note-card"} data-state={note.status}>
              <header>
                <h2>{note.title}</h2>
                <button type="button" onClick={() => onPin(note.id, !note.pinned)}>
                  {note.pinned ? "Pinned" : "Pin"}
                </button>
              </header>
              <p>{label}</p>
              <ul>
                {note.tags.map((tag) => (
                  <li key={tag}>#{tag}</li>
                ))}
              </ul>
              <button type="button" onClick={() => onOpen(note.id)}>
                Open
              </button>
            </article>
          );
        }

        export function ReviewQueue{{index}}(): JSX.Element {
          const [mode, setMode] = useState<NoteStatus{{index}}>("review");
          const [notes, setNotes] = useState<Note{{index}}[]>(notes{{index}});
          const visibleNotes = useMemo(
            () => notes
              .filter((note) => note.status === mode)
              .sort((left, right) => Number(right.pinned) - Number(left.pinned)),
            [mode, notes],
          );

          function pinNote(id: string, pinned: boolean) {
            setNotes((current) =>
              current.map((note) => note.id === id ? { ...note, pinned } : note),
            );
          }

          return (
            <section className="review-queue" data-mode={mode}>
              <header>
                <p className="eyebrow">Library {{index}}</p>
                <h1>Review queue</h1>
                <select value={mode} onChange={(event) => setMode(event.target.value as NoteStatus{{index}})}>
                  <option value="draft">Draft</option>
                  <option value="review">Review</option>
                  <option value="published">Published</option>
                </select>
              </header>
              {visibleNotes.map((note) => (
                <NoteCard{{index}}
                  key={note.id}
                  note={note}
                  onOpen={(id) => console.log("open", id)}
                  onPin={pinNote}
                />
              ))}
              <footer>
                <small>{visibleNotes.length} notes shown</small>
              </footer>
            </section>
          );
        }
    """,
)

internal val WebScriptFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        JavaScriptRepresentativeFixture,
        TypeScriptRepresentativeFixture,
        JsxRepresentativeFixture,
        TsxRepresentativeFixture,
    )
