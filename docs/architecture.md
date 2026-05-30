# SyntaxMP Architecture

SyntaxMP turns a `(code, languageLabel)` pair into a list of styled text spans you can drop into a Compose UI. Its scope is intentionally narrow: lexical syntax highlighting only. It does not parse, type-check, resolve symbols, run diagnostics, auto-detect languages, or render text. Those belong to the host application.

This document explains the shape of the library: the pipeline a snippet travels through, where state lives, what runs on which platforms, and what scanner-based highlighting can and cannot do.

For a copy-paste-ready usage path, read the [README](../README.md) Quick Start first; this document is the "how it works" companion.

## The pipeline

A call from a host application travels through six stages. Stages 1–5 are pure Kotlin and platform-agnostic. Stage 6 produces Compose text types.

```
host: (code: String, languageLabel: String?)
  │
  ▼
[1] Language resolution                     SyntaxTokenizerEngine.resolveLanguageId
  │      └─ extensions → aliases → built-ins → exact custom id
  ▼
[2] Engine routing                          SyntaxTokenizerEngine.tokenize
  │      └─ resolveTokenizer()              extensions → built-ins
  ▼
[3] Per-language tokenizer                  languages/<lang>/<Lang>Tokenizer
  │      └─ drives a scanner with vocabulary + options
  ▼
[4] Scanner                                 engine/scanners/* or languages/<lang>/*Scanner
  │      └─ single-pass char state machine; may recurse into embedded languages
  │      → raw List<SyntaxTokenSpan> (possibly overlapping/out-of-order)
  ▼
[5] Span normalization                      engine/spans/TokenSpanNormalizer
  │      → clean List<SyntaxTokenSpan>      (engine.tokenize boundary)
  │
  ▼
[6] Theme + Compose styling                 compose/, compose/theme/
         └─ SyntaxTheme.resolveSpanStyle(span) → SpanStyle
         → AnnotatedString  or  List<SyntaxStyledSpan>
```

### Stage 1: Language resolution

The engine takes a raw nullable language label. It normalizes the label, checks registered extension language ids and extension aliases first, then built-in aliases (`kt` → `Kotlin`, `tf` → `Terraform`, …), then returns an exact custom language for unknown non-blank labels. Blank or null input returns `null`.

Hosts that need the canonical `SyntaxLanguageId` before tokenizing can call `engine.resolveLanguageId(label)`. Otherwise, pass the same raw label directly to `engine.tokenize(code, label)`. SyntaxMP still does not auto-detect; it resolves only the label the host provides.

### Stage 2: Engine routing

`SyntaxTokenizerEngine` is the only stateful, configured object a host constructs. At construction it takes:

- `builtInLanguages: Set<SyntaxLanguageId>`: built-ins enabled for this engine (default: `SyntaxLanguageId.BuiltIns`, all built-ins);
- `extensions: List<SyntaxLanguageExtension>`: host-supplied tokenizers, checked before built-ins.

`tokenize(code, languageLabel)` is a pure function. It resolves the raw label, then calls `resolveTokenizer(languageId)`, which walks extensions first and then the built-in tokenizer map. Extensions can therefore override a built-in language. An unregistered, blank, or `null` label returns an empty span list; empty code does the same. Tokenizer throws degrade to zero spans so a buggy tokenizer cannot crash text rendering.

### Stage 3: Per-language tokenizer

Each built-in language is an `internal object <Language>Tokenizer` under `languages/<lang>/`. The tokenizer receives a `SyntaxTokenizeRequest(code, languageId)` and returns a `SyntaxTokenizeResult(spans)`. Its job is to wire language-specific vocabulary (keywords, builtins, type names, directive sets, held in `<Language>Lexicon.kt`) and scanner options (literal forms, comment shapes, number rules) into the right scanner. Adding or tuning a language is usually editing a tokenizer plus a lexicon: the scanner mechanics stay the same.

Host-supplied tokenizers implement `SyntaxTokenizer` directly. There is no internal contract they have to match. They receive the same `SyntaxTokenizeRequest` and produce the same `SyntaxTokenizeResult` shape.

### Stage 4: Scanner

A scanner is a hand-written `while (index < code.length)` loop. Each iteration looks at the current character (and a few characters of lookahead) and dispatches to a `scanX(start): Int` handler that emits zero or more `SyntaxTokenSpan` entries and returns the next index. There is no regex, no backtracking, and no grammar runtime. Scanners are forward lexical walks with deterministic behavior. Cost scales with code length, though language-specific constructs and embedded-language routing make some languages and source shapes heavier than others, so performance-sensitive hosts should measure their actual inputs.

Two shared scanners back most languages: `CLikeScanner` (compiled brace languages with preprocessor, annotations, raw/triple/interpolated strings, C-style numbers) and `ScriptLikeScanner` (dynamic `#`-comment languages with regex literals, template strings, identifiers). CSS-family and markup-family scanners are similarly shared: `engine/scanners/markup/` is the single home for HTML / XML / JSX / TSX / Vue / Svelte / Astro / MDX. Vocabulary stays with the language package, not with the shared scanner.

Scanners may emit spans that overlap, sit out of order, or temporarily run past the end of the code. The normalizer cleans that up. They may also recurse into embedded languages (see [docs/embedded-languages.md](embedded-languages.md)) through the `SyntaxTokenizeRequest.tokenizeEmbedded(...)` affordance and scanner helpers that offset child spans into the host code. Routed child labels use the same engine resolver as top-level labels, and the engine caps recursion depth at three so a pathological host language cannot loop forever through embedded content.

### Stage 5: Span normalization

`engine/spans/TokenSpanNormalizer` cleans up tokenizer output before it leaves the engine. It:

1. Clips every span to `[0, codeLength]` and drops empty ranges.
2. Uses a fast path for already-sorted, non-overlapping spans, merging adjacent output segments
   that share the same role *and* the same language id.
3. Falls back to a deterministic sweep for overlapping, out-of-order, or clipped spans.
4. When spans overlap, picks a winner by comparator: shortest span wins (most specific), then
   deepest dotted role (more refined wins), then earliest emission index.

A `string.regex` inside a `string`, or an interpolation marker inside a multi-line string, therefore wins its sub-range without the scanner needing to know how to carve holes in the broader span.

`SyntaxTokenizerEngine.tokenize(...)` returns the normalized `List<SyntaxTokenSpan>` directly; there is no wrapper type. Each span carries `start`, `endExclusive`, a `SyntaxRole`, and the resolved `SyntaxLanguageId` that produced it. The list is the engine output boundary. Stages 1 through 5 are plain Kotlin with no Compose dependency; stage 6 is where Compose types enter.

### Stage 6: Theme resolution and Compose styling

This is where roles become colors. `SyntaxTheme.resolveSpanStyle(span)` walks the role's parent chain root-to-exact (e.g. for `keyword.declaration`: first `keyword`, then `keyword.declaration`), merging each step's `SyntaxStyle` from the global `roleStyles`. It then walks the same chain against the span's matching `languageOverrides[span.languageId]`, merging again. Unset roles contribute nothing; the host's Compose `TextStyle` remains visible for plain text and for any role the theme doesn't style. See [docs/theming.md](theming.md) for worked examples.

The Compose layer exposes two output shapes:

- `buildSyntaxAnnotatedString(code, spans, theme)` returns an `AnnotatedString` for the read-only render path; pass it to `BasicText`. From a Composable, prefer the `rememberSyntaxAnnotatedString` wrapper, which chains `engine.tokenize` and `buildSyntaxAnnotatedString` with the correct `remember` keys.
- `buildSyntaxStyledSpans(code, spans, theme)` returns a `List<SyntaxStyledSpan>` and splits every styled run at `\n`/`\r\n` so no style straddles a line. Editable buffers need this. Apply via `TextFieldBuffer.applySyntaxStyledSpans(...)` inside a Compose `outputTransformation`. See [docs/building-an-editor.md](building-an-editor.md).

## Where state lives

| Lifetime | What it holds | Owner |
|---|---|---|
| Per-call | The code string, the raw label, the resolved language id, the resulting spans. Pure inputs, pure outputs. | Caller's stack frame |
| Per-engine | Enabled built-in set, normalized extensions, the precomputed tokenizer route map. Immutable after construction. | `SyntaxTokenizerEngine` instance, usually a single per-app value |
| Per-host | The active `SyntaxTheme`, the surrounding `TextStyle`, and any app-level engine provider. | Application (SyntaxMP ships no `CompositionLocal` for engine or theme values) |

SyntaxMP has no internal cache. `engine.tokenize(...)` is a pure function of `(code, languageLabel)`, so any caching policy belongs to the host. A `remember(engine, code, languageLabel) { engine.tokenize(...) }` is usually enough for Compose call sites. See [docs/building-an-editor.md](building-an-editor.md) for engine sharing and caching.

The local developer benchmark harness lives in `syntaxmp-benchmarks`. It is intentionally explicit:
it is not wired into normal `check`, and generated reports under `syntaxmp-benchmarks/build/` are
build output unless a maintainer deliberately captures a reviewed baseline. See
[`docs/internal/performance-benchmarking.md`](internal/performance-benchmarking.md) for task names
and report interpretation.

## Cross-platform posture

SyntaxMP targets **JVM**, **Android**, **iOS arm64**, **iOS simulator arm64**, and **web through Kotlin/Wasm**. All source lives in `commonMain`: there are no `expect`/`actual` declarations, no platform-specific source sets, and no JS or native bridges. Stages 1 through 5 are plain Kotlin standard-library code; stage 6 depends on Compose Multiplatform's text types and runs identically across all targets the host supports. The same scanner code produces the same spans on every target.

## Limitations

- *Anything that needs real parsing.* No symbol resolution, no type information, no diagnostic spans, no semantic highlighting. Heuristics handle context-sensitive cases like "is `/foo/` a regex literal or division?" but cannot recover when local context isn't enough.
- *Deep or context-sensitive nesting.* Heredocs, attribute-value embedded languages, tagged template literals, and template languages embedded in HTML are either approximated or deliberately out of scope. See [docs/embedded-languages.md](embedded-languages.md) for the full list.
- *Auto-detection and language guessing.* The engine takes a language label or nothing. Guessing is a separate problem with different correctness and performance tradeoffs, and SyntaxMP doesn't ship one.

## Where to go next

- [README Quick Start](../README.md#quick-start): the smallest possible usage path.
- [docs/syntax-roles.md](syntax-roles.md): the roles primer (root and refinement constants, custom-role factories). Theme authors should start here.
- [docs/languages.md](languages.md): the per-language catalog of roles each built-in tokenizer emits, plus aliases, `SyntaxLanguageId` constants, and embedded-language routing.
- [docs/theming.md](theming.md): the theming reference. Role tree, resolution policy, language overrides, copy/override helpers.
- [docs/language-extension.md](language-extension.md): adding a custom language via `SyntaxLanguageExtension`.
- [docs/building-an-editor.md](building-an-editor.md): read-only vs. editable render paths, caching, and large-document guidance.
- [docs/embedded-languages.md](embedded-languages.md): what the engine routes automatically for HTML, Markdown, JSX/TSX/MDX, Vue, Svelte, and Astro, and what it deliberately doesn't.
