# AGENTS.md — SyntaxMP

> Read this before changing SyntaxMP. SyntaxMP is a standalone, publishable Kotlin Multiplatform library.

## What SyntaxMP Is

`SyntaxMP` is a standalone Kotlin Multiplatform syntax highlighting library.

It currently contains two publishable modules and two non-published developer modules:

- `syntaxmp-tokenizer` - publishable pure Kotlin tokenizer artifact.
- `syntaxmp` - publishable Compose highlighter artifact; it api-depends on `syntaxmp-tokenizer`.
- `syntaxmp-demo` — local Wasm-only Compose demo app.
- `syntaxmp-benchmarks` — local JVM-only performance benchmark harness.

The tokenizer and Compose layers are deliberately split. `syntaxmp-tokenizer` owns the pure Kotlin tokenization machinery and built-in language tokenizers. `syntaxmp` owns the Compose text styling helpers and re-exports tokenizer types through its `api(project(":syntaxmp-tokenizer"))` dependency.

Current publication identity:

- Group: `com.gallatinapps.syntaxmp`
- Artifacts: `syntaxmp` and `syntaxmp-tokenizer`
- Coordinates: `com.gallatinapps.syntaxmp:syntaxmp:<version>` and `com.gallatinapps.syntaxmp:syntaxmp-tokenizer:<version>`
- Root package: `com.gallatinapps.syntaxmp`

The local build targets JVM, Android, `iosArm64`, `iosSimulatorArm64`, and web through
Kotlin/Wasm `wasmJs`.

## Module Scope

### `syntaxmp-tokenizer`

This module owns:

- `SyntaxTokenizer`
- `LanguageTokenizer`
- `TokenizeRequest`
- language identifiers, built-in aliases, built-in language sets, and extension overrides
- syntax roles, token spans, and tokenizer contracts
- lexical tokenizers for the built-in language set: Bash, C, C#, C++, CSS, CSV, Dart, Diff, Dockerfile, Dotenv, Go, GraphQL, HTML, INI, Java, JavaScript, JSON, JSX, Kotlin, Makefile, Markdown, PHP, PostgreSQL, PowerShell, Properties, Protobuf, Python, Ruby, Rust, Shell, SQL, SQLite, Swift, TOML, TSX, TypeScript, XML, YAML, and Zsh
- span normalization and shared scanner helpers

This module must not own:

- Compose `SpanStyle`, `AnnotatedString`, `TextFieldBuffer`, or other renderer-specific helpers
- host-app workspace, file, editor, navigation, autosave, export, or app-shell behavior
- app design-system tokens or Material 3 adapters
- language-server semantic highlighting, diagnostics, or decoration overlays unless a future change explicitly adds them

### `syntaxmp`

This module owns:

- the public `api(project(":syntaxmp-tokenizer"))` dependency that makes `syntaxmp` the one-line Compose coordinate
- Compose `SpanStyle`, `AnnotatedString`, and `TextFieldBuffer` helpers
- `SyntaxStyle`, `SyntaxRoleStyles`, `SyntaxTheme`, and its `DefaultLight` / `DefaultDark` starter themes

This module must not own:

- pure tokenizer implementation, built-in language tokenizers, scanner primitives, routing, or span normalization
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

SyntaxMP source packages are split by module. The `engine/` umbrella is gone; the `:syntaxmp-tokenizer` module name now carries that identity.

```text
:syntaxmp-tokenizer
├── language/    # LanguageId, LanguageExtension, label normalization
├── role/        # SyntaxRole and role-path helpers
├── tokenizer/   # SyntaxTokenizer, LanguageTokenizer, TokenizeRequest
├── spans/       # SyntaxTokenSpan, span normalization, and embedded-span helpers
├── primitives/  # low-level reusable scanner building blocks
│   ├── strings/ # string, raw string, heredoc, interpolation, format, and prefix rules
│   └── numbers/ # numeric literal scanners and modes
├── routing/     # internal built-in/extension tokenizer routing
├── scanners/    # shared family scanners such as clike, script, and markup
└── builtins/    # one package per built-in language

:syntaxmp
└── compose/       # Compose text helpers
    └── theme/     # SyntaxStyle, SyntaxRoleStyles, SyntaxTheme
```

Package ownership rules:

1. The root `com.gallatinapps.syntaxmp` namespace has no flat Kotlin files. New pure tokenizer code belongs under `language/`, `role/`, `spans/`, `tokenizer/`, `primitives/`, `scanners/`, `routing/`, or `builtins/` in `:syntaxmp-tokenizer`. New Compose code belongs under `compose/` or `compose/theme/` in `:syntaxmp`.
2. Public pure SyntaxMP APIs live under `language/`, `role/`, `spans/`, and `tokenizer/`. Public Compose APIs live in `compose/` or `compose/theme/`.
3. New built-in languages get a lowercase, hyphenless package under `builtins/<normalized-id>/`.
4. A language package owns its tokenizer, lexicon, language-specific scanner options, dedicated scanner, and language fixture tests.
5. A scanner remains under `scanners/` only if at least two unrelated language packages use it and it has no consuming-language names or lexicons. Reuse by dialects/extensions of one primary language does not qualify.
6. Primary-language family scanners live in the primary language package. CSS owns `CssScanner`; SQL owns `SqlScanner`; shell-family public identities can import `ShellTokenizer` while keeping their own language packages.
7. Dialect/extension language packages keep their own tokenizer folder even when they import a primary language scanner. SQLite/PostgreSQL import `SqlScanner`; other dialects should import a primary scanner only when the shared mechanics are still owned by an active built-in language.
8. Family scanner data belongs to the primary language package unless the scanner mechanics are genuinely language-agnostic.
9. `scanners/markup/` is the single shared home for markup parsing across HTML, XML, JSX, and TSX. Component behavior is configured through `MarkupScannerOptions`; `scanners/component/` no longer exists.
10. Shared scanner primitives live under `primitives/`. They may reference generic syntax concepts and reusable lexical shapes only.
11. Do not add broad `ScannerOptions.kt`, `Models.kt`, `Helpers.kt`, or `Support.kt` files when a precise file name exists.
12. Small related model clusters can live together. For example, comment options can share one file; a sealed interface can live with its small implementations.
13. Numeric literal support belongs under `primitives/numbers/`, string-literal support under `primitives/strings/`, and qualified-name/comment/identifier/brace primitives live flat under `primitives/`.
14. Embedded-language routing is request-based. Tokenizers call `TokenizeRequest.tokenizeEmbedded(...)`, span offset helpers live under `spans/`, and markup raw-text label resolution lives under `scanners/markup/`. Do not add a separate embedded-language engine package or a public callback type for this plumbing.
15. Tests mirror source ownership: language fixtures under `builtins/fixtures/<language>/`, shared tokenizer mechanics under the matching top-level tokenizer package, and Compose tests under `compose/`.

Do not introduce host-app-specific naming prefixes or packages in SyntaxMP. Markdown is just one supported language here; library types stay library-centric.

## Tokenizer, Scanner, Options, And Lexicon Ownership

Use these definitions when adding or moving language code:

- A `Tokenizer` is the built-in language entrypoint. Built-in tokenizer objects implement `LanguageTokenizer`, receive `TokenizeRequest`, choose scanner mechanics, pass language vocabulary and scanner options into the scanner, and return `List<SyntaxTokenSpan>`. Internal language tokenizer objects are named `<Language>Tokenizer` under `builtins/<language>/`; do not include the redundant `Syntax` middle in those internal names.
- A `Scanner` is the lexical walker that emits token spans. Shared scanners under `scanners/` contain mechanics only and must not contain consuming-language names, aliases, keyword sets, or dialect policy. Dedicated scanners live beside their tokenizer under `builtins/<language>/`, but they still receive language vocabulary from tokenizer-wired constructor parameters rather than hiding semantic word tables inside scanner code.
- Scanner `Options` are immutable mechanics configuration passed to a scanner when it has several reusable behaviors: comment forms, literal rules, number scanners, identifier rules, role mappings, and embedded-language policy functions. Options do not carry vocabulary. Use direct scanner inputs for narrow one-off settings instead of adding an empty or single-use standard options wrapper.
- A `Lexicon` file is language-owned vocabulary and string-table data: keyword role maps, builtin role maps, constants, type names, directives, at-rules, raw-text tag names, embedded-language aliases, and role-specific word or prefix sets. When a language has curated vocabulary, put top-level `internal val` or `internal const val` declarations in `<Language>Lexicon.kt`; use `LexemeRoleMap`/`lexemeRoleMap(...)` when the vocabulary bucket owns per-lexeme roles. Languages with no curated vocabulary do not need marker lexicon files. Literal delimiters, interpolation mechanics, number suffix parsing, and scanner routing stay in tokenizers/options unless they are themselves language vocabulary.
- Tokenizers only reference their own language's lexicon. Derived languages have their own lexicon file that inherits relevant vals from the parent when they have curated or inherited vocabulary, such as JSX/TSX raw-text tags inherited from HTML.
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
| `*Request` | Per-call input packet. | `StringLiteralRequest`, `TokenizeRequest` |
| `*Result` | Runtime output packet from matching/finding/scanning. | `PrefixResult`, `ClosingResult` |
| `*Map` | Static lookup data that classifies already-read lexemes or symbols. | `LexemeRoleMap` |
| `*Mode` | Enum-like behavior choice. | `EscapeMode`, `OctalMode`, `HeredocMode` |

Retired suffixes for primitives are `*Lexer`, `*Matcher`, `*Spec`, `*Variant`, `*Style`, `*Descriptor`, and `*Match`. Rename new or touched primitive code to the active vocabulary instead of extending those older names.

Primitive verbs follow the type role:

- Scanners use `scan(...)` for their principal method.
- Rules use `tryMatch(...)` for position-tested pattern matching.
- Scope methods that delegate to scanners may use `scan<Thing>(...)`, such as `scanNestedCode(...)`.
- Plain helpers use direct verbs such as `find...`, `is...`, and `roleFor...`.

Do not introduce a named `fun interface` for a one-off internal decision callback. Prefer a named function property when the type is only "given already-read input, choose this value", such as `escapeModeForPrefix`, `tripleEscapeModeForPrefix`, or `interpolationRulesForPrefix`.

## Public Naming Convention

The `Syntax` prefix is used where it earns its keep as the domain object or value being passed around: `SyntaxTokenizer`, `SyntaxRole`, `SyntaxTokenSpan`, `SyntaxStyle`, and `SyntaxTheme`.

Use `Language*` for language setup and authoring concepts: `LanguageId`, `LanguageExtension`, and `LanguageTokenizer`.

Use the plain `TokenizeRequest` name for the per-call input packet; the `tokenizer` package supplies the context. There is no public tokenize-result wrapper. Tokenizers return `List<SyntaxTokenSpan>` directly, and `SyntaxTokenizer.tokenize(...)` returns the normalized list directly.

## Public API Expectations

Important public pure API surfaces live in `:syntaxmp-tokenizer`:

- `com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer`
- `com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer`
- `com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest`
- `com.gallatinapps.syntaxmp.language.LanguageId`
- `com.gallatinapps.syntaxmp.language.LanguageExtension`
- `com.gallatinapps.syntaxmp.role.SyntaxRole`
- `com.gallatinapps.syntaxmp.role.rolePathValuesFromRoot`
- `com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan`

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

The authoritative public API reference lives in [`docs/api.md`](docs/api.md). **Any change that adds, removes, renames, or alters the signature of a public symbol must update `docs/api.md` in the same change.** This includes adding a built-in language constant on `LanguageId.Companion`, adding a role to `SyntaxRole`, adding a copy/override helper, or any new public function/class/extension under the public tokenizer packages, `com.gallatinapps.syntaxmp.compose`, or `com.gallatinapps.syntaxmp.compose.theme`. The doc is the curated index of the intended public surface; source remains the final source of truth, but drift between the two should be closed in the same PR.

Theme-author role documentation is split across two files: [`docs/syntax-roles.md`](docs/syntax-roles.md) (the roles primer: root and refinement constants, custom-role factories) and [`docs/languages.md`](docs/languages.md) (the per-language catalog of emitted roles, aliases, `LanguageId` constants, and embedded-language routing). Update `docs/languages.md` whenever built-in tokenizers add, remove, or rename emitted roles; update `docs/syntax-roles.md` when root or refinement constants change.

Fixture coverage documentation lives in [`docs-internal/fixture-coverage.md`](docs-internal/fixture-coverage.md). Update it whenever tokenizer behavior or fixture coverage changes.


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
- A `rememberSyntaxTokenizer` helper. Engine scope is a host decision (per-screen `remember`, host-defined `staticCompositionLocalOf`, or a DI container); the library shouldn't pre-bake a Composable-scoped default that masks that decision.
- A `CompositionLocal` or provider for the engine or the theme (see the Theme Shape section). Hosts own scoping, lifecycle, and provider semantics.

What the library **does** ship on the Compose side: `rememberSyntaxAnnotatedString` (memoization helper for the read-only path), `buildSyntaxStyledSpans` + `TextFieldBuffer.applySyntaxStyledSpans` (editable path), `buildSyntaxAnnotatedString` (pure function for non-Composable callers), and `SyntaxStyledSpan` as the styled-span type.

When considering a new Compose helper, the test is: does this expose a primitive the user couldn't easily get right themselves (e.g. multi-key `remember` chains with subtle staleness bugs), or does it just package primitives behind a friendlier-looking façade? Ship the first; reject the second.

## Published Artifact Shape

SyntaxMP ships two published artifacts:

- `syntaxmp-tokenizer`: pure Kotlin tokenization layer for token-only or bring-your-own-renderer consumers.
- `syntaxmp`: Compose highlighter layer and the headline quick-start coordinate. It api-depends on `syntaxmp-tokenizer`, so a `syntaxmp` consumer can see tokenizer types used by public Compose signatures.

Both coordinates must be published. The published POM for `syntaxmp` references `syntaxmp-tokenizer`, so consumers cannot resolve `syntaxmp` unless the tokenizer coordinate is available too.

Do not add speculative umbrella or per-language artifacts. Additional renderers, if they are ever added, should be separate coordinates that depend on `syntaxmp-tokenizer`.

## Release Prep

Before cutting a release:

1. Set the root project version in `build.gradle.kts` to the exact release version, with no `-SNAPSHOT` suffix.
2. Update `CHANGELOG.md` with the release date, notable changes, and compare links for the new tag.
3. Update public install/version references in `README.md`, including the version badge and version catalog snippet.
4. Update demo-facing install snippets and displayed dependency versions, especially `syntaxmp-demo/src/commonMain/kotlin/com/gallatinapps/syntaxmp/demo/panes/GetStartedSamples.kt`.
5. Search for stale snapshot or previous-version references in Gradle files, docs, README content, demo resources, and demo Kotlin samples.
6. Verify both published coordinates are still configured for the same version: `com.gallatinapps.syntaxmp:syntaxmp` and `com.gallatinapps.syntaxmp:syntaxmp-tokenizer`.
7. Run the full web/demo validation pass from the Testing section, plus `git diff --check`, before handing off the release prep.

## Adding a New Language

1. Pick a lowercase, hyphenless folder name under `builtins/`, matching the `commonTest/.../builtins/fixtures/<lang>/` fixture directory.
2. If the language has curated or inherited vocabulary, create `builtins/<lang>/<Language>Lexicon.kt` with top-level `internal val` or `internal const val` declarations for keyword role maps, builtin role maps, constants, type names, directives, at-rules, raw-text tag names, embedded-language aliases, and role-specific word or prefix sets. Derived languages should re-export parent vals through their own lexicon. Do not create an empty marker lexicon for a language with neither curated nor inherited vocabulary.
3. Create `builtins/<lang>/<Language>Tokenizer.kt` containing `internal object <Language>Tokenizer : LanguageTokenizer`, the language-owned options or scanner-input setup, and `override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan>`.
4. If the language uses a shared engine scanner, build the language-owned scanner inputs in the tokenizer package and pass them to `scanners/<family>/`. Shared engine scanners are for unrelated language families.
5. If the language needs a dedicated scanner, create `builtins/<lang>/<Language>Scanner.kt` beside the tokenizer. Keep language-specific facts in the language package and read vocabulary from `<Language>Lexicon.kt`, not from shared engine scanner packages.
6. If the language is a dialect or distinct public identity, keep a tokenizer folder for that identity and import the primary scanner directly when it shares scanner mechanics. For example, SQLite/PostgreSQL import `builtins.sql.SqlScanner`, and Bash/Zsh reuse the shell scanner through their own packages. Use the Shared Scanner Placement Rule to decide whether a dialect imports a primary scanner or owns its scanner locally.
7. Register the language in `language/LanguageId.kt` and add it to `LanguageId.BuiltIns`.
8. Register routing in the built-in tokenizer registry under `routing/`.
9. Add aliases only when the label is a true alternate label for the same public language identity. If a label names a format or language that may later specialize, give it its own `LanguageId` and tokenizer package or leave it unsupported.
10. Add fixture tests under `commonTest/.../builtins/fixtures/<lang>/`.
11. Add or update the language row in `docs-internal/fixture-coverage.md`, recording covered, deferred, and not-applicable constructs.
12. Add or update the language section in `docs/languages.md` (aliases, `LanguageId` constant, embedded languages if any, and the role table). If a tokenizer introduces a new root or refinement role constant, also update the tables in `docs/syntax-roles.md`.
13. Run the SyntaxMP validation commands listed in the Testing section.

## Build Notes

`settings.gradle.kts` uses `rootProject.name = "SyntaxMP"`. Published artifact
coordinates are `com.gallatinapps.syntaxmp:syntaxmp` and
`com.gallatinapps.syntaxmp:syntaxmp-tokenizer`. When SyntaxMP is consumed as an included
build, the consuming build may need to assign a distinct internal name to avoid
generated-accessor collisions with the `:syntaxmp` library module.

`local.properties` is local machine configuration and must not be committed.

## Testing

For pure tokenizer library changes:

```bash
./gradlew :syntaxmp-tokenizer:jvmTest \
  :syntaxmp-tokenizer:compileKotlinJvm \
  :syntaxmp-tokenizer:compileAndroidMain \
  :syntaxmp-tokenizer:compileKotlinIosArm64 \
  :syntaxmp-tokenizer:compileKotlinIosSimulatorArm64 \
  :syntaxmp-tokenizer:compileKotlinWasmJs
```

For Compose library changes:

```bash
./gradlew :syntaxmp:jvmTest \
  :syntaxmp:compileKotlinJvm \
  :syntaxmp:compileAndroidMain \
  :syntaxmp:compileKotlinIosArm64 \
  :syntaxmp:compileKotlinIosSimulatorArm64 \
  :syntaxmp:compileKotlinWasmJs
```

For benchmark harness compile checks:

```bash
./gradlew :syntaxmp-benchmarks:compileKotlinJvm
```

For an explicit local benchmark run:

```bash
./gradlew :syntaxmp-benchmarks:runSyntaxMpBenchmarks
```

For demo changes:

```bash
./gradlew :syntaxmp-demo:compileKotlinWasmJs :syntaxmp-demo:wasmJsBrowserTest
```

For the full web/demo validation pass:

```bash
./gradlew :syntaxmp-tokenizer:jvmTest \
  :syntaxmp-tokenizer:compileKotlinJvm \
  :syntaxmp-tokenizer:compileAndroidMain \
  :syntaxmp-tokenizer:compileKotlinIosArm64 \
  :syntaxmp-tokenizer:compileKotlinIosSimulatorArm64 \
  :syntaxmp-tokenizer:compileKotlinWasmJs \
  :syntaxmp:jvmTest \
  :syntaxmp:compileKotlinJvm \
  :syntaxmp:compileAndroidMain \
  :syntaxmp:compileKotlinIosArm64 \
  :syntaxmp:compileKotlinIosSimulatorArm64 \
  :syntaxmp:compileKotlinWasmJs \
  :syntaxmp-demo:compileKotlinWasmJs \
  :syntaxmp-demo:wasmJsBrowserDistribution \
  :syntaxmp-benchmarks:compileKotlinJvm
```

Before handing off, run:

```bash
git diff --check
```
