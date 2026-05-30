package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine

internal object LargeFileTokenizationBenchmarks {
    private val defaultEngine = SyntaxTokenizerEngine()
    private val targetSize = BenchmarkTargetSize.HighlightThreshold

    fun cases(): List<BenchmarkCase> =
        samples().map { sample ->
            BenchmarkCase(
                id = sample.caseId,
                group = "Large editor diagnostics",
                name = sample.name,
                workloadKind = sample.workloadKind,
                sizeName = sample.sizeName,
                inputChars = sample.chars,
                inputLines = sample.lines,
                warmupIterations = 1,
                measuredIterations = 3,
            ) {
                val spans = defaultEngine.tokenize(
                    code = sample.code,
                    languageLabel = sample.languageLabel,
                )
                BenchmarkIteration(
                    spanCount = spans.size,
                    checksum = checksumTokenSpans(sample.code, spans),
                )
            }
        }

    private fun samples(): List<SourceSample> =
        listOf(
            markdownEmbedded(),
            mdxMixed(),
            tsxComponents(),
            sqlMigration(),
            postgresqlMigration(),
            sqliteMigration(),
            jsonStructured(),
            htmlRawText(),
            xmlConfig(),
        )

    private fun markdownEmbedded(): SourceSample =
        largeEditorSample(
            id = "markdown-embedded",
            name = "Markdown embedded highlight threshold",
            languageLabel = "markdown",
            code = markdownEmbeddedDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxMixed(): SourceSample =
        largeEditorSample(
            id = "mdx-mixed",
            name = "MDX mixed highlight threshold",
            languageLabel = "mdx",
            code = mdxMixedDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun tsxComponents(): SourceSample =
        largeEditorSample(
            id = "tsx-components",
            name = "TSX components highlight threshold",
            languageLabel = "tsx",
            code = tsxComponentDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun sqlMigration(): SourceSample =
        largeEditorSample(
            id = "sql-migration",
            name = "SQL migration highlight threshold",
            languageLabel = "sql",
            code = sqlMigrationDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    private fun postgresqlMigration(): SourceSample =
        largeEditorSample(
            id = "postgresql-migration",
            name = "PostgreSQL migration highlight threshold",
            languageLabel = "postgresql",
            code = postgresqlMigrationDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    private fun sqliteMigration(): SourceSample =
        largeEditorSample(
            id = "sqlite-migration",
            name = "SQLite migration highlight threshold",
            languageLabel = "sqlite",
            code = sqliteMigrationDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    private fun jsonStructured(): SourceSample =
        BenchmarkSamplesFactory.jsonStructured(targetSize).copy(
            caseId = largeEditorCaseId("json-structured"),
            name = "JSON structured highlight threshold",
            sizeName = targetSize.displayName,
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    private fun htmlRawText(): SourceSample =
        largeEditorSample(
            id = "html-raw-text",
            name = "HTML raw-text highlight threshold",
            languageLabel = "html",
            code = htmlRawTextDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun xmlConfig(): SourceSample =
        largeEditorSample(
            id = "xml-config",
            name = "XML config highlight threshold",
            languageLabel = "xml",
            code = xmlConfigDocument(targetSize.targetChars),
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    private fun largeEditorSample(
        id: String,
        name: String,
        languageLabel: String,
        code: String,
        workloadKind: BenchmarkWorkloadKind,
    ): SourceSample =
        SourceSample(
            caseId = largeEditorCaseId(id),
            name = name,
            languageLabel = languageLabel,
            code = code,
            sizeName = targetSize.displayName,
            workloadKind = workloadKind,
        )

    private fun largeEditorCaseId(id: String): String =
        "diagnostics/large-editor/$id/${targetSize.idSegment}"

    private fun markdownEmbeddedDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("# Large Embedded Markdown Review")
            appendLine()
            appendLine("This diagnostic keeps Markdown prose, fenced source, and inline HTML active.")
            appendLine()
            var index = 0
            while (length < targetChars) {
                appendLine("## Release section $index")
                appendLine()
                appendLine("The section mixes prose with tables, links, tasks, and child-language fences.")
                appendLine()
                appendLine("| Field | Value |")
                appendLine("| :---- | :---- |")
                appendLine("| Owner | team-$index |")
                appendLine("| Status | review |")
                appendLine()
                appendLine("- [x] Capture migration notes for batch $index.")
                appendLine("- [ ] Verify embedded highlighting for the generated snippets.")
                appendLine()
                appendLine("```kotlin")
                appendLine("data class ReleaseNote$index(val id: String, val title: String, val words: Int)")
                appendLine("val note$index = ReleaseNote$index(\"note-$index\", \"Highlight budget\", ${1200 + index})")
                appendLine("```")
                appendLine()
                appendLine("```sql")
                appendLine("CREATE INDEX idx_note_${index}_updated ON notes_${index}(updated_at DESC);")
                appendLine("UPDATE notes_${index} SET reviewed = TRUE WHERE words > ${1200 + index};")
                appendLine("```")
                appendLine()
                appendLine("```json")
                appendLine("{ \"id\": \"note-$index\", \"tags\": [\"release\", \"syntax\"], \"pinned\": ${index % 2 == 0} }")
                appendLine("```")
                appendLine()
                appendLine("<details data-section=\"$index\">")
                appendLine("  <summary>Embedded HTML summary $index</summary>")
                appendLine("  <script type=\"application/json\">")
                appendLine("    { \"section\": $index, \"visible\": true, \"kind\": \"markdown-embedded\" }")
                appendLine("  </script>")
                appendLine("</details>")
                appendLine()
                index++
            }
        }

    private fun mdxMixedDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("import { Callout, MetricCard, SourceTabs } from \"./components\"")
            appendLine()
            appendLine("export const metadata = {")
            appendLine("  title: \"Large MDX diagnostic\",")
            appendLine("  owner: \"syntaxmp\",")
            appendLine("  tags: [\"benchmark\", \"highlight\"],")
            appendLine("}")
            appendLine()
            var index = 0
            while (length < targetChars) {
                appendLine("## MDX section $index")
                appendLine()
                appendLine("Markdown prose surrounds JSX islands and fenced code so host and child routing stay active.")
                appendLine()
                appendLine("<Callout tone={${if (index % 2 == 0) "\"info\"" else "\"warning\""}} title={`Section ${'$'}{$index}`}>")
                appendLine("  <MetricCard")
                appendLine("    id=\"metric-$index\"")
                appendLine("    value={metrics[$index]?.value ?? $index}")
                appendLine("    label={metrics[$index]?.label ?? \"Words\"}")
                appendLine("  />")
                appendLine("  <SourceTabs initialTab=\"tsx\" tabs={[\"tsx\", \"json\", \"sql\"]} />")
                appendLine("</Callout>")
                appendLine()
                appendLine("```tsx")
                appendLine("export const Section$index = ({ title }: { title: string }) => <article>{title}</article>")
                appendLine("```")
                appendLine()
                appendLine("```json")
                appendLine("{ \"section\": $index, \"visible\": true, \"owner\": \"docs\" }")
                appendLine("```")
                appendLine()
                appendLine("<section data-mdx=\"$index\">")
                appendLine("  <p>{metrics[$index]?.description ?? \"Ready for review\"}</p>")
                appendLine("</section>")
                appendLine()
                index++
            }
        }

    private fun tsxComponentDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("import React, { useMemo, useState } from \"react\";")
            appendLine()
            appendLine("type Metric = { id: string; title: string; value: number; owner?: string; tags: string[] };")
            appendLine("type DashboardProps = { metrics: Metric[]; selectedId?: string };")
            appendLine()
            appendLine("function formatValue(value: number): string {")
            appendLine("  return new Intl.NumberFormat().format(value);")
            appendLine("}")
            appendLine()
            appendLine("export function LargeDashboard({ metrics, selectedId }: DashboardProps): JSX.Element {")
            appendLine("  const [query, setQuery] = useState(\"\");")
            appendLine("  const visibleMetrics = useMemo(() => metrics.filter((metric) => metric.title.includes(query)), [metrics, query]);")
            appendLine("  return (")
            appendLine("    <main data-selected={selectedId} aria-label=\"Large dashboard\">")
            appendLine("      <input value={query} onChange={(event) => setQuery(event.currentTarget.value)} />")
            var index = 0
            while (length < targetChars) {
                appendLine("      <section className=\"metric-group\" data-index=\"$index\">")
                appendLine("        <header>")
                appendLine("          <h2>{visibleMetrics[$index]?.title ?? \"Untitled metric\"}</h2>")
                appendLine("          <span>{formatValue(visibleMetrics[$index]?.value ?? $index)}</span>")
                appendLine("        </header>")
                appendLine("        {visibleMetrics.slice(0, 6).map((metric, childIndex) => (")
                appendLine("          <article")
                appendLine("            key={`${'$'}{metric.id}-$index-${'$'}{childIndex}`}")
                appendLine("            data-active={metric.id === selectedId}")
                appendLine("            className={metric.value > $index ? \"metric-card is-hot\" : \"metric-card\"}")
                appendLine("          >")
                appendLine("            <button type=\"button\" onClick={() => console.log(metric.id)}>{metric.title}</button>")
                appendLine("            <p>{metric.owner ?? \"workspace\"}</p>")
                appendLine("            <small>{metric.tags.join(\" / \")}</small>")
                appendLine("          </article>")
                appendLine("        ))}")
                appendLine("      </section>")
                index++
            }
            appendLine("    </main>")
            appendLine("  );")
            appendLine("}")
        }

    private fun sqlMigrationDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("-- Large SQL migration diagnostic")
            appendLine("BEGIN;")
            appendLine()
            var index = 0
            while (length < targetChars) {
                appendLine("CREATE TABLE IF NOT EXISTS note_batch_$index (")
                appendLine("  id INTEGER PRIMARY KEY,")
                appendLine("  title TEXT NOT NULL,")
                appendLine("  body TEXT NOT NULL,")
                appendLine("  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
                appendLine("  reviewed BOOLEAN NOT NULL DEFAULT FALSE")
                appendLine(");")
                appendLine("CREATE INDEX idx_note_batch_${index}_reviewed ON note_batch_$index(reviewed, created_at DESC);")
                appendLine("ALTER TABLE note_batch_$index ADD COLUMN owner TEXT DEFAULT 'workspace';")
                appendLine("INSERT INTO note_batch_$index (id, title, body, reviewed) VALUES")
                appendLine("  ($index, 'Highlight budget $index', 'Large SQL migration row for SyntaxMP benchmarking', ${index % 2 == 0}),")
                appendLine("  (${index + 1000}, 'Follow up $index', 'Additional row for parser-shaped work', FALSE);")
                appendLine("UPDATE note_batch_$index SET reviewed = TRUE WHERE id = $index AND title LIKE 'Highlight%';")
                appendLine()
                index++
            }
            appendLine("COMMIT;")
        }

    private fun postgresqlMigrationDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("-- Large PostgreSQL migration diagnostic")
            appendLine("BEGIN;")
            appendLine()
            var index = 0
            while (length < targetChars) {
                appendLine("CREATE TABLE IF NOT EXISTS public.note_event_$index (")
                appendLine("  id BIGSERIAL PRIMARY KEY,")
                appendLine("  note_id UUID NOT NULL,")
                appendLine("  payload JSONB NOT NULL DEFAULT '{}'::jsonb,")
                appendLine("  tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],")
                appendLine("  created_at TIMESTAMPTZ NOT NULL DEFAULT now()")
                appendLine(");")
                appendLine("CREATE INDEX note_event_${index}_payload_gin ON public.note_event_$index USING gin (payload jsonb_path_ops);")
                appendLine("CREATE INDEX note_event_${index}_created_idx ON public.note_event_$index (created_at DESC);")
                appendLine("CREATE OR REPLACE FUNCTION public.touch_note_event_$index() RETURNS trigger LANGUAGE plpgsql AS ${'$'}migration_$index${'$'}")
                appendLine("BEGIN")
                appendLine("  NEW.payload = jsonb_set(NEW.payload, '{migration}', to_jsonb('$index'::text), true);")
                appendLine("  RETURN NEW;")
                appendLine("END;")
                appendLine("${'$'}migration_$index${'$'};")
                appendLine("CREATE TRIGGER touch_note_event_${index}_before_insert BEFORE INSERT ON public.note_event_$index")
                appendLine("  FOR EACH ROW EXECUTE FUNCTION public.touch_note_event_$index();")
                appendLine()
                index++
            }
            appendLine("COMMIT;")
        }

    private fun sqliteMigrationDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("-- Large SQLite migration diagnostic")
            appendLine("PRAGMA foreign_keys = ON;")
            appendLine("BEGIN TRANSACTION;")
            appendLine()
            var index = 0
            while (length < targetChars) {
                appendLine("CREATE TABLE IF NOT EXISTS note_cache_$index (")
                appendLine("  id INTEGER PRIMARY KEY AUTOINCREMENT,")
                appendLine("  path TEXT NOT NULL UNIQUE,")
                appendLine("  title TEXT NOT NULL,")
                appendLine("  body TEXT NOT NULL,")
                appendLine("  flags INTEGER NOT NULL DEFAULT 0,")
                appendLine("  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP")
                appendLine(");")
                appendLine("CREATE INDEX IF NOT EXISTS idx_note_cache_${index}_updated ON note_cache_$index(updated_at DESC);")
                appendLine("CREATE TRIGGER IF NOT EXISTS note_cache_${index}_touch AFTER UPDATE ON note_cache_$index")
                appendLine("BEGIN")
                appendLine("  UPDATE note_cache_$index SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;")
                appendLine("END;")
                appendLine("INSERT OR IGNORE INTO note_cache_$index(path, title, body, flags) VALUES")
                appendLine("  ('/library/note-$index.md', 'Note $index', 'SQLite migration payload', ${index % 4});")
                appendLine()
                index++
            }
            appendLine("COMMIT;")
        }

    private fun htmlRawTextDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("<!doctype html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"utf-8\">")
            appendLine("  <title>Large HTML diagnostic</title>")
            appendLine("  <style>")
            appendLine("    .metric-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem; }")
            appendLine("    .metric-card[data-state=\"warning\"] { color: #7a4b00; border-color: #d6a100; }")
            appendLine("  </style>")
            appendLine("  <script type=\"module\">")
            appendLine("    const formatter = new Intl.NumberFormat();")
            appendLine("    export function renderValue(value) { return formatter.format(value); }")
            appendLine("  </script>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <main class=\"metric-grid\">")
            var index = 0
            while (length < targetChars) {
                appendLine("    <article class=\"metric-card\" data-index=\"$index\" data-state=\"${if (index % 3 == 0) "warning" else "ready"}\">")
                appendLine("      <header><h2>Metric $index</h2><span>${1000 + index}</span></header>")
                appendLine("      <p>Large HTML rows keep ordinary tags, attributes, entities &amp; raw text in view.</p>")
                appendLine("      <template id=\"metric-template-$index\">")
                appendLine("        <button type=\"button\" data-action=\"open\" data-target=\"note-$index\">Open note $index</button>")
                appendLine("      </template>")
                appendLine("      <script type=\"application/json\">")
                appendLine("        { \"id\": \"metric-$index\", \"value\": ${1000 + index}, \"visible\": true }")
                appendLine("      </script>")
                appendLine("    </article>")
                index++
            }
            appendLine("  </main>")
            appendLine("</body>")
            appendLine("</html>")
        }

    private fun xmlConfigDocument(targetChars: Int): String =
        buildString(targetChars + 4096) {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<workspace id=\"syntaxmp\" generated=\"true\">")
            appendLine("  <metadata owner=\"benchmarks\" kind=\"large-editor\" />")
            var index = 0
            while (length < targetChars) {
                appendLine("  <library id=\"library-$index\" path=\"/workspace/library-$index\">")
                appendLine("    <note id=\"note-$index\" title=\"Highlight threshold $index\" pinned=\"${index % 2 == 0}\">")
                appendLine("      <tags>")
                appendLine("        <tag>syntax</tag>")
                appendLine("        <tag>benchmark</tag>")
                appendLine("        <tag>large-file</tag>")
                appendLine("      </tags>")
                appendLine("      <stats words=\"${1200 + index}\" headings=\"${index % 6 + 1}\" links=\"${index % 9}\" />")
                appendLine("      <body>XML config text keeps nested elements and attributes active for scanner review.</body>")
                appendLine("    </note>")
                appendLine("  </library>")
                index++
            }
            appendLine("</workspace>")
        }
}
