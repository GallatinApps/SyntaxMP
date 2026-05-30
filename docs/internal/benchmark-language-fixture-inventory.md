# Benchmark Language Fixture Inventory

## Purpose

This document is the inventory of benchmark-owned representative language fixtures. It maps each
built-in SyntaxMP language to its benchmark fixture, owner file, body line target, representative
matrix rows, and the demo sample it draws from.

Current coverage:

- `SyntaxLanguageId.BuiltIns` contains 54 built-in languages.
- `syntaxmp-demo` contains 54 demo language samples.
- The benchmark fixture matrix will contain 55 representative fixture rows because Markdown is
  split into plain Markdown and embedded-fence Markdown.

The benchmark fixtures should be authored independently from the demo samples. Demo samples are
presentation-oriented and may change freely; benchmark fixtures should change only when the
representative workload itself is intentionally revised.

## Catalog Layout

Place fixture code under:

```text
syntaxmp-benchmarks/src/jvmMain/kotlin/com/gallatinapps/syntaxmp/benchmarks/fixtures/
```

Suggested owner files:

| Owner file | Responsibility |
|---|---|
| `LanguageBenchmarkFixture.kt` | Fixture model, family enum, multiplier enum, and expansion helper. |
| `LanguageBenchmarkCatalog.kt` | Registers all representative language fixtures and exposes the representative matrix. |
| `source/GeneralSourceFixtures.kt` | Kotlin/Java/C-family/general source fixtures. |
| `source/WebScriptFixtures.kt` | JavaScript, TypeScript, JSX, and TSX fixtures. |
| `source/ScriptLikeFixtures.kt` | Python, Ruby, PHP, Shell, and PowerShell fixtures. |
| `markup/MarkupComponentFixtures.kt` | HTML, XML, MDX, Vue, Svelte, and Astro fixtures. |
| `markdown/MarkdownFixtures.kt` | Plain Markdown and embedded-fence Markdown fixtures. |
| `styles/StylesheetFixtures.kt` | CSS, SCSS, Less, and GLSL fixtures. |
| `query/QueryInfrastructureFixtures.kt` | SQL-family, GraphQL, Protobuf, HCL, and Terraform fixtures. |
| `config/ConfigDataFixtures.kt` | JSON, JSON5, YAML, TOML, and INI fixtures. |
| `compact/CompactLineFixtures.kt` | CSV, Diff, DNS zone, Apache config, Dockerfile, and Makefile fixtures. |

Stable case ids should use:

```text
languages/representative/<fixture-id>/<multiplier>
```

Examples:

```text
languages/representative/kotlin/1x
languages/representative/kotlin/10x
languages/representative/markdown-plain/1x
languages/representative/markdown-embedded/10x
```

## Matrix Policy

All representative fixtures run at `1x` and `10x`. There is no quick/full profile split.

Large-file coverage stays outside this representative matrix. Do not add a broad `100x`
representative multiplier across every language; use focused diagnostics for specific large file
shapes that need product-sized coverage. The first targeted set lives in the diagnostics section
under `diagnostics/large-editor/*/highlight-threshold` and covers Markdown embedded, MDX, TSX,
SQL/PostgreSQL/SQLite migration shapes, structured JSON, HTML raw text, and XML config around
250 KiB.

## Inventory

All owner paths below are relative to
`syntaxmp-benchmarks/src/jvmMain/kotlin/com/gallatinapps/syntaxmp/benchmarks/fixtures/`.

| Fixture id | Language label | Display name | Family | Target body lines | Matrix rows | Owner file | Demo sample inspiration |
|---|---|---|---|---:|---|---|---|
| `json` | `json` | JSON | Config data | 60 | `1x`, `10x` | `config/ConfigDataFixtures.kt` | `JsonSample.kt` |
| `yaml` | `yaml` | YAML | Config data | 60 | `1x`, `10x` | `config/ConfigDataFixtures.kt` | `YamlSample.kt` |
| `toml` | `toml` | TOML | Config data | 60 | `1x`, `10x` | `config/ConfigDataFixtures.kt` | `TomlSample.kt` |
| `csv` | `csv` | CSV | Compact line format | 12 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `CsvSample.kt` |
| `markdown-plain` | `markdown` | Markdown plain | Markdown plain | 100 | `1x`, `10x` | `markdown/MarkdownFixtures.kt` | `MarkdownSample.kt` plain portions |
| `markdown-embedded` | `markdown` | Markdown embedded | Markdown embedded | 100 | `1x`, `10x` | `markdown/MarkdownFixtures.kt` | `MarkdownSample.kt` fenced/HTML portions |
| `sql` | `sql` | SQL | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `SqlSample.kt` |
| `sqlite` | `sqlite` | SQLite | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `SqliteSample.kt` |
| `diff` | `diff` | Diff | Compact line format | 30 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `DiffSample.kt` |
| `css` | `css` | CSS | Stylesheet | 90 | `1x`, `10x` | `styles/StylesheetFixtures.kt` | `CssSample.kt` |
| `html` | `html` | HTML | Markup/component | 90 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `HtmlSample.kt` |
| `xml` | `xml` | XML | Markup/component | 90 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `XmlSample.kt` |
| `ini` | `ini` | INI / Properties | Config data | 60 | `1x`, `10x` | `config/ConfigDataFixtures.kt` | `IniSample.kt` |
| `json5` | `json5` | JSON5 | Config data | 60 | `1x`, `10x` | `config/ConfigDataFixtures.kt` | `Json5Sample.kt` |
| `dockerfile` | `dockerfile` | Dockerfile | Compact line format | 35 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `DockerfileSample.kt` |
| `makefile` | `makefile` | Makefile | Compact line format | 35 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `MakefileSample.kt` |
| `terraform` | `terraform` | Terraform | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `TerraformSample.kt` |
| `hcl` | `hcl` | HCL | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `HclSample.kt` |
| `graphql` | `graphql` | GraphQL | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `GraphQlSample.kt` |
| `protobuf` | `protobuf` | Protocol Buffers | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `ProtobufSample.kt` |
| `postgresql` | `postgresql` | PostgreSQL | Query/schema | 80 | `1x`, `10x` | `query/QueryInfrastructureFixtures.kt` | `PostgresqlSample.kt` |
| `glsl` | `glsl` | GLSL | Stylesheet | 90 | `1x`, `10x` | `styles/StylesheetFixtures.kt` | `GlslSample.kt` |
| `apacheconf` | `apacheconf` | Apache Config | Compact line format | 35 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `ApacheConfSample.kt` |
| `dns-zone` | `dns-zone` | DNS Zone | Compact line format | 30 | `1x`, `10x` | `compact/CompactLineFixtures.kt` | `DnsZoneSample.kt` |
| `objective-c` | `objective-c` | Objective-C | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `ObjectiveCSample.kt` |
| `lua` | `lua` | Lua | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `LuaSample.kt` |
| `r` | `r` | R | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `RSample.kt` |
| `scala` | `scala` | Scala | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `ScalaSample.kt` |
| `elixir` | `elixir` | Elixir | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `ElixirSample.kt` |
| `python` | `python` | Python | Script-like source | 90 | `1x`, `10x` | `source/ScriptLikeFixtures.kt` | `PythonSample.kt` |
| `ruby` | `ruby` | Ruby | Script-like source | 90 | `1x`, `10x` | `source/ScriptLikeFixtures.kt` | `RubySample.kt` |
| `php` | `php` | PHP | Script-like source | 90 | `1x`, `10x` | `source/ScriptLikeFixtures.kt` | `PhpSample.kt` |
| `go` | `go` | Go | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `GoSample.kt` |
| `rust` | `rust` | Rust | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `RustSample.kt` |
| `dart` | `dart` | Dart | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `DartSample.kt` |
| `groovy` | `groovy` | Groovy | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `GroovySample.kt` |
| `c` | `c` | C | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `CSample.kt` |
| `cpp` | `cpp` | C++ | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `CppSample.kt` |
| `csharp` | `csharp` | C# | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `CSharpSample.kt` |
| `perl` | `perl` | Perl | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `PerlSample.kt` |
| `kotlin` | `kotlin` | Kotlin | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `KotlinSample.kt` |
| `swift` | `swift` | Swift | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `SwiftSample.kt` |
| `java` | `java` | Java | General source | 100 | `1x`, `10x` | `source/GeneralSourceFixtures.kt` | `JavaSample.kt` |
| `javascript` | `javascript` | JavaScript | Web script source | 100 | `1x`, `10x` | `source/WebScriptFixtures.kt` | `JavaScriptSample.kt` |
| `typescript` | `typescript` | TypeScript | Web script source | 100 | `1x`, `10x` | `source/WebScriptFixtures.kt` | `TypeScriptSample.kt` |
| `jsx` | `jsx` | JSX | Web script source | 100 | `1x`, `10x` | `source/WebScriptFixtures.kt` | `JsxSample.kt` |
| `tsx` | `tsx` | TSX | Web script source | 100 | `1x`, `10x` | `source/WebScriptFixtures.kt` | `TsxSample.kt` |
| `mdx` | `mdx` | MDX | Markup/component | 90 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `MdxSample.kt` |
| `vue` | `vue` | Vue | Markup/component | 90 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `VueSample.kt` |
| `svelte` | `svelte` | Svelte | Markup/component | 90 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `SvelteSample.kt` |
| `astro` | `astro` | Astro | Markup/component | 75 | `1x`, `10x` | `markup/MarkupComponentFixtures.kt` | `AstroSample.kt` |
| `shell` | `shell` | Shell | Script-like source | 90 | `1x`, `10x` | `source/ScriptLikeFixtures.kt` | `ShellSample.kt` |
| `scss` | `scss` | SCSS | Stylesheet | 90 | `1x`, `10x` | `styles/StylesheetFixtures.kt` | `ScssSample.kt` |
| `less` | `less` | Less | Stylesheet | 90 | `1x`, `10x` | `styles/StylesheetFixtures.kt` | `LessSample.kt` |
| `powershell` | `powershell` | PowerShell | Script-like source | 90 | `1x`, `10x` | `source/ScriptLikeFixtures.kt` | `PowerShellSample.kt` |

## Conventions

- Each fixture row carries a stable case id; reports and comparisons key on it, not on display name.
- The representative language matrix is tokenization-only. Compose span-building lives in the
  diagnostics section.
- The self-check warns when an authored body is outside roughly +/-30% of its target line count.
- `{{index}}` is the only placeholder fixtures use.
- Latest reports refresh `syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks/latest/`.
