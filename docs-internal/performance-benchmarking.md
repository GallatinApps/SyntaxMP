# Performance Benchmarking

SyntaxMP has a local JVM-only benchmark harness in `syntaxmp-benchmarks`. It is a developer tool
for comparing representative language tokenization, focused tokenizer hot paths,
span-normalization, embedded-routing, extension-lookup, and Compose span-building costs. It is not
part of normal validation or `check`.

## Running Benchmarks

Run the combined benchmark from the SyntaxMP root:

```bash
./gradlew :syntaxmp-benchmarks:runSyntaxMpBenchmarks
```

SyntaxMP intentionally has one normal benchmark task. The generated report keeps the interpretation
models separate inside one document:

- The language matrix appears first and contains the representative `1x`/`10x` fixture rows for
  every built-in language.
- The diagnostics section follows with focused mechanism rows, product-sized large-editor target
  rows, normalizer, embedded substring, extension lookup, and Compose span-building.

For Kotlin hot-path profiling, use the benchmark-local JFR task:

```bash
./gradlew :syntaxmp-benchmarks:runKotlinHotPathProfiling
```

## Representative Language Fixtures

The language matrix is a benchmark-owned representative fixture matrix for every built-in
SyntaxMP language. Markdown is split into two fixture rows: plain Markdown and Markdown with
embedded fences/HTML. The language matrix is tokenization-only; Compose span-building remains in
the diagnostics section.

Each representative fixture is authored as:

```text
header + repeat(body with {{index}} replacement) + footer
```

The header and footer appear once. Only the body is repeated. This avoids unrealistic scaled files
with ten package declarations, ten import blocks, or ten copies of other one-time file prologues.
The `{{index}}` placeholder lets repeated functions, selectors, resource names, routes, and call
sites remain plausible.

Representative language sizes:

| Multiplier | Expansion |
|---|---|
| `1x` | Header + one body + footer. |
| `10x` | Header + ten indexed bodies + footer. |

For source-like languages, fixture bodies target roughly 80-100 meaningful lines so `10x` lands
near an editor-large file shape. Compact formats such as CSV, diff, Dockerfiles,
and small config formats intentionally use smaller body targets. A thousand CSV lines is a
different workload from a thousand Kotlin lines; the matrix reports actual lines, characters, and
spans so reviewers can interpret those differences.

These fixtures are representative benchmark inputs, not real-world corpus samples and not a promise
that the expanded file would compile or run. Repeating bodies is still an artificial scaling tool.
Use the matrix to compare scanner cost across language-shaped inputs, then use focused diagnostics
or real host profiling before optimizing a specific path.

The representative matrix intentionally stops at `1x` and `10x`. Do not add a broad `100x`
representative multiplier just to approximate large files; that would repeat artificial fixture
bodies across every language and make report interpretation worse. Use targeted diagnostics when a
specific product-sized file shape needs coverage.

## Run Metadata

Benchmark results are only meaningful when compared against another run from a similar local
environment. The generated report records runtime metadata such as CPU family, OS, JDK, Kotlin
version, Gradle invocation, power state, and whether JVM allocation tracking was available.

Set these environment variables when you need more specific local metadata than the JVM exposes:

```bash
SYNTAXMP_BENCHMARK_CPU="Local workstation" \
SYNTAXMP_BENCHMARK_POWER="AC power" \
./gradlew :syntaxmp-benchmarks:runSyntaxMpBenchmarks
```

Do not treat a checked-in machine description as the project baseline. For a performance change,
run the benchmark before and after the change on the same machine, power state, JDK, and checkout
shape whenever possible.

## Reports

The task writes one timestamped historical report and refreshes one `latest` report directory:

```text
syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/runs/<timestamp>/report.md
syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/runs/<timestamp>/metadata.json
syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/latest/report.md
syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/latest/metadata.json
```

Every run creates a new historical directory and refreshes `latest/`. Each report directory should
contain only:

```text
report.md
metadata.json
```

`report.md` is the human-readable document. It starts with run metadata, then the language matrix,
then diagnostics, and finally an informational comparison section when `compareTo` is supplied.
`metadata.json` is the machine-readable source for comparison and historical aggregation.

The Kotlin profiler writes a Markdown summary plus raw `.jfr` files to:

```text
syntaxmp-benchmarks/build/reports/syntaxmp-profiling/
```

`build/` output is generated and should stay uncommitted unless a reviewed baseline snapshot is
explicitly requested.

## Informational Comparison

Use `syntaxmp.benchmark.compareTo` to compare the current run against a previous report. The value
can point to a historical run directory, `metadata.json`, or `report.md`:

```bash
./gradlew \
  -Dsyntaxmp.benchmark.compareTo=syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/runs/<timestamp>/metadata.json \
  :syntaxmp-benchmarks:runSyntaxMpBenchmarks
```

Comparison rows are keyed by stable case id, not display name. The report shows current median,
baseline median, absolute delta, and percentage delta, and marks missing/new rows. Comparison is
informational only: there are no drift thresholds and the benchmark task does not fail because a
row regressed. Local benchmark noise depends on power state, thermal state, JDK, background load,
and allocation tracking availability.

## Historical Run Aggregation

Use `aggregateSyntaxMpBenchmarkRuns` to summarize repeated historical runs without running the
benchmarks again. The aggregate task reads `metadata.json` as the source of truth; it does not
scrape Markdown tables and does not run in-process benchmark repeats.

Pass at least three historical run directories or `metadata.json` files as a comma-separated list:

```bash
./gradlew \
  -Dsyntaxmp.benchmark.aggregateRuns=<run-a>,<run-b>,<run-c> \
  :syntaxmp-benchmarks:aggregateSyntaxMpBenchmarkRuns
```

Use run directories such as
`syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/runs/<timestamp>` or direct paths to their
`metadata.json` files.

The task requires three runs and warns when fewer than five are supplied. Use five comparable runs
for stability decisions. By default, aggregate reports are written under:

```text
syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/aggregates/
```

Each aggregate row reports the stable case id, case name, median of medians, min/max median,
spread percentage, worst p90, median-allocation range, and an informational unstable flag. Spread
uses:

```text
(max observed median ms - min observed median ms) / median(observed median ms) * 100
```

Rows with spread `>= 30%` are marked unstable. This flag is review guidance only; the aggregate
task does not fail because a row is unstable.

## Focused Embedded And Component Diagnostics

The diagnostics section includes focused rows for the embedded/component watch area. These rows are
controlled shapes for answering a measurement question; they are not representative language
fixtures and should not replace language-matrix stability runs.

Component-routing diagnostics use stable ids under:

```text
diagnostics/component-routing/
```

They include TSX many-small-expression and few-large-expression shapes plus a JSX many-expression
shape.

Embedded-routing diagnostics use stable ids under:

```text
diagnostics/embedded-routing/
```

They include Markdown many-small-fence, few-large-fence, and unknown-language-fence shapes. Compare
rows with similar byte sizes to decide whether cost tracks bytes, span count, embedded region count,
child-tokenizer routing, or component scanner mechanics.

## Large Editor Diagnostics

The diagnostics section also includes large-editor rows under:

```text
diagnostics/large-editor/
```

These rows target `Highlight threshold file` size, about 250 KiB. That size is deliberately between
the medium editor-file diagnostics and the existing 500 KiB large-file stress rows, and it matches
the kind of upper-bound highlighted editor budget a host app can choose before falling back to
plain or read-only large-file behavior.

Current large-editor diagnostic rows:

| Case id | Language label | Question |
|---|---|---|
| `diagnostics/large-editor/markdown-embedded/highlight-threshold` | `markdown` | Large Markdown document with prose, tables, known-language fences, JSON fences, and embedded HTML. |
| `diagnostics/large-editor/tsx-components/highlight-threshold` | `tsx` | Large TSX component file with hooks, mapped children, props, expressions, and event handlers. |
| `diagnostics/large-editor/sql-migration/highlight-threshold` | `sql` | Large generic SQL migration/schema file. |
| `diagnostics/large-editor/postgresql-migration/highlight-threshold` | `postgresql` | Large PostgreSQL migration with JSONB, indexes, triggers, and dollar-quoted function bodies. |
| `diagnostics/large-editor/sqlite-migration/highlight-threshold` | `sqlite` | Large SQLite migration with tables, indexes, triggers, and inserts. |
| `diagnostics/large-editor/json-structured/highlight-threshold` | `json` | Large structured JSON document, contrasting with low-span string-payload rows. |
| `diagnostics/large-editor/html-raw-text/highlight-threshold` | `html` | Large HTML document with ordinary markup, templates, style, module script, and JSON script raw text. |
| `diagnostics/large-editor/xml-config/highlight-threshold` | `xml` | Large XML configuration/document shape with nested tags and attributes. |

Large-editor rows are diagnostics, not representative language baselines. They use one warmup plus
three measured samples to keep the diagnostics run practical.

## Reading Results

Each case reports actual character, line, and span counts. Generated diagnostic samples target
approximate byte sizes:

| Size name | Target size |
|---|---:|
| Medium editor file | 50 KB |
| Highlight threshold file | 250 KB |
| Large file | 500 KB |
| Stress file | 2 MB |

The language matrix is the primary report section for representative fixture review. It keeps the
table to case name, characters, lines, spans, measured samples, median time, p90 time, and median
allocation.
The diagnostics table retains stable grouping, workload kind, warmups, measured samples, median
time, p90 time, and median allocation for mechanism-specific cases.

Use the median column for normal comparisons and p90 for noise/regression checks. `chars/ms` helps
compare tokenization-style workloads, while `spans/ms` is more useful for normalizer and Compose
span-conversion cases. The checksum is intentionally printed and stored in a JVM `@Volatile` sink
so benchmark work cannot be trivially optimized away.

When the JVM exposes per-thread allocation counters, the report includes `Median alloc KiB` and
`P90 alloc KiB`. These numbers come from the JVM thread allocated bytes MXBean and include all
allocations performed by the measured iteration, not just the object type the benchmark is focused
on. Treat them as local directional allocation samples. If the JVM does not expose the counter, the
allocation cells are `n/a`.

The report also includes `Warmups` and `Samples`. Medium cases currently run two warmups and five
measured samples. Highlight-threshold, large, and stress cases use one warmup plus three measured
samples. A min/median/max row with identical values should be treated as suspicious;
normally it means the case has too few measured samples or is below timer resolution.

The diagnostics table's `Kind` column is part of the workload contract:

| Kind | Meaning |
|---|---|
| Representative | Benchmark-owned language matrix rows or source-shaped diagnostics intended to behave like editor workloads. |
| Embedded/raw-text routing | Markdown, HTML raw text, framework single-file components, heredocs, or synthetic embedded-language routing. |
| Low-span payload | Large or stress-sized file with one huge string/comment/raw-text body. This keeps B1 large-file coverage without pretending it has ordinary source-code span density. |
| Span-dense stress | Repeated small code blocks designed to create many spans. Use this to stress tokenization/normalization, not as a normal editor-file proxy. |
| Focused scanner shape | Controlled source-shaped input that isolates one scanner concern such as string interpolation, qualified-name context, ordinary identifiers, named arguments, or annotations. |
| Synthetic | Controlled spans emitted through registered public extension tokenizers or Compose-only span-building workloads. |
| Micro | Tiny extension lookup/tokenization checks where the case itself may batch many operations internally. |

Do not compare a medium representative/span-dense source row directly against a large low-span
payload row as a file-size scaling curve. Compare within the same `Kind`, or use the rows to answer
different questions:

- Representative rows show the current cost of code-shaped tokenization.
- Span-dense rows show how costs move when span count is intentionally high.
- Low-span payload rows show whether very large files avoid work proportional to the byte count
  when the tokenizer can collapse the content into a few spans.
- Compose-only rows isolate SyntaxMP-owned span-to-Compose conversion from tokenizer cost. The
  `engine.tokenize + ...` Compose rows are included only for real tokenized workloads, not for
  synthetic spans.

When the language matrix finds an expensive row, first decide whether the fixture content is
realistic and whether the cost matters for expected editor usage. Then compare against diagnostic
rows to determine whether the cost tracks bytes, spans, identifiers, embedded routing, string
contents, or language-specific scanner behavior.

The first harness intentionally avoids JMH or `kotlinx-benchmark`. Treat numbers as local,
directional measurements: close background apps, keep the machine on the same power source, and
compare before/after runs from the same checkout shape.

## Embedded Substring Copy Investigation

The benchmark includes focused rows under `Embedded substring copy`. They hold total embedded text
constant at 500 KiB, then split that same payload across 1, 10, 100, and 1000 regions:

- `range walk no copy` checks the range loop and checksum overhead without creating child strings.
- `substring copy only` performs the same `String.substring(...)` copies used before embedded
  routing.
- `public embedded route` routes each copied child string through `SyntaxTokenizer` to a
  child extension tokenizer that emits one span, isolating copy plus engine routing without a
  realistic child scanner dominating the result.

Illustrative local run (one machine/JVM; absolute times vary, the allocation shape is the durable
point):

| Regions | No-copy median ms | Substring median ms | Substring median alloc KiB | Public route median ms | Public route median alloc KiB |
|---:|---:|---:|---:|---:|---:|
| 1 | 0.001 | 0.067 | 500.1 | 0.119 | 500.6 |
| 10 | 0.003 | 0.020 | 500.5 | 0.133 | 502.9 |
| 100 | 0.023 | 0.051 | 504.0 | 0.236 | 528.7 |
| 1000 | 0.522 | 0.602 | 539.1 | 0.901 | 787.4 |

The copy allocation is visible: a 500 KiB embedded ASCII payload allocates roughly 500 KiB of child
string storage on this JVM, with extra object/list/span overhead as region count climbs. The wall
time is small in this isolated workload, and remains small compared with representative
tokenization, normalizer fallback, and Compose span-building rows. The large HTML raw-text public
tokenization row also shows about one payload-sized allocation, which matches the expected single
raw-text substring copy.

Current outcome: keep the current `appendEmbeddedSpans` substring behavior. It is simple,
keeps ordinary tokenizer authorship straightforward, and the measured time is not yet high enough
to justify source-window or range-aware tokenizer APIs. Revisit targeted internal range APIs only if
real host profiling shows large embedded regions causing memory pressure or GC churn.

## Incremental Tokenization Decision

The incremental-tokenization decision:

- Do not add mutable full-result caching to `SyntaxTokenizer`.
- Keep full-result caching host-owned and keyed by engine identity, language label, and content
  revision or hash.
- After the Plan 31 Kotlin scanner fixes, representative Kotlin rows are no longer the outlier that
  justifies immediate incremental-tokenization design.
- Keep host-owned debounce, off-immediate-path tokenization, and stale-result rejection as editor
  guidance rather than adding SyntaxMP helper APIs in this plan.
- Revisit incremental-tokenization design only if future host/editor profiling shows large
  source-shaped files still need low-latency partial rehighlighting.
