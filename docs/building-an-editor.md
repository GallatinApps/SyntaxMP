# Building a Code Editor

This guide is for building an editable code surface with SyntaxMP: a `BasicTextField` (or similar Compose editable text component) that highlights its contents as the user types. It covers the editable render path, how to share one tokenizer engine across an app, why `buildSyntaxStyledSpans` splits at line breaks, what to do (and not do) on large documents, and how caching fits in.

## The three layers

SyntaxMP's render pipeline has three layers, and consumers can plug in at any of them:

```
engine.tokenize(code, languageLabel)
  → List<SyntaxTokenSpan>          ← cache here (theme-independent)
      │
      ├─ + theme → buildSyntaxAnnotatedString → AnnotatedString → BasicText
      │   (or rememberSyntaxAnnotatedString from a Composable)
      │
      └─ + theme → buildSyntaxStyledSpans → List<SyntaxStyledSpan>
                                              │
                                              └─ TextFieldBuffer.applySyntaxStyledSpans → BasicTextField
```

The cache layer is the right place to memoize. `SyntaxTokenSpan` is theme-independent: toggle dark/light mode and the cache is still valid. The downstream conversions (`buildSyntaxAnnotatedString` and `buildSyntaxStyledSpans`) are cheap and need to re-run when the theme changes anyway.

`SyntaxMP` deliberately doesn't ship a cache. Cache shape (key, eviction, max-entries) is
consumer-specific. See the [Caching](#caching) section below.

## The read-only path

For simple read-only snippets, prefer `rememberSyntaxAnnotatedString(...)` instead of hand-writing
the tokenization and `AnnotatedString` remember chain:

```kotlin
val highlighted = rememberSyntaxAnnotatedString(
    code = code,
    languageLabel = languageLabel,
    engine = engine,
    theme = theme,
)

BasicText(text = highlighted)
```

The helper remembers tokenization on `(engine, code, languageLabel)`, then remembers styling on
`(code, spans, theme)`. Theme changes restyle the cached token spans without re-tokenizing, while
content or language changes produce a fresh tokenization result.

## The editable path

Compose's editable text APIs own the string content, so you can't hand them an `AnnotatedString`. Instead, build a `List<SyntaxStyledSpan>` with `buildSyntaxStyledSpans` and apply it through `TextFieldBuffer.applySyntaxStyledSpans` inside a `BasicTextField` `outputTransformation`.

```kotlin
@Composable
fun CodeField(
    state: TextFieldState,
    languageLabel: String?,
    engine: SyntaxTokenizer,
    theme: SyntaxTheme,
) {
    BasicTextField(
        state = state,
        outputTransformation = {
            val code = asCharSequence().toString()
            val tokens = engine.tokenize(code = code, languageLabel = languageLabel)
            val spans = buildSyntaxStyledSpans(
                code = code,
                spans = tokens,
                theme = theme,
            )
            applySyntaxStyledSpans(spans)
        },
        textStyle = TextStyle(fontFamily = FontFamily.Monospace),
    )
}
```

`buildSyntaxStyledSpans` splits styled spans at every `\n` and `\r\n` so no produced span straddles a line. See [why](#why-buildsyntaxstyledspans-splits-at-line-breaks) below.

For production document editors, tokenize into host state keyed by document revision, then have
`outputTransformation` build and apply styled spans from the latest matching token snapshot. That
keeps buffer mutation in `outputTransformation` and tokenization in host state where you control its
lifecycle.

## Sharing the engine

`SyntaxTokenizer` is the only stateful object in SyntaxMP, and its state is the precomputed routing map for built-ins plus your normalized extensions. Once constructed it's immutable and safe to share across compositions and threads.

SyntaxMP deliberately doesn't ship a Composable helper for engine construction or a `CompositionLocal` for engine threading. Engine scope is a host-app decision. Most Compose apps should create one engine for the app's syntax configuration and pass it through their own app wiring.

**CompositionLocal.** A host-owned `staticCompositionLocalOf` is the straightforward Compose pattern when many surfaces need the same engine. Define the local in your app, construct the engine once near the root, and read it at call sites that need to tokenize:

```kotlin
val LocalAppSyntaxEngine = staticCompositionLocalOf<SyntaxTokenizer> {
    error("LocalAppSyntaxEngine not provided")
}

@Composable
fun AppSyntaxProvider(
    extensions: List<LanguageExtension> = emptyList(),
    content: @Composable () -> Unit,
) {
    val engine = remember(extensions) {
        SyntaxTokenizer(extensions = extensions)
    }

    CompositionLocalProvider(LocalAppSyntaxEngine provides engine) {
        content()
    }
}

// at a call site
val engine = LocalAppSyntaxEngine.current
```

`staticCompositionLocalOf` is the right flavor for this case because the engine is immutable and should rarely change after the app syntax configuration is chosen.

**One surface.** If one composable owns the only syntax-highlighted surface, construct the engine with `remember`. When extensions are dynamic inputs, key the `remember` on those inputs:

```kotlin
val engine = remember(extensions) {
    SyntaxTokenizer(extensions = extensions)
}
```

**Dependency injection.** Apps that already use DI can create one `SyntaxTokenizer` in that container and inject or pass it to Compose call sites. SyntaxMP APIs take the engine as a parameter, so no SyntaxMP-specific adapter is required.

Engine construction precomputes routing. Build the engine once for the scope that owns your syntax configuration, then reuse it for tokenization. Do not construct a fresh engine inside `outputTransformation` or another per-keystroke path.

## Why `buildSyntaxStyledSpans` splits at line breaks

Compose's `TextFieldBuffer.addStyle` misbehaves when a styled range crosses a `\n`: the styling gets applied unevenly across the wrapped lines or drops out entirely. `buildSyntaxStyledSpans` defends against this by splitting every styled run at line breaks, so each produced `SyntaxStyledSpan` covers a single line at most.

The practical consequence: a block comment that spans five lines becomes five styled spans, one per line. Rendered output looks the same; the difference is purely in how Compose stores the styling.

The read-only `buildSyntaxAnnotatedString` does *not* split, because `AnnotatedString` consumers (`BasicText`) handle multi-line ranges correctly. Don't feed unsplit output into a `TextFieldBuffer`, and don't bother running editor-split output through `AnnotatedString`. Use each function for its intended path.

## Handling large documents

Tokenization is not the expensive part, so tokenize the whole document every time. A Compose `remember` keyed on `(engine, code, languageLabel)` avoids re-tokenizing on every recomposition. What grows with document size is Compose text layout, not the scan.

### Displaying

Read-only display scales: tokenize once, build the `AnnotatedString`, and render it in `BasicText`. If a single very large `BasicText` becomes expensive to lay out, split the document into contiguous sections (for example by line range) and render each in its own `BasicText` inside a `LazyColumn`, so Compose only lays out the visible sections. Tokenize the whole document first and slice the resulting spans per section; do not tokenize sections independently, or multi-line constructs that cross a boundary are mis-scanned.

### Editing

Editing is where document size bites, and the cause is `BasicTextField`: it does not virtualize text layout, so every keystroke re-measures the entire document's text to place the cursor. That base layout cost is unavoidable and grows with document size, and an editable field cannot be chunked into a `LazyColumn` the way read-only display can, because it is one editing surface with a single cursor and selection.

The styling layered on top is separate, and the host can reduce it: building styled spans, applying them to the buffer, and the styled-paragraph work they induce all scale with how many spans cover how much of the document. Three techniques help on large editable documents:

- **Window the applied spans.** Build and apply styled spans only for the visible line range plus a small margin, not the whole document. Styling changes appearance, not text content, so a windowed set lays out the same as the full set and causes no caret or scroll jumps. (Weight and italic styles can nudge glyph metrics, so window by whole lines.) Fewer applied spans means less per-keystroke styling work.
- **Tokenize off the main thread.** Hold the latest tokens in host state keyed by revision, debounce re-tokenization, and run the scan on a background dispatcher. Between scans, shift the cached span offsets by the edit delta so colors track typing without a fresh scan.
- **Expand when idle.** Once typing settles, style the whole document so scrolling becomes a pure relayout that translates existing styling instead of restyling per scroll step. Optionally defer restyling while a scroll is in progress and snap when it stops.

These keep large files usable but do not remove the whole-document layout, so very large documents still cost more per keystroke than small ones.

## Caching

`engine.tokenize(...)` is pure, so caching is the host's choice and SyntaxMP ships no cache. The only safe key is `(engine, language label, content revision or hash) -> List<SyntaxTokenSpan>`. Spans are theme-independent, so a theme toggle does not invalidate them.

Inside a single editor a cache buys little: the buffer changes on every keystroke, so each edit is a miss, and spans tokenized from one revision must never be applied to a different buffer revision or the ranges land in the wrong place. Caching pays off across surfaces rather than within one, for example paging between open documents or a preview pane re-showing source you already tokenized.

For read-only call sites, `rememberSyntaxAnnotatedString(...)` already remembers tokenization on `(engine, code, languageLabel)` and styling separately, which is all most apps need.

## Where to go next

- [docs/theming.md](theming.md): what to do with the spans `buildSyntaxStyledSpans` produces, and how the resolver turns `SyntaxRole` plus `LanguageId` into a `SpanStyle`.
- [docs/embedded-languages.md](embedded-languages.md): what gets routed to which embedded language automatically inside HTML, Markdown, JSX, and TSX.
- [docs/architecture.md](architecture.md): where the editable path sits in the broader pipeline.
- [docs/language-extension.md](language-extension.md): adding a custom language whose spans will flow through everything above.
