package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal object ComponentRoutingBenchmarks {
    private val defaultEngine = SyntaxTokenizer()

    fun cases(): List<BenchmarkCase> {
        val samples = listOf(
            tsxManyExpressions(),
            tsxFewLargeExpressions(),
            jsxManyExpressions(),
            markdownManyFences(),
            markdownFewLargeFences(),
            markdownUnknownFences(),
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
