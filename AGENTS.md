# AGENTS.md — SyntaxMP

> Read this before changing SyntaxMP. SyntaxMP is a standalone, publishable Kotlin Multiplatform library.

## What SyntaxMP Is

`SyntaxMP` is a standalone Kotlin Multiplatform syntax highlighting library.

It currently contains one publishable module and two non-published developer modules:

- `syntaxmp` — publishable library artifact.
- `syntaxmp-demo` — local Wasm-only Compose demo app.
- `syntaxmp-benchmarks` — local JVM-only performance benchmark harness.

The `syntaxmp` module owns both the pure Kotlin tokenizer layer and the Compose text styling helpers. This is a deliberate single-artifact decision for the current consumer set, not an accidental coupling.

Current publication identity:

- Group: `com.gallatinapps.syntaxmp`
- Artifact: `syntaxmp`
- Coordinate: `com.gallatinapps.syntaxmp:syntaxmp:<version>`
- Root package: `com.gallatinapps.syntaxmp`

The local build targets JVM, Android, `iosArm64`, `iosSimulatorArm64`, and web through
Kotlin/Wasm `wasmJs`.

## Module Scope

### `syntaxmp`

This module owns:

- `SyntaxTokenizerEngine`
- language identifiers, built-in aliases, built-in language sets, and extension overrides
- syntax roles, token spans, text spans, and tokenizer contracts
- lexical tokenizers for the built-in language set: Apache config, Astro, C, C#, C++, CSS, CSV, Dart, Diff, DNS zone files, Dockerfile, Elixir, GLSL, Go, GraphQL, Groovy/Gradle, HCL, HTML, INI/properties, Java, JavaScript, JSON, JSON5, JSX, Kotlin, Less, Lua, Makefile, Markdown, MDX, Objective-C, Perl, PHP, PostgreSQL, PowerShell, Protobuf, Python, R, Ruby, Rust, Scala, SCSS, Shell, SQL, SQLite, Svelte, Swift, Terraform, TOML, TSX, TypeScript, Vue, XML, and YAML
- span normalization and shared scanner helpers
- Compose `SpanStyle`, `AnnotatedString`, and `TextFieldBuffer` helpers
- `SyntaxStyle`, `SyntaxRoleStyles`, `SyntaxTheme`, and its `DefaultLight` / `DefaultDark` starter themes

This module must not own:

- host-app workspace, file, editor, navigation, autosave, export, or app-shell behavior
- host-app editor, renderer, preview, source-editor integration, or fenced-code policy
- app design-system tokens or Material 3 adapters
- language-server semantic highlighting, diagnostics, or decoration overlays unless a future change explicitly adds them

### `syntaxmp-demo`

This module is a non-published local demo app. It owns:

- the demo language catalog and curated showcase samples
- Navigation 3 route keys, back-stack behavior, and route serialization
- reusable common Compose UI for the Get Started guide, language list, preview pane, and not-found pane
- the Wasm browser entrypoint and browser-only URL/hash synchronization
- static web resources for the local demo, including `index.html`, `styles.css`, and the local-testable `404.html` redirect page

This module must not own:

- public SyntaxMP APIs or language metadata that the library depends on
- Maven publication configuration
- host-app concepts, DI wiring, persistence, editor state, or Material 3 visual components
- GitHub Pages workflows, hosted deployment configuration, or custom local server scripts

### `syntaxmp-benchmarks`

This module is a non-published JVM-only developer harness. It owns:

- dependency-light benchmark cases for tokenization, span normalization, embedded-language routing,
  extension lookup, substring-copy investigation, and Compose span-building
- quick/full benchmark profiles
- Markdown report generation under `syntaxmp-benchmarks/build/reports/`

This module must not own:

- public SyntaxMP APIs
- Maven publication configuration
- normal `check` task wiring or required CI validation
- committed generated benchmark reports unless explicitly requested for a reviewed baseline

## Package Layout

SyntaxMP uses three top-level source packages:

- `engine/`: pure tokenization machinery and public tokenization APIs.
- `languages/`: one package per built-in language, including language tokenizers, language-owned lexicons/options, and dedicated scanners.
- `compose/`: Compose text helpers and syntax theme APIs, with theme types under `compose/theme/`.

`engine/` is organized by subsystem:

```text
engine/
├── language/    # SyntaxLanguageId, SyntaxLanguageExtension, label normalization
├── role/        # SyntaxRole and role-path helpers
├── tokenizer/   # SyntaxTokenizer, requests/results, SyntaxTokenizerEngine
├── spans/       # SyntaxTokenSpan, span normalization, and embedded-span helpers
├── primitives/  # low-level reusable scanner building blocks
│   ├── strings/ # string, raw string, heredoc, interpolation, format, and prefix rules
│   └── numbers/ # numeric literal scanners and modes
├── routing/     # internal built-in/extension tokenizer routing
└── scanners/    # shared family scanners such as clike, script, and markup
```

Package ownership rules:

1. The root `com.gallatinapps.syntaxmp` namespace has no Kotlin files. New code belongs under `engine/`, `languages/`, or `compose/`.
2. Public pure SyntaxMP APIs live under `engine/language/`, `engine/role/`, `engine/spans/`, and `engine/tokenizer/`. Public Compose APIs live in `compose/` or `compose/theme/`.
3. New built-in languages get a lowercase, hyphenless package under `languages/<normalized-id>/`.
4. A language package owns its tokenizer, lexicon, language-specific scanner options, dedicated scanner, and language fixture tests.
5. A scanner remains under `engine/scanners/` only if at least two unrelated language packages use it and it has no consuming-language names or lexicons. Reuse by dialects/extensions of one primary language does not qualify.
6. Primary-language family scanners live in the primary language package. CSS owns `CssScanner`; SQL owns `SqlScanner`; HCL owns `HclScanner`.
7. Dialect/extension language packages keep their own tokenizer folder even when they import a primary language scanner. SCSS/Less import `CssScanner`; SQLite/PostgreSQL import `SqlScanner`; Terraform imports `HclScanner`.
8. Family scanner data belongs to the primary language package unless the scanner mechanics are genuinely language-agnostic.
9. `engine/scanners/markup/` is the single shared home for markup parsing across HTML, XML, JSX, TSX, Vue, Svelte, Astro, and MDX. Component behavior is configured through `MarkupScannerOptions`; `engine/scanners/component/` no longer exists.
10. Shared scanner primitives live under `engine/primitives/`. They may reference generic syntax concepts and reusable lexical shapes only.
11. Do not add broad `ScannerOptions.kt`, `Models.kt`, `Helpers.kt`, or `Support.kt` files when a precise file name exists.
12. Small related model clusters can live together. For example, comment options can share one file; a sealed interface can live with its small implementations.
13. Numeric literal support belongs under `engine/primitives/numbers/`, string-literal support under `engine/primitives/strings/`, and qualified-name/comment/identifier/brace primitives live flat under `engine/primitives/`.
14. Embedded-language routing is request-based. Tokenizers call `SyntaxTokenizeRequest.tokenizeEmbedded(...)`, span offset helpers live under `engine/spans/`, and markup raw-text label resolution lives under `engine/scanners/markup/`. Do not add a separate embedded-language engine package or a public callback type for this plumbing.
15. Tests mirror source ownership: language fixtures under `languages/fixtures/<language>/`, shared engine mechanics under `engine/<support>/`, and Compose tests under `compose/`.

Do not introduce host-app-specific naming prefixes or packages in SyntaxMP. Markdown is just one supported language here; library types stay library-centric.

## Tokenizer, Scanner, Options, And Lexicon Ownership

Use these definitions when adding or moving language code:

- A `Tokenizer` is the built-in language entrypoint. It receives `SyntaxTokenizeRequest`, chooses scanner mechanics, passes language vocabulary and scanner options into the scanner, and returns `SyntaxTokenizeResult`. Internal language tokenizer objects are named `<Language>Tokenizer` under `languages/<language>/`; do not include the redundant `Syntax` middle in those internal names.
- A `Scanner` is the lexical walker that emits token spans. Shared scanners under `engine/scanners/` contain mechanics only and must not contain consuming-language names, aliases, keyword sets, or dialect policy. Dedicated scanners live beside their tokenizer under `languages/<language>/`, but they still receive language vocabulary from tokenizer-wired constructor parameters rather than hiding semantic word tables inside scanner code.
- Scanner `Options` are immutable mechanics configuration passed to a scanner when it has several reusable behaviors: comment forms, literal rules, number scanners, identifier rules, role mappings, and embedded-language policy functions. Options do not carry vocabulary. Use direct scanner inputs for narrow one-off settings instead of adding an empty or single-use standard options wrapper.
- A `Lexicon` file is language-owned vocabulary and string-table data: keyword role maps, builtin role maps, constants, type names, directives, at-rules, raw-text tag names, embedded-language aliases, and role-specific word or prefix sets. When a language has curated vocabulary, put top-level `internal val` or `internal const val` declarations in `<Language>Lexicon.kt`; use `LexemeRoleMap`/`lexemeRoleMap(...)` when the vocabulary bucket owns per-lexeme roles. Languages with no curated vocabulary do not need marker lexicon files. Literal delimiters, interpolation mechanics, number suffix parsing, and scanner routing stay in tokenizers/options unless they are themselves language vocabulary.
- Tokenizers only reference their own language's lexicon. Derived languages have their own lexicon file that inherits relevant vals from the parent. Even pure re-exports get their own lexicon file, such as JSX/TSX/Vue/Svelte/Astro raw-text tags inherited from HTML or Less at-rules and constants inherited from CSS.
- A dialect tokenizer owns dialect vocabulary and options choices even when it imports a primary-language scanner. Dialects expose inherited vocabulary through their own `<Language>Lexicon.kt`, and dialect-specific words stay there too; do not expose tokenizer object values only for vocabulary reuse.
- Shared scanner packages may define reusable mechanics/options types, but they must not define language vocabularies or generic lexicon DTOs.

## Primitive Naming Convention

Use one suffix per durable primitive role:

| Suffix | Meaning | Examples |
|---|---|---|
| `*Options` | Reusable configuration. | `StringLiteralOptions`, `CommentOptions`, `QualifiedNameOptions` |
| `*Rule` | Configured pattern matcher tried at a specific position. | `QuotedStringRule`, `StringLiteralStartRule`, `StringLiteralClosingRule` |
| `*Scanner` | Worker that walks source text or a bounded range. | `StringLiteralScanner`, `NumericLiteralScanner` |
| `*Scope` | Operation surface available while rules/scanners run. | `StringLiteralScope` |
| `*Request` | Per-call input packet. | `StringLiteralRequest`, `SyntaxTokenizeRequest` |
| `*Result` | Runtime output packet from matching/finding/scanning. | `PrefixResult`, `ClosingResult`, `SyntaxTokenizeResult` |
| `*Map` | Static lookup data that classifies already-read lexemes or symbols. | `LexemeRoleMap` |
| `*Mode` | Enum-like behavior choice. | `EscapeMode`, `OctalMode`, `HeredocMode` |

Retired suffixes for primitives are `*Lexer`, `*Matcher`, `*Spec`, `*Variant`, `*Style`, `*Descriptor`, and `*Match`. Rename new or touched primitive code to the active vocabulary instead of extending those older names.

Primitive verbs follow the type role:

- Scanners use `scan(...)` for their principal method.
- Rules use `tryMatch(...)` for position-tested pattern matching.
- Scope methods that delegate to scanners may use `scan<Thing>(...)`, such as `scanNestedCode(...)`.
- Plain helpers use direct verbs such as `find...`, `is...`, and `roleFor...`.

Do not introduce a named `fun interface` for a one-off internal decision callback. Prefer a named function property when the type is only "given already-read input, choose this value", such as `escapeModeForPrefix`, `tripleEscapeModeForPrefix`, or `interpolationRulesForPrefix`.

## Public API Expectations

Important public pure API surfaces live in engine subpackages:

- `com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine`
- `com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer`
- `com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest`
- `com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult`
- `com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId`
- `com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension`
- `com.gallatinapps.syntaxmp.engine.role.SyntaxRole`
- `com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan`

Important public Compose API surfaces live in `com.gallatinapps.syntaxmp.compose` and `com.gallatinapps.syntaxmp.compose.theme`:

- `com.gallatinapps.syntaxmp.compose.SyntaxStyledSpan`
- `com.gallatinapps.syntaxmp.compose.buildSyntaxStyledSpans`
- `com.gallatinapps.syntaxmp.compose.applySyntaxStyledSpans`
- `com.gallatinapps.syntaxmp.compose.buildSyntaxAnnotatedString`
- `com.gallatinapps.syntaxmp.compose.rememberSyntaxAnnotatedString`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxStyle`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxRoleStyles`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxRoleStyles.withRoleStyle`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme.DefaultLight`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme.DefaultDark`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme.withRoleStyle`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme.withLanguageRoleStyle`
- `com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme.withLanguageRoleStyles`

SyntaxMP is still pre-release. Direct API cleanup is acceptable when it improves the long-term library shape, but keep naming library-centric and update this file plus `README.md` when the public API changes.

The authoritative public API reference lives in [`docs/api.md`](docs/api.md). **Any change that adds, removes, renames, or alters the signature of a public symbol must update `docs/api.md` in the same change.** This includes adding a built-in language constant on `SyntaxLanguageId.Companion`, adding a role to `SyntaxRole`, adding a copy/override helper, or any new public function/class/extension under `com.gallatinapps.syntaxmp.engine.*`, `com.gallatinapps.syntaxmp.compose`, or `com.gallatinapps.syntaxmp.compose.theme`. The doc is the curated index of the intended public surface; source remains the final source of truth, but drift between the two should be closed in the same PR.

Theme-author role documentation is split across two files: [`docs/syntax-roles.md`](docs/syntax-roles.md) (the roles primer: root and refinement constants, custom-role factories) and [`docs/languages.md`](docs/languages.md) (the per-language catalog of emitted roles, aliases, `SyntaxLanguageId` constants, and embedded-language routing). Update `docs/languages.md` whenever built-in tokenizers add, remove, or rename emitted roles; update `docs/syntax-roles.md` when root or refinement constants change.

Fixture coverage documentation lives in [`docs/internal/fixture-coverage.md`](docs/internal/fixture-coverage.md). Update it whenever tokenizer behavior or fixture coverage changes.

Theme color policy documentation lives in [`docs/internal/theme-color-policy.md`](docs/internal/theme-color-policy.md). Update it whenever a built-in language is added or a language's emitted roles, visual-group mapping, default-theme impact, or override recommendation changes.

## Theme Shape

The recommended styling path is intentionally constrained:

- `SyntaxStyle`: color plus optional `FontWeight` and `FontStyle`
- `SyntaxRoleStyles`: sparse `Map<SyntaxRole, SyntaxStyle>` entries for root or exact roles
- `SyntaxTheme`: global `roleStyles` plus optional per-language `languageOverrides`

SyntaxMP does not own a `CompositionLocal` or provider for syntax themes. Consumers that want subtree-wide theme threading should define their own local in the host app and pass that value to SyntaxMP call sites.

Do not replace this with a `SpanStyle`-per-role public style unless an explicit future plan changes the theme policy. Consumers that need full `SpanStyle` flexibility can bypass `SyntaxTheme` and write their own resolver at the call site.

## Primitives, Not Wrappers

SyntaxMP ships primitives for getting tokenized text into Compose, not opinionated wrappers around them. The library deliberately does **not** ship:

- A top-level `SyntaxText` composable. Users compose `rememberSyntaxAnnotatedString` + `BasicText` themselves — five lines that make the engine, theme, and text-style decisions explicit.
- A `rememberSyntaxTokenizerEngine` helper. Engine scope is a host decision (per-screen `remember`, host-defined `staticCompositionLocalOf`, or a DI container); the library shouldn't pre-bake a Composable-scoped default that masks that decision.
- A `CompositionLocal` or provider for the engine or the theme (see the Theme Shape section). Hosts own scoping, lifecycle, and provider semantics.

What the library **does** ship on the Compose side: `rememberSyntaxAnnotatedString` (memoization helper for the read-only path), `buildSyntaxStyledSpans` + `TextFieldBuffer.applySyntaxStyledSpans` (editable path), `buildSyntaxAnnotatedString` (pure function for non-Composable callers), and `SyntaxStyledSpan` as the styled-span type.

When considering a new Compose helper, the test is: does this expose a primitive the user couldn't easily get right themselves (e.g. multi-key `remember` chains with subtle staleness bugs), or does it just package primitives behind a friendlier-looking façade? Ship the first; reject the second.

## Single-Artifact Decision

SyntaxMP currently ships as one artifact even though the tokenizer layer is pure Kotlin and the text helpers depend on Compose. The current primary consumers use Compose, so splitting tokens from text would add build and API churn without a real consumer need.

If a non-Compose consumer materializes later:

- extract token model, engine, language tokenizers, scanners, routing, and spans into `syntaxmp-tokens`
- keep `syntaxmp` as the Compose text/theme artifact or as an umbrella depending on the migration plan
- preserve source compatibility for existing `syntaxmp` consumers where practical

Do not create this split speculatively.

## Adding a New Language

1. Pick a lowercase, hyphenless folder name under `languages/`, matching the `commonTest/.../languages/fixtures/<lang>/` fixture directory.
2. If the language has curated or inherited vocabulary, create `languages/<lang>/<Language>Lexicon.kt` with top-level `internal val` or `internal const val` declarations for keyword role maps, builtin role maps, constants, type names, directives, at-rules, raw-text tag names, embedded-language aliases, and role-specific word or prefix sets. Derived languages should re-export parent vals through their own lexicon. Do not create an empty marker lexicon for a language with neither curated nor inherited vocabulary.
3. Create `languages/<lang>/<Language>Tokenizer.kt` containing `internal object <Language>Tokenizer`, the language-owned options or scanner-input setup, and `fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult`.
4. If the language uses a shared engine scanner, build the language-owned scanner inputs in the tokenizer package and pass them to `engine/scanners/<family>/`. Shared engine scanners are for unrelated language families.
5. If the language needs a dedicated scanner, create `languages/<lang>/<Language>Scanner.kt` beside the tokenizer. Keep language-specific facts in the language package and read vocabulary from `<Language>Lexicon.kt`, not from shared engine scanner packages.
6. If the language is a dialect, keep a dialect tokenizer folder and import the primary scanner directly when it shares scanner mechanics. For example, SCSS/Less import `languages.css.CssScanner`, SQLite/PostgreSQL import `languages.sql.SqlScanner`, and Terraform imports `languages.hcl.HclScanner`. If a dialect has its own scanner, place that scanner in the dialect folder. Use the Shared Scanner Placement Rule to decide whether a dialect imports a primary scanner or owns its scanner locally.
7. Register the language in `engine/language/SyntaxLanguageId.kt` and add it to `SyntaxLanguageId.BuiltIns`.
8. Register routing in the built-in tokenizer registry under `engine/routing/`.
9. Add aliases only when the label is a true user-facing language alias.
10. Add fixture tests under `commonTest/.../languages/fixtures/<lang>/`.
11. Add or update the language row in `docs/internal/fixture-coverage.md`, recording covered, deferred, and not-applicable constructs.
12. Add or update the language section in `docs/languages.md` (aliases, `SyntaxLanguageId` constant, embedded languages if any, and the role table). If a tokenizer introduces a new root or refinement role constant, also update the tables in `docs/syntax-roles.md`.
13. Add or update the language row in `docs/internal/theme-color-policy.md`, including current roles, visual groups, default-theme impact, override recommendation, scope gaps, fixture gaps, and follow-up route.
14. Run the SyntaxMP validation commands listed in the Testing section.

## Build Notes

`settings.gradle.kts` uses `rootProject.name = "SyntaxMP"`. The published artifact
coordinate is `com.gallatinapps.syntaxmp:syntaxmp`. When SyntaxMP is consumed as an
included build, the consuming build may need to assign a distinct internal name to avoid
generated-accessor collisions with the `:syntaxmp` library module.

`local.properties` is local machine configuration and must not be committed.

## Testing

For SyntaxMP library changes:

```bash
../gradlew -p . :syntaxmp:jvmTest \
  :syntaxmp:compileKotlinJvm \
  :syntaxmp:compileAndroidMain \
  :syntaxmp:compileKotlinIosArm64 \
  :syntaxmp:compileKotlinIosSimulatorArm64 \
  :syntaxmp:compileKotlinWasmJs
```

For benchmark harness compile checks:

```bash
../gradlew -p . :syntaxmp-benchmarks:compileKotlinJvm
```

For an explicit local benchmark run:

```bash
../gradlew -p . :syntaxmp-benchmarks:runSyntaxMpBenchmarks
```

For demo changes:

```bash
../gradlew -p . :syntaxmp-demo:compileKotlinWasmJs :syntaxmp-demo:wasmJsBrowserTest
```

For the full web/demo validation pass:

```bash
../gradlew -p . :syntaxmp:jvmTest \
  :syntaxmp:compileKotlinJvm \
  :syntaxmp:compileAndroidMain \
  :syntaxmp:compileKotlinIosArm64 \
  :syntaxmp:compileKotlinIosSimulatorArm64 \
  :syntaxmp:compileKotlinWasmJs \
  :syntaxmp-demo:compileKotlinWasmJs \
  :syntaxmp-demo:wasmJsBrowserDistribution
```

Before handing off, run:

```bash
git diff --check
```
