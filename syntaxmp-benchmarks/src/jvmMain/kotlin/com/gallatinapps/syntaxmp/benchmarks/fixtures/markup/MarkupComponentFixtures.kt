package com.gallatinapps.syntaxmp.benchmarks.fixtures.markup

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val HtmlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "html",
    languageLabel = "html",
    displayName = "HTML",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 90,
    header = """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Workspace Overview</title>
          <style>
            :root { --accent: #2563eb; --surface: #f8fafc; }
            body { margin: 0; font-family: system-ui, sans-serif; background: var(--surface); }
            main { max-width: 72rem; margin-inline: auto; padding: 2rem; }
            article[data-state="dirty"] { border-color: var(--accent); }
          </style>
        </head>
        <body>
          <main aria-labelledby="page-title">
            <h1 id="page-title">Workspace Overview</h1>
    """,
    body = """
            <section class="library-section" data-library="library-{{index}}">
              <header>
                <p class="eyebrow">Library {{index}}</p>
                <h2>Release Notes {{index}}</h2>
                <p>Three sections changed since the last save and indexing pass.</p>
              </header>
              <article class="note-card" data-state="dirty" aria-live="polite">
                <header>
                  <h3>Launch Plan {{index}}</h3>
                  <p>
                    <strong>Owner:</strong>
                    <a href="/people/docs-{{index}}" data-owner="docs">Documentation</a>
                  </p>
                </header>
                <form action="/search" method="get">
                  <label for="query-{{index}}">Search notes</label>
                  <input
                    id="query-{{index}}"
                    name="query"
                    value="syntax {{index}}"
                    autocomplete="off"
                    data-filter="dirty">
                  <button type="button" data-action="review">Review changes</button>
                </form>
                <details open>
                  <summary>Index details</summary>
                  <table>
                    <caption>Recent file changes</caption>
                    <thead>
                      <tr>
                        <th scope="col">File</th>
                        <th scope="col">Words</th>
                        <th scope="col">State</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <th scope="row">launch-{{index}}.md</th>
                        <td>1240</td>
                        <td><mark>review</mark></td>
                      </tr>
                      <tr>
                        <th scope="row">sync-{{index}}.md</th>
                        <td>820</td>
                        <td>draft</td>
                      </tr>
                    </tbody>
                  </table>
                </details>
                <template id="note-template-{{index}}">
                  <article class="note-card" data-state="clean">
                    <h4>Template card</h4>
                    <p>Server-rendered fallback content.</p>
                  </article>
                </template>
                <aside aria-label="Review queue {{index}}">
                  <nav>
                    <a href="#query-{{index}}">Search</a>
                    <a href="/library/{{index}}/recent">Recent</a>
                    <a href="/library/{{index}}/settings">Settings</a>
                  </nav>
                  <ol>
                    <li data-priority="high">Resolve save conflict warnings.</li>
                    <li data-priority="medium">Refresh the search index.</li>
                    <li data-priority="low">Export the release packet.</li>
                  </ol>
                </aside>
              </article>
            </section>
    """,
    footer = """
          </main>
          <script type="module">
            const buttons = document.querySelectorAll("[data-action='review']");
            for (const button of buttons) {
              button.addEventListener("click", () => {
                const count = document.querySelectorAll("[data-state='dirty']").length;
                console.log(`open ${'$'}{count} review items`);
              });
            }
          </script>
        </body>
        </html>
    """,
)

private val XmlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "xml",
    languageLabel = "xml",
    displayName = "XML",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 90,
    header = """
        <?xml version="1.0" encoding="UTF-8"?>
        <workspace xmlns="https://example.test/hashjot/workspace"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="https://example.test/hashjot/workspace workspace.xsd">
    """,
    body = """
          <library id="library-{{index}}" enabled="true">
            <name>Library {{index}}</name>
            <rootPath>~/Notes/Library{{index}}</rootPath>
            <display accent="#2563eb" icon="folder" collapsed="false" />
            <scan followSymlinks="false" maxDepth="8">
              <include pattern="**/*.md" />
              <include pattern="**/*.markdown" />
              <include pattern="**/*.txt" />
              <include pattern="projects/**/*.json" />
              <exclude pattern="archive/**" />
              <exclude pattern=".hashjot/**" />
              <exclude pattern="exports/tmp/**" />
            </scan>
            <limits readableBytes="2000000"
                    enhancedEditBytes="256000"
                    metadataOnlyBytes="50000000" />
            <search enabled="true" indexCodeBlocks="true">
              <weight field="title" value="3.0" />
              <weight field="path" value="1.25" />
              <weight field="body" value="1.0" />
              <weight field="tags" value="1.5" />
            </search>
            <recent maxItems="40" includeExternal="true">
              <sort field="pinned" direction="desc" />
              <sort field="modifiedAt" direction="desc" />
            </recent>
            <workflows>
              <workflow id="scan-{{index}}" trigger="manual">
                <step>restore-grants</step>
                <step>enumerate-files</step>
                <step>extract-text</step>
                <step>write-index</step>
              </workflow>
              <workflow id="export-{{index}}" trigger="on-demand">
                <step>load-session</step>
                <step>render-artifact</step>
                <step>show-save-panel</step>
              </workflow>
            </workflows>
            <permissions>
              <grant platform="desktop" restored="true" />
              <grant platform="android" restored="false" />
              <grant platform="ios" restored="false" />
            </permissions>
            <recent>
              <file path="notes/launch-{{index}}.md" words="1240" status="review" />
              <file path="notes/sync-{{index}}.md" words="820" status="draft" />
              <file path="notes/export-{{index}}.md" words="560" status="published" />
            </recent>
            <actions>
              <action id="open" label="Open note" />
              <action id="pin" label="Pin note" />
              <action id="export" label="Export note" />
            </actions>
            <audit createdBy="benchmark" createdAt="2026-05-28T10:00:00Z">
              <event type="scan" result="ok" />
              <event type="index" result="ok" />
            </audit>
            <notes><![CDATA[
              Keep user-authored Markdown files as the source of truth.
              XML CDATA keeps punctuation like <, >, and & readable here.
            ]]></notes>
          </library>
    """,
    footer = """
        </workspace>
    """,
)

private val MdxRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "mdx",
    languageLabel = "mdx",
    displayName = "MDX",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 90,
    header = """
        import { Callout, Chart, NoteCard } from "./components";

        export const metadata = {
          title: "Library Review",
          owner: "docs",
        };

        # Library Review
    """,
    body = """
        ## Release slice {{index}}

        The prose should remain Markdown while JSX islands and fenced code route separately.

        <Callout tone="info" title="Scan status {{index}}">
          The library scan keeps user-authored files as the source of truth.
        </Callout>

        <NoteCard
          id="note-{{index}}"
          title="Launch plan {{index}}"
          words={1240 + {{index}}}
          pinned
          tags={["release", "review", "benchmark"]}
        />

        ```kotlin
        data class ReviewNote{{index}}(
            val title: String,
            val words: Int,
            val pinned: Boolean = false,
        )

        fun ReviewNote{{index}}.label(): String {
            val minutes = maxOf(1, words / 220)
            return "${'$'}title - ${'$'}minutes min"
        }
        ```

        <Chart
          title="Review throughput {{index}}"
          data={[
            { label: "Draft", value: 12 },
            { label: "Review", value: 8 },
            { label: "Published", value: 4 },
          ]}
        />

        <section data-state="dirty">
          <h3>Embedded HTML {{index}}</h3>
          <p>HTML blocks should not swallow following Markdown content.</p>
        </section>

        | Region | Expected route |
        | :----- | :------------- |
        | Markdown | prose scanner |
        | JSX | component scanner |
        | Kotlin | fenced code |
        | HTML | markup scanner |

        - [x] Preserve headings.
        - [x] Highlight JSX attributes.
        - [ ] Compare against plain Markdown.

        ```json
        {
          "library": "release-{{index}}",
          "status": "review",
          "limits": {
            "readableBytes": 2000000,
            "enhancedEditBytes": 256000
          }
        }
        ```

        <Callout tone="warning" title="Follow-up {{index}}">
          Re-run the language matrix before deleting older generated rows.
        </Callout>
    """,
)

private val VueRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "vue",
    languageLabel = "vue",
    displayName = "Vue",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 90,
    body = """
        <template>
          <section class="library-panel-{{index}}" :data-state="state">
            <header>
              <p class="eyebrow">Library {{index}}</p>
              <h1>{{ title }}</h1>
              <button type="button" @click="togglePinned">
                {{ pinned ? "Pinned" : "Pin" }}
              </button>
            </header>
            <NoteCard
              v-for="note in sortedNotes"
              :key="note.id"
              :title="note.title"
              :words="note.words"
              :pinned="note.pinned"
              @open="openNote(note.id)"
            />
            <footer v-if="sortedNotes.length === 0">
              No notes are ready for review.
            </footer>
          </section>
        </template>

        <script setup lang="ts">
        import { computed, ref } from "vue";
        import NoteCard from "./NoteCard.vue";

        type NoteStatus{{index}} = "draft" | "review" | "published";

        interface Note{{index}} {
          id: string;
          title: string;
          words: number;
          pinned: boolean;
          status: NoteStatus{{index}};
        }

        const title = "Review Queue {{index}}";
        const state = ref<NoteStatus{{index}}>("review");
        const pinned = ref(false);
        const notes = ref<Note{{index}}[]>([
          { id: "launch-{{index}}", title: "Launch plan", words: 1240, pinned: true, status: "review" },
          { id: "sync-{{index}}", title: "Sync notes", words: 820, pinned: false, status: "draft" },
        ]);

        const sortedNotes = computed(() =>
          notes.value
            .filter((note) => note.status === state.value)
            .sort((left, right) => Number(right.pinned) - Number(left.pinned)),
        );

        function togglePinned() {
          pinned.value = !pinned.value;
        }

        function openNote(id: string) {
          console.log(`open note ${'$'}{id}`);
        }
        </script>

        <style scoped>
        .library-panel-{{index}} {
          display: grid;
          gap: 0.75rem;
          padding: 1rem;
        }

        .library-panel-{{index}}[data-state="review"] {
          border-left: 4px solid #2563eb;
        }
        </style>
    """,
)

private val SvelteRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "svelte",
    languageLabel = "svelte",
    displayName = "Svelte",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 90,
    header = """
        <script lang="ts">
          type NoteStatus = "draft" | "review" | "published";

          interface Note {
            id: string;
            title: string;
            words: number;
            pinned: boolean;
            status: NoteStatus;
            tags: string[];
          }

          let state: NoteStatus = "review";

          function readingMinutes(note: Note): number {
            return Math.max(1, Math.ceil(note.words / 220));
          }

          function openNote(id: string) {
            console.log(`open note ${'$'}{id}`);
          }
        </script>
    """,
    body = """
        <section class="library-panel-{{index}}" data-state={state}>
          <header>
            <p class="eyebrow">Library {{index}}</p>
            <h1>Review Queue {{index}}</h1>
            <p>Local component state filters this repeated review panel.</p>
          </header>

          <div class="toolbar" role="group" aria-label="Queue filters">
            <select bind:value={state} aria-label="Queue mode">
              <option value="draft">Draft</option>
              <option value="review">Review</option>
              <option value="published">Published</option>
            </select>
            <button type="button" on:click={() => openNote("launch-{{index}}")}>Open first note</button>
          </div>

          {#each [
            {
              id: "launch-{{index}}",
              title: "Launch plan",
              words: 1240,
              pinned: true,
              status: "review",
              tags: ["release", "review"],
            },
            {
              id: "sync-{{index}}",
              title: "Sync notes",
              words: 820,
              pinned: false,
              status: "draft",
              tags: ["sync", "offline"],
            },
          ]
            .filter((note) => note.status === state)
            .sort((left, right) => Number(right.pinned) - Number(left.pinned)) as note (note.id)}
            <article class:pinned={note.pinned}>
              <header>
                <h2>{note.title}</h2>
                <span>{note.status}</span>
              </header>
              <p>{readingMinutes(note)} min</p>
              <ul>
                {#each note.tags as tag}
                  <li>#{tag}</li>
                {/each}
              </ul>
              <button type="button" on:click={() => openNote(note.id)}>Open</button>
            </article>
          {:else}
            <p>No notes are ready for review.</p>
          {/each}

          <aside class="handoff">
            <h3>Review handoff</h3>
            <p>Component markup, event bindings, keyed loops, and class directives stay in the repeated body.</p>
            <dl>
              <dt>Owner</dt>
              <dd>Documents team</dd>
              <dt>Mode</dt>
              <dd>{state}</dd>
            </dl>
          </aside>
        </section>
    """,
    footer = """
        <style>
          .library-panel {
            display: grid;
            gap: 0.75rem;
            padding: 1rem;
          }

          article.pinned {
            border-left: 4px solid #2563eb;
          }
        </style>
    """,
)

private val AstroRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "astro",
    languageLabel = "astro",
    displayName = "Astro",
    family = LanguageBenchmarkFamily.MarkupComponent,
    targetBodyLines = 75,
    header = """
        ---
        import NoteCard from "../components/NoteCard.astro";

        type NoteStatus = "draft" | "review" | "published";

        interface Note {
          id: string;
          title: string;
          words: number;
          pinned: boolean;
          status: NoteStatus;
          tags: string[];
        }

        const queueMode: NoteStatus = "review";
        ---
    """,
    body = """
        <section class="library-panel-{{index}}" data-state="review">
          <header>
            <p class="eyebrow">Library {{index}}</p>
            <h1>Review Queue {{index}}</h1>
            <p>Repeated Astro sections keep frontmatter at the top of the file.</p>
          </header>

          {[
            {
              id: "launch-{{index}}",
              title: "Launch plan",
              words: 1240,
              pinned: true,
              status: queueMode,
              tags: ["release", "review"],
            },
            {
              id: "sync-{{index}}",
              title: "Sync notes",
              words: 820,
              pinned: false,
              status: "draft",
              tags: ["sync", "offline"],
            },
          ]
            .filter((note) => note.status === queueMode)
            .sort((left, right) => Number(right.pinned) - Number(left.pinned))
            .map((note) => (
            <NoteCard
              id={note.id}
              title={note.title}
              words={note.words}
              pinned={note.pinned}
              tags={note.tags}
              client:visible
            />
          ))}

          <details open>
            <summary>Scan notes</summary>
            <p>Astro frontmatter, markup, expressions, and styles share this fixture.</p>
          </details>

          <aside class="handoff">
            <h2>Review handoff</h2>
            <p>Template expressions, component directives, and ordinary markup stay in the repeated body.</p>
            <dl>
              <dt>Owner</dt>
              <dd>Documents team</dd>
              <dt>Mode</dt>
              <dd>{queueMode}</dd>
            </dl>
          </aside>
        </section>
    """,
    footer = """
        <script>
          document.querySelectorAll(".library-panel").forEach((panel) => {
            panel.addEventListener("click", (event) => {
              console.log("clicked", event.target);
            });
          });
        </script>

        <style>
          .library-panel {
            display: grid;
            gap: 0.75rem;
            padding: 1rem;
            border-left: 4px solid #2563eb;
          }

          .eyebrow {
            text-transform: uppercase;
            letter-spacing: 0.08em;
          }

          .handoff {
            border-block-start: 1px solid currentColor;
            padding-block-start: 0.75rem;
          }
        </style>
    """,
)

internal val MarkupComponentFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        HtmlRepresentativeFixture,
        XmlRepresentativeFixture,
        MdxRepresentativeFixture,
        VueRepresentativeFixture,
        SvelteRepresentativeFixture,
        AstroRepresentativeFixture,
    )
