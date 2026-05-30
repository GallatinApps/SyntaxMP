package com.gallatinapps.syntaxmp.benchmarks.fixtures.markdown

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val MarkdownPlainFixture = LanguageBenchmarkFixture(
    fixtureId = "markdown-plain",
    languageLabel = "markdown",
    displayName = "Markdown plain",
    family = LanguageBenchmarkFamily.MarkdownPlain,
    targetBodyLines = 100,
    header = """
        # Workspace Operating Notes

        This document avoids fenced code so the plain Markdown scanner path is measured on its own.
    """,
    body = """
        ## Library {{index}} Review

        The first scan should feel **instant** for small libraries and _honest_ for larger folders.
        Inline code like `ReadableTextMaxBytes` should sit beside [policy links][policy-{{index}}].
        Escaped punctuation such as \*literal asterisks\* should stay readable.

        ### Goals

        - Keep user-authored Markdown files as the source of truth.
        - Restore library grants before scanning.
        - Hash editable text files after extraction.
        - Keep image entries metadata-only.
        - Reconcile missing files without rewriting external files.

        ### Checklist

        - [x] Load the selected folder.
        - [x] Read editable Markdown and text files.
        - [ ] Skip files above the preview threshold.
        - [ ] Refresh the search rows.
        - [ ] Surface conflict hints before saving.

        1. Rebuild the tree rows.
        2. Sort recent notes by modified time.
        3. Preserve folder expansion state.
        4. Update the selected detail panel.
        5. Announce the final scan count.

        > Keep files on disk as the authority.
        > Nested details can stay in the same quote block.
        >
        > The reviewer should be able to tell whether a delay is scanning,
        > indexing, or rendering the visible rows.

        | Kind | Limit | Action |
        | :--- | ----: | :----- |
        | Markdown | 256 KB | rich edit |
        | Plain text | 2 MB | chunked preview |
        | Image | metadata | preview only |
        | Unsupported | metadata | external open |

        ### Notes

        A status phrase like "Library {{index}} is synchronized" should tokenize as normal prose.
        Emphasis can be **bold**, _italic_, or ***both*** inside the same paragraph.
        Links may point to <https://example.test/library/{{index}}> or use reference labels.
        Inline HTML is intentionally avoided in the plain fixture.
        The final paragraph keeps enough body text to look like a real document section.

        ### Decision Notes

        The scan state should distinguish files that were skipped because they are too large
        from files that failed because the platform grant was missing.
        Search indexing should write the title, path, extension, and extracted text in one pass.
        A clean file should not flash as dirty simply because its preview was opened.
        External files should remain visible in recents even when they are not inside a library.

        #### Review Prompts

        - Does the folder picker restore the previous grant?
        - Does the search result explain why a preview-only item cannot be edited?
        - Does the save conflict dialog name the file that changed on disk?
        - Does the status line use a stable phrase while the scan is still running?
        - Does the row stay keyboard-focusable after the tree refreshes?

        The reviewer should be able to scan a long note without hunting for hidden state.
        A second paragraph keeps the body closer to a normal planning document instead of a
        compressed syntax checklist.

        | Signal | Source | Owner |
        | :----- | :----- | :---- |
        | Dirty state | editor session | documents |
        | Indexed text | scan result | browser |
        | Active pane | workspace focus | workspace |
        | Export type | inspector option | export |

        > The document owns the words.
        > The database owns the index.
        > The workspace owns the visible selection.
        > None of those facts should rewrite the source file by surprise.

        ##### Final Pass

        1. Read the visible rows.
        2. Open a representative note.
        3. Toggle preview and source mode.
        4. Save a small edit.
        5. Confirm the search result updates.

        [policy-{{index}}]: https://example.test/docs/policy/{{index}} "Large document policy"
    """,
)

private val MarkdownEmbeddedFixture = LanguageBenchmarkFixture(
    fixtureId = "markdown-embedded",
    languageLabel = "markdown",
    displayName = "Markdown embedded",
    family = LanguageBenchmarkFamily.MarkdownEmbedded,
    targetBodyLines = 100,
    header = """
        # Embedded Language Review

        This document measures Markdown routing into fenced code and HTML blocks.
    """,
    body = """
        ## Embedded Section {{index}}

        The prose around the code fences should remain ordinary Markdown with `inline code`,
        **strong text**, links, block quotes, and tables around the routed regions.

        ```kotlin
        data class FixtureNote{{index}}(
            val title: String,
            val words: Int,
            val pinned: Boolean = false,
        )

        fun FixtureNote{{index}}.label(): String {
            val minutes = maxOf(1, words / 220)
            return "${'$'}title - ${'$'}minutes min"
        }
        ```

        ```typescript
        interface FixtureNote{{index}} {
          title: string;
          words: number;
          pinned?: boolean;
        }

        export function label{{index}}(note: FixtureNote{{index}}): string {
          const minutes = Math.max(1, Math.ceil(note.words / 220));
          return `${'$'}{note.title} - ${'$'}{minutes} min`;
        }
        ```

        ```sql
        WITH recent_notes_{{index}} AS (
            SELECT id, title, updated_at
            FROM library_files
            WHERE kind = 'markdown'
        )
        SELECT title, updated_at
        FROM recent_notes_{{index}}
        ORDER BY updated_at DESC
        FETCH FIRST 20 ROWS ONLY;
        ```

        ```json
        {
          "library": "release-{{index}}",
          "limits": {
            "readableBytes": 2000000,
            "enhancedEditBytes": 256000
          },
          "features": ["scan", "search", "preview"]
        }
        ```

        <details open>
          <summary>HTML block {{index}}</summary>
          <p data-state="draft">Inline <strong>markup</strong> is tokenized too.</p>
          <ul>
            <li>Route fenced code by language label.</li>
            <li>Keep prose outside code regions as Markdown.</li>
          </ul>
        </details>

        | Region | Purpose |
        | :----- | :------ |
        | Kotlin | General source comparison |
        | TypeScript | Web source comparison |
        | SQL | Query scanner comparison |
        | JSON | Structured-data comparison |

        ```css
        .fixture-note-{{index}} {
          --accent: #2563eb;
          display: grid;
          gap: 0.75rem;
          border-left: 4px solid var(--accent);
          padding: clamp(1rem, 2vw, 1.5rem);
        }

        .fixture-note-{{index}}[data-state="dirty"] {
          color: color-mix(in srgb, var(--accent), black 24%);
        }
        ```

        > Routed regions should not leak styles into neighboring Markdown.
        > The paragraphs before and after a fence should still receive Markdown roles.

        - [x] Kotlin fence routed.
        - [x] TypeScript fence routed.
        - [x] SQL fence routed.
        - [x] JSON fence routed.
        - [ ] CSS fence reviewed.
    """,
)

internal val MarkdownFixtures: List<LanguageBenchmarkFixture> =
    listOf(MarkdownPlainFixture, MarkdownEmbeddedFixture)
