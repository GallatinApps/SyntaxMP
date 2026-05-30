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




internal val MarkupComponentFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        HtmlRepresentativeFixture,
        XmlRepresentativeFixture,
    )
