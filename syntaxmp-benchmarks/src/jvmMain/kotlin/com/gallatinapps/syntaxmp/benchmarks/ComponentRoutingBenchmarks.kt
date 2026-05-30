package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine

internal object ComponentRoutingBenchmarks {
    private val defaultEngine = SyntaxTokenizerEngine()

    fun cases(): List<BenchmarkCase> {
        val samples = listOf(
            tsxManyExpressions(),
            tsxFewLargeExpressions(),
            jsxManyExpressions(),
            markdownManyFences(),
            markdownFewLargeFences(),
            markdownUnknownFences(),
            mdxMarkdownOnly(),
            mdxJsxOnly(),
            mdxMixedRegions(),
            mdxEsmOnly(),
            astroFrontmatterHeavy(),
            astroTemplateHeavy(),
            mdxFencedCodeHeavy(),
        )
        return samples.map { sample ->
            BenchmarkCase(
                id = sample.caseId,
                group = sample.group,
                name = sample.name,
                workloadKind = sample.workloadKind,
                sizeName = sample.sizeName,
                inputChars = sample.chars,
                inputLines = sample.lines,
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
    }

    private fun tsxManyExpressions(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/tsx-many-expressions/medium",
            group = "Component routing diagnostics",
            name = "TSX many small expressions",
            languageLabel = "tsx",
            code = tsxExpressionDocument(
                repeatedSection = """
                    <MetricCard
                      key={`metric-${'$'}{index}-{{item}}`}
                      title={metrics[{{item}}]?.title ?? "Untitled"}
                      value={formatValue(metrics[{{item}}]?.value ?? {{item}})}
                      tone={metrics[{{item}}]?.value > threshold ? "critical" : "normal"}
                      onOpen={() => openMetric(metrics[{{item}}]?.id ?? "metric-{{item}}")}
                    >
                      <span>{metrics[{{item}}]?.owner ?? "docs"}</span>
                      <small>{new Intl.NumberFormat().format(metrics[{{item}}]?.words ?? 0)} words</small>
                    </MetricCard>
                """,
            ),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun tsxFewLargeExpressions(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/tsx-few-large-expressions/medium",
            group = "Component routing diagnostics",
            name = "TSX few large expressions",
            languageLabel = "tsx",
            code = tsxExpressionDocument(
                repeatedSection = """
                    <section className="metric-cluster" data-index="{{item}}">
                      {metrics
                        .filter((metric) => metric.group === "review" || metric.pinned)
                        .map((metric) => ({
                          ...metric,
                          label: `${'$'}{metric.title} / ${'$'}{formatValue(metric.value)}`,
                          owner: metric.owner ?? "docs",
                          visible: metric.value > threshold || metric.pinned,
                        }))
                        .sort((left, right) => Number(right.visible) - Number(left.visible))
                        .slice(0, 12)
                        .map((metric) => (
                          <MetricCard key={metric.id} title={metric.label} value={metric.value} tone={metric.visible ? "critical" : "normal"}>
                            <span>{metric.owner}</span>
                          </MetricCard>
                        ))}
                    </section>
                """,
            ),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun jsxManyExpressions(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/jsx-many-expressions/medium",
            group = "Component routing diagnostics",
            name = "JSX many small expressions",
            languageLabel = "jsx",
            code = jsxExpressionDocument(),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun markdownManyFences(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/embedded-routing/markdown-many-fences/medium",
            group = "Embedded routing diagnostics",
            name = "Markdown many small fences",
            languageLabel = "markdown",
            code = markdownFenceDocument(
                fenceCount = 80,
                languageCycle = listOf("kotlin", "typescript", "json", "sql"),
                linesPerFence = 4,
            ),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun markdownFewLargeFences(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/embedded-routing/markdown-few-large-fences/medium",
            group = "Embedded routing diagnostics",
            name = "Markdown few large fences",
            languageLabel = "markdown",
            code = markdownFenceDocument(
                fenceCount = 6,
                languageCycle = listOf("kotlin", "typescript", "json", "sql"),
                linesPerFence = 70,
            ),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun markdownUnknownFences(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/embedded-routing/markdown-unknown-fences/medium",
            group = "Embedded routing diagnostics",
            name = "Markdown unknown-language fences",
            languageLabel = "markdown",
            code = markdownFenceDocument(
                fenceCount = 80,
                languageCycle = listOf("bench-unknown-a", "bench-unknown-b"),
                linesPerFence = 4,
            ),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxMarkdownOnly(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/mdx-markdown-only/medium",
            group = "Component routing diagnostics",
            name = "MDX markdown-only",
            languageLabel = "mdx",
            code = repeatedToAtLeast(mdxMarkdownBlock, BenchmarkTargetSize.MediumEditorFile.targetChars),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxJsxOnly(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/mdx-jsx-only/medium",
            group = "Component routing diagnostics",
            name = "MDX JSX-only islands",
            languageLabel = "mdx",
            code = repeatedToAtLeast(mdxJsxBlock, BenchmarkTargetSize.MediumEditorFile.targetChars),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxMixedRegions(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/mdx-markdown-vs-jsx/medium",
            group = "Component routing diagnostics",
            name = "MDX mixed Markdown and JSX",
            languageLabel = "mdx",
            code = repeatedToAtLeast(mdxMixedBlock, BenchmarkTargetSize.MediumEditorFile.targetChars),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxEsmOnly(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/mdx-esm-only/medium",
            group = "Component routing diagnostics",
            name = "MDX ESM-heavy",
            languageLabel = "mdx",
            code = repeatedToAtLeast(mdxEsmBlock, BenchmarkTargetSize.MediumEditorFile.targetChars),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun mdxFencedCodeHeavy(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/mdx-fenced-code-heavy/medium",
            group = "Component routing diagnostics",
            name = "MDX fenced-code-heavy",
            languageLabel = "mdx",
            code = repeatedToAtLeast(mdxFenceBlock, BenchmarkTargetSize.MediumEditorFile.targetChars),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun astroFrontmatterHeavy(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/astro-frontmatter-heavy/medium",
            group = "Component routing diagnostics",
            name = "Astro frontmatter-heavy",
            languageLabel = "astro",
            code = astroFrontmatterDocument(),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun astroTemplateHeavy(): DiagnosticSourceSample =
        DiagnosticSourceSample(
            caseId = "diagnostics/component-routing/astro-template-heavy/medium",
            group = "Component routing diagnostics",
            name = "Astro template-heavy",
            languageLabel = "astro",
            code = astroTemplateDocument(),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    private fun tsxExpressionDocument(repeatedSection: String): String {
        val section = repeatedSection.trimIndent()
        return buildString(BenchmarkTargetSize.MediumEditorFile.targetChars + section.length) {
            appendLine("import React from \"react\";")
            appendLine("type Tone = \"normal\" | \"critical\";")
            appendLine("type Metric = { id: string; title: string; value: number; words: number; owner?: string; group?: string; pinned?: boolean };")
            appendLine("const threshold = 42;")
            appendLine("function formatValue(value: number): string { return value.toLocaleString(); }")
            appendLine("function openMetric(id: string): void { console.log(id); }")
            appendLine("function MetricCard(props: { title: string; value: string | number; tone: Tone; onOpen?: () => void; children?: React.ReactNode }): JSX.Element {")
            appendLine("  return <article data-tone={props.tone} onClick={props.onOpen}><h2>{props.title}</h2>{props.children}</article>;")
            appendLine("}")
            appendLine("export function Dashboard({ metrics }: { metrics: Metric[] }): JSX.Element {")
            appendLine("  return <main>")
            var index = 0
            while (length < BenchmarkTargetSize.MediumEditorFile.targetChars) {
                appendLine(section.replace("{{item}}", index.toString()))
                index++
            }
            appendLine("  </main>;")
            appendLine("}")
        }
    }

    private fun jsxExpressionDocument(): String {
        val section = """
            <MetricCard
              key={`metric-${'$'}{index}-{{item}}`}
              title={metrics[{{item}}]?.title ?? "Untitled"}
              value={formatValue(metrics[{{item}}]?.value ?? {{item}})}
              tone={metrics[{{item}}]?.value > threshold ? "critical" : "normal"}
              onOpen={() => openMetric(metrics[{{item}}]?.id ?? "metric-{{item}}")}
            >
              <span>{metrics[{{item}}]?.owner ?? "docs"}</span>
              <small>{new Intl.NumberFormat().format(metrics[{{item}}]?.words ?? 0)} words</small>
            </MetricCard>
        """.trimIndent()
        return buildString(BenchmarkTargetSize.MediumEditorFile.targetChars + section.length) {
            appendLine("import React from \"react\";")
            appendLine("const threshold = 42;")
            appendLine("function formatValue(value) { return value.toLocaleString(); }")
            appendLine("function openMetric(id) { console.log(id); }")
            appendLine("function MetricCard(props) {")
            appendLine("  return <article data-tone={props.tone} onClick={props.onOpen}><h2>{props.title}</h2>{props.children}</article>;")
            appendLine("}")
            appendLine("export default function Dashboard({ metrics }) {")
            appendLine("  return <main>")
            var index = 0
            while (length < BenchmarkTargetSize.MediumEditorFile.targetChars) {
                appendLine(section.replace("{{item}}", index.toString()))
                index++
            }
            appendLine("  </main>;")
            appendLine("}")
        }
    }

    private fun markdownFenceDocument(
        fenceCount: Int,
        languageCycle: List<String>,
        linesPerFence: Int,
    ): String =
        buildString(BenchmarkTargetSize.MediumEditorFile.targetChars + 1024) {
            appendLine("# Embedded Routing Diagnostic")
            appendLine()
            repeat(fenceCount) { index ->
                appendLine("## Region $index")
                appendLine()
                appendLine("Prose before the fence keeps Markdown host scanning active.")
                appendLine()
                val language = languageCycle[index % languageCycle.size]
                appendLine("```$language")
                repeat(linesPerFence) { line ->
                    appendLine(fenceLine(language = language, regionIndex = index, lineIndex = line))
                }
                appendLine("```")
                appendLine()
            }
            while (length < BenchmarkTargetSize.MediumEditorFile.targetChars) {
                appendLine("Trailing prose keeps total byte size comparable across fence shapes.")
            }
        }

    private fun fenceLine(
        language: String,
        regionIndex: Int,
        lineIndex: Int,
    ): String =
        when (language) {
            "kotlin" -> "val note${regionIndex}_$lineIndex = \"region-$regionIndex-$lineIndex\""
            "typescript" -> "const note${regionIndex}_$lineIndex: string = \"region-$regionIndex-$lineIndex\";"
            "json" -> "\"note_${regionIndex}_$lineIndex\": { \"enabled\": true, \"words\": ${regionIndex + lineIndex} },"
            "sql" -> "SELECT '$regionIndex' AS region_id, '$lineIndex' AS line_id;"
            else -> "region=$regionIndex line=$lineIndex alpha beta gamma delta epsilon"
        }

    private fun astroFrontmatterDocument(): String =
        buildString(BenchmarkTargetSize.MediumEditorFile.targetChars + 1024) {
            appendLine("---")
            appendLine("import NoteCard from \"../components/NoteCard.astro\";")
            appendLine("type Note = { id: string; title: string; words: number; pinned: boolean };")
            appendLine("const notes: Note[] = [")
            var index = 0
            while (length < BenchmarkTargetSize.MediumEditorFile.targetChars) {
                appendLine("  { id: \"note-$index\", title: \"Launch $index\", words: ${800 + index}, pinned: ${index % 3 == 0} },")
                index++
            }
            appendLine("];")
            appendLine("const visibleNotes = notes.filter((note) => note.pinned).slice(0, 20);")
            appendLine("---")
            appendLine("<section>{visibleNotes.map((note) => <NoteCard {...note} />)}</section>")
        }

    private fun astroTemplateDocument(): String {
        val section = """
            <section class="library-panel" data-index="{{item}}">
              <header>
                <p class="eyebrow">Library {{item}}</p>
                <h2>{notes[{{item}}]?.title ?? "Untitled"}</h2>
              </header>
              {notes.slice(0, 4).map((note) => (
                <NoteCard id={note.id} title={note.title} words={note.words} pinned={note.pinned} client:visible />
              ))}
              <details open>
                <summary>Template routing {{item}}</summary>
                <p>{notes[{{item}}]?.words ?? 0} words are ready for review.</p>
              </details>
            </section>
        """.trimIndent()
        return buildString(BenchmarkTargetSize.MediumEditorFile.targetChars + section.length) {
            appendLine("---")
            appendLine("import NoteCard from \"../components/NoteCard.astro\";")
            appendLine("const notes = [{ id: \"note-1\", title: \"Launch\", words: 1240, pinned: true }];")
            appendLine("---")
            var index = 0
            while (length < BenchmarkTargetSize.MediumEditorFile.targetChars) {
                appendLine(section.replace("{{item}}", index.toString()))
                index++
            }
        }
    }

    private data class DiagnosticSourceSample(
        val caseId: String,
        val group: String,
        val name: String,
        val languageLabel: String,
        val code: String,
        val sizeName: String,
        val workloadKind: BenchmarkWorkloadKind,
    ) {
        val chars: Int = code.length
        val lines: Int = lineCount(code)
    }
}

private val mdxMarkdownBlock = """
    ## Review Section

    The Markdown-only MDX diagnostic avoids JSX islands and fenced child languages.
    It keeps emphasis, `inline code`, tables, links, block quotes, and lists in the host scanner.

    | Signal | Owner | Status |
    | :----- | :---- | :----- |
    | Scan | Browser | Ready |
    | Edit | Documents | Review |

    > Host Markdown should dominate this row.

    - Keep Markdown markers realistic.
    - Avoid component islands.
    - Avoid fenced code routing.

""".trimIndent() + "\n\n"

private val mdxJsxBlock = """
    <Callout tone="info" title={`Review ${'$'}{index}`}>
      <NoteCard
        id={`note-${'$'}{index}`}
        title="Launch plan"
        words={1240 + index}
        pinned={index % 2 === 0}
        tags={["release", "review"]}
      />
      <Chart
        data={[
          { label: "Draft", value: 12 },
          { label: "Review", value: 8 },
          { label: "Published", value: 4 },
        ]}
      />
    </Callout>

""".trimIndent() + "\n\n"

private val mdxMixedBlock = """
    ## Mixed Section

    Markdown prose appears before and after component islands so host and component routing are both active.

    <Callout tone="warning" title="Review queue">
      <NoteCard id="note-mixed" title="Launch plan" words={1240} pinned />
    </Callout>

    ```tsx
    export const Card = ({ title }: { title: string }) => <article>{title}</article>
    ```

    More Markdown text follows the JSX island and fenced code.

""".trimIndent() + "\n\n"

private val mdxEsmBlock = """
    import { Callout, NoteCard } from "./components"
    export const metadata = {
      title: "Review queue",
      owner: "docs",
      tags: ["release", "review"],
    }
    export const notes = Array.from({ length: 8 }, (_, index) => ({
      id: `note-${'$'}{index}`,
      title: `Launch ${'$'}{index}`,
      words: 800 + index,
    }))

""".trimIndent() + "\n\n"

private val mdxFenceBlock = """
    ## Fenced Code Section

    ```tsx
    export function Card({ title, words }: { title: string; words: number }) {
      return <article data-words={words}>{title}</article>
    }
    ```

    ```json
    { "title": "Launch plan", "words": 1240, "pinned": true }
    ```

    ```kotlin
    data class Note(val title: String, val words: Int)
    ```

""".trimIndent() + "\n\n"
