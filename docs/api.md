# SyntaxMP API Reference

A reference for every public symbol SyntaxMP ships. Each entry shows the declaration, what the symbol is for, how it behaves, and where it fits with the rest of the API.

For a guided walk through the library rather than a per-symbol lookup, start with [architecture.md](architecture.md). For the worked Compose paths, see [building-an-editor.md](building-an-editor.md). The roles primer is in [syntax-roles.md](syntax-roles.md) and the theming reference is in [theming.md](theming.md).

## Contents

- [At-a-glance](#at-a-glance)
- [Engine](#engine)
  - [`SyntaxTokenizer`](#syntaxtokenizer)
  - [`LanguageTokenizer`](#languagetokenizer)
  - [`TokenizeRequest`](#tokenizerequest)
  - [`LanguageId`](#languageid)
  - [`LanguageExtension`](#languageextension)
  - [`SyntaxRole`](#syntaxrole)
  - [`rolePathValuesFromRoot`](#rolepathvaluesfromroot)
  - [`SyntaxTokenSpan`](#syntaxtokenspan)
- [Compose](#compose)
  - [`SyntaxStyledSpan`](#syntaxstyledspan)
  - [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans)
  - [`TextFieldBuffer.applySyntaxStyledSpans`](#textfieldbufferapplysyntaxstyledspans)
  - [`buildSyntaxAnnotatedString`](#buildsyntaxannotatedstring)
  - [`rememberSyntaxAnnotatedString`](#remembersyntaxannotatedstring)
- [Theme](#theme)
  - [`SyntaxStyle`](#syntaxstyle)
  - [`SyntaxRoleStyles`](#syntaxrolestyles)
  - [`SyntaxTheme`](#syntaxtheme)
- [Shared contracts](#shared-contracts)

---

## At-a-glance

| Layer | What's in it |
| --- | --- |
| Engine | Pure tokenization. Construct an engine, hand it `(code, languageLabel)`, get back token spans. Zero Compose dependency. |
| Compose | Glue between engine output and Compose text APIs. Memoization-aware helpers for the read-only `BasicText` path, span helpers for the editable `BasicTextField` path. |
| Theme | The theming primitive (`SyntaxStyle`), the role-styles map (`SyntaxRoleStyles`), and the theme type (`SyntaxTheme`) that turns `(role, languageId)` into a Compose `SpanStyle`. |

A typical render pipeline crosses all three layers:

1. Construct a `SyntaxTokenizer`.
2. Call `engine.tokenize(code = code, languageLabel = languageLabel)` to get back `List<SyntaxTokenSpan>`.
3. Resolve each span's `SpanStyle` through a `SyntaxTheme`.
4. Apply to a `BasicText` or `BasicTextField` via the Compose helpers.

`rememberSyntaxAnnotatedString` collapses steps 2 and 3 into one Composable call with the correct `remember` keys, leaving the host to wrap it in any `BasicText`-shaped composable.

---

## Engine

### `SyntaxTokenizer`

Source: [SyntaxTokenizer.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/tokenizer/SyntaxTokenizer.kt)

```kotlin
public class SyntaxTokenizer(
    builtInLanguages: Set<LanguageId> = LanguageId.BuiltIns,
    extensions: List<LanguageExtension> = emptyList(),
) {
    public val languageIds: Set<LanguageId>
    public val languageLabels: Set<String>
}
```

**Purpose.** The tokenization entry point. Wraps the built-in routing table and any host-supplied extensions and turns `(code, languageLabel)` into a normalized list of token spans.

**Description.** A `SyntaxTokenizer` is the only stateful object SyntaxMP constructs at runtime. The state is the enabled built-in language set, the host-supplied extensions, and the precomputed label map for this tokenizer instance. Once constructed it's immutable and safe to share across compositions and threads. Construct one per app or per editor surface; there is no benefit to building a fresh engine per call site.

The engine is also the routing authority for embedded languages. When a tokenizer encounters embedded code (HTML script/style, Markdown fenced blocks, JSX/TSX script/style blocks, etc.) and calls `TokenizeRequest.tokenizeEmbedded(...)`, the request goes back through this engine, applying the same active label catalog. Recursive embedded-language tokenization is capped at depth 3 to prevent pathological loops.

**Constructor parameters**

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `builtInLanguages` | `Set<LanguageId>` | `LanguageId.BuiltIns` | Subset of built-in languages enabled for this engine. This controls each built-in registration: its id, tokenizer implementation, and built-in aliases. Must be a subset of `LanguageId.BuiltIns`; register custom languages through `LanguageExtension`. |
| `extensions` | `List<LanguageExtension>` | `emptyList()` | Host-supplied tokenizers. Extensions are checked in order before built-ins, so an extension covering `LanguageId.Kotlin` overrides the built-in Kotlin tokenizer. |

**Properties**

| Name | Type | Description |
| --- | --- | --- |
| `languageIds` | `Set<LanguageId>` | Language ids this tokenizer instance can tokenize: enabled built-ins plus extension language ids. If an extension overrides an enabled built-in id, that id appears once. |
| `languageLabels` | `Set<String>` | Normalized labels this tokenizer instance recognizes. Includes enabled built-in ids and aliases, extension language id values, and explicit extension aliases. Disabled built-in aliases are absent unless an extension explicitly claims the label. |

**Methods**

**`resolveLanguageId(languageLabel)`**

```kotlin
public fun resolveLanguageId(languageLabel: String?): LanguageId?
```

Resolves a raw nullable label through this engine's active language catalog. Extension language id values and aliases participate, as do enabled built-in ids and aliases. Returns `null` if `languageLabel` is `null`, blank, unknown, or disabled for this tokenizer instance.

Call this only when you need the canonical `LanguageId` before tokenizing. For tokenization itself, pass the raw label directly to `tokenize(...)`.

**`tokenize(code, languageLabel)`**

```kotlin
public fun tokenize(code: String, languageLabel: String?): List<SyntaxTokenSpan>
```

Tokenizes `code` as the raw `languageLabel`. Resolves the label first, then returns the normalized list of token spans: sorted by start offset, with overlaps resolved and out-of-range spans clipped (see [span normalization](#span-normalization)).

Returns `emptyList()` and never throws when:

- `languageLabel` is `null`, empty, or blank.
- `code.isEmpty()`.
- the label is unknown or disabled for this tokenizer instance.
- The selected tokenizer throws. The throwable is caught and replaced with empty spans.

**Notes**

- The engine itself caches nothing across `tokenize` calls. Identical `(code, languageLabel)` pairs produce identical output but re-run the tokenizer each time. Hosts that need caching should `remember` on the Compose side or build a small LRU. See [building-an-editor.md](building-an-editor.md).

**See also**: [`LanguageTokenizer`](#languagetokenizer), [`LanguageExtension`](#languageextension).

---

### `LanguageTokenizer`

Source: [LanguageTokenizer.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/tokenizer/LanguageTokenizer.kt)

```kotlin
public fun interface LanguageTokenizer {
    public fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan>
}
```

**Purpose.** The contract every tokenizer (built-in and extension) implements.

**Description.** A `LanguageTokenizer` is a pure function from [`TokenizeRequest`](#tokenizerequest) to `List<SyntaxTokenSpan>`. Implementations should be stateless; the engine may invoke the same instance concurrently across multiple call sites. They may emit overlapping or out-of-order spans; span normalization in the engine sorts and resolves overlaps before the result reaches the caller.

The interface is declared as a `fun interface` so a tokenizer can be supplied as a lambda when convenient (`LanguageTokenizer { request -> ... }`), or as an `object` for shared state.

**Notes**

- Token offsets are UTF-16 code units (Kotlin `String` indices), not byte offsets and not code points. This matters for emoji-heavy or other non-BMP content; index against the same `String` you received.
- Throwing is allowed but produces an empty result for that call. Don't rely on it for signaling; prefer empty spans for "I don't know what this is." Implementations that want to observe or log their own failures should wrap their tokenizer body in `try/catch`.
- Each emitted span must populate `languageId`, usually by passing through `request.languageId`.

**See also**: [`TokenizeRequest`](#tokenizerequest), [`LanguageExtension`](#languageextension), and [language-extension.md](language-extension.md) for a worked custom tokenizer.

---

### `TokenizeRequest`

Source: [TokenizeRequest.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/tokenizer/TokenizeRequest.kt)

```kotlin
public class TokenizeRequest(
    val code: String,
    val languageId: LanguageId,
) {
    public fun tokenizeEmbedded(
        code: String,
        languageLabel: String,
    ): List<SyntaxTokenSpan>
}
```

**Purpose.** The input every tokenizer receives.

**Description.** The engine constructs a request per `tokenize` call. The `languageId` field is always the resolved canonical language id, not the raw label the caller passed in. Tokenizers can rely on it being one of the languages they claim to handle.

The public constructor takes `code` and `languageId` only. Requests constructed this way are useful for direct unit tests of a tokenizer, but they are not bound to a `SyntaxTokenizer`; `tokenizeEmbedded(...)` returns an empty list on manually constructed requests. Engine-created requests carry the internal routing function needed for embedded-language tokenization.

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `code` | `String` | Original code to tokenize. Tokenizers index offsets against this string. |
| `languageId` | `LanguageId` | Resolved language id. Non-null because the engine returns empty spans before constructing a request for an unresolvable language. |

**Methods**

**`tokenizeEmbedded(code, languageLabel)`**

```kotlin
public fun tokenizeEmbedded(
    code: String,
    languageLabel: String,
): List<SyntaxTokenSpan>
```

Routes a child code region through the same engine lookup as a top-level call. Returned spans are relative to the child `code`; tokenizers that merge them into a host result must offset them into the parent code range. Unknown, disabled, blank, or too-deep labels return an empty list. Returns an empty list on manually constructed requests.

### `LanguageId`

Source: [LanguageId.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/language/LanguageId.kt)

```kotlin
@JvmInline
public value class LanguageId private constructor(public val value: String)
```

**Purpose.** Canonical language identifier. Used as the routing key throughout the engine and as a theme-override key in `SyntaxTheme.languageOverrides`.

**Description.** A value class wrapping a normalized string id. Construction is deliberately private; callers go through two doors:

1. **Built-in constants** on the companion (`LanguageId.Kotlin`, etc.) when the language is known at compile time.
2. **`LanguageId.fromString(value)`** for exact custom language ids used by an extension. Does *not* honor aliases.

For runtime raw labels (file extensions, Markdown fence info strings, user-config strings), use [`SyntaxTokenizer.resolveLanguageId`](#syntaxtokenizer) when you need the canonical `LanguageId` before tokenizing. It can see the labels active for that tokenizer instance, including extension aliases and enabled built-in aliases.

The constructor normalizes by trimming whitespace and lowercasing. Equality is by the normalized `value` string, so `LanguageId.fromString("PYTHON")` equals `LanguageId.Python` after normalization.

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `value` | `String` | Normalized language id (trimmed, lowercased). Use it for exact identity comparisons and language override keys. For persisted user or file choices, store the original raw label and resolve it again later. |

**Built-in constants.** 39 languages live on `LanguageId.Companion` as `public val Name: LanguageId` entries (e.g. `LanguageId.Kotlin`). For the full list, see [languages.md](languages.md) or [LanguageId.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/language/LanguageId.kt).

**Companion members**

**`BuiltIns`**

```kotlin
public val BuiltIns: Set<LanguageId>
```

The full set of built-in languages, in registration order. Used as the default for `SyntaxTokenizer.builtInLanguages`.

**`fromString(value)`**

```kotlin
public fun fromString(value: String): LanguageId
```

Constructs an exact language id, bypassing alias resolution. Throws `IllegalArgumentException` if `value` is blank after normalization. Used when a host knows exactly which custom id its extension uses (typical pattern: `val Myql = LanguageId.fromString("myql")`).

**Notes**

- `LanguageId` is the type used for theme `languageOverrides` keys and for the `languageId` field on `SyntaxTokenSpan`. Hosts can `when` on `span.languageId` for per-language post-processing.
- Built-in aliases include `js`, `ts`, `jsx`, `tsx`, `kt`, `kts`, `md`, `yml`, `pgsql`, `sqlite3`, `env`, `sh`, `bash`, `zsh`, and others. They are resolved by `SyntaxTokenizer` only when the corresponding built-in language registration is enabled; see `BuiltInAliases.kt` for the authoritative list.

**See also**: [`LanguageExtension`](#languageextension), [`SyntaxTokenSpan`](#syntaxtokenspan), [`SyntaxTheme`](#syntaxtheme).

---

### `LanguageExtension`

Source: [LanguageExtension.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/language/LanguageExtension.kt)

```kotlin
public data class LanguageExtension(
    val languageId: LanguageId,
    val aliases: Set<String> = emptySet(),
    val tokenizer: LanguageTokenizer,
)
```

**Purpose.** The bundle a host registers with the engine to add or override a language tokenizer.

**Description.** An extension binds a `LanguageTokenizer` to one `LanguageId` value. The engine resolves extensions before built-ins, so an extension whose `languageId` is `LanguageId.Kotlin` overrides the built-in Kotlin tokenizer. Multiple extensions may be registered; resolution is first-match in the order they were passed to the engine.

The extension language id value itself is always an active label for the tokenizer instance. The optional `aliases` set adds host-defined labels that also resolve to this extension's `languageId` at the engine boundary and when discovered inside source text. Typically Markdown fence labels (e.g. `mql` -> `myql`) or markup raw-text `lang=` values.

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `languageId` | `LanguageId` | The language this extension handles. Used as the routing key. |
| `aliases` | `Set<String>` | Labels that resolve to `languageId` when discovered inside source text (e.g. Markdown fence info, `<script lang="...">`, `<style lang="...">`). Defaults to empty. |
| `tokenizer` | `LanguageTokenizer` | The tokenizer that handles `languageId`. |

**Notes**

- One language per extension. If one tokenizer handles multiple languages, register multiple `LanguageExtension` entries that share the same tokenizer.
- `aliases` entries are trimmed and lowercased when checked at runtime, so case in the set doesn't matter. Blank aliases are ignored.
- An extension can override a built-in language by declaring the same id. If the built-in registration remains enabled, the extension tokenizer also receives the enabled built-in labels. If the built-in registration is disabled, only the extension language id value and explicit extension aliases are active.
- Extension aliases can shadow built-in aliases for this tokenizer instance without changing the built-in language's canonical id label.

**See also**: [`LanguageTokenizer`](#languagetokenizer), [`SyntaxTokenizer`](#syntaxtokenizer), and [language-extension.md](language-extension.md).

---

### `SyntaxRole`

Source: [SyntaxRole.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/role/SyntaxRole.kt)

```kotlin
public sealed class SyntaxRole protected constructor(public val value: String) {
    public fun append(suffix: String): SyntaxRole
    public fun rolesFromRoot(): List<SyntaxRole>
    public companion object {
        public fun of(value: String): SyntaxRole
    }
}
```

**Purpose.** Identifier for what kind of thing a token is, themed by `SyntaxTheme`.

**Description.** A role is a dotted path string (`keyword`, `keyword.declaration`, `function.builtin`, `constant.builtin.true`, etc.). The dotted structure gives themes a parent-fallback story: if a theme styles `keyword` but not `keyword.declaration`, a declaration keyword still picks up the `keyword` style.

Construction is restricted. Callers go through:

1. **Nested constant objects** (`SyntaxRole.Keyword`, `SyntaxRole.Function.Builtin`, etc.) when the role is known at compile time.
2. **`SyntaxRole.of(value)`** for arbitrary paths supplied as strings.
3. **`role.append(suffix)`** to extend an existing role with a dotted segment.

The constructor is `protected` so external code can't subclass; the sealed hierarchy keeps `equals`/`hashCode` value-based on the underlying string.

**Built-in role tree**

| Root role | Child roles |
| --- | --- |
| `Keyword` (`"keyword"`) | `Control`, `Declaration`, `Modifier`, `AtRule` |
| `String` (`"string"`) | `Regex`, `Language`, `Url` |
| `Number` (`"number"`) | *(leaf)* |
| `Comment` (`"comment"`) | *(leaf)* |
| `Function` (`"function"`) | `Builtin`, `Declaration`, `Member`, `Macro` |
| `Type` (`"type"`) | *(leaf)* |
| `Property` (`"property"`) | `Name`, `Quoted`, `Section` |
| `Variable` (`"variable"`) | `Parameter`, `Namespace` |
| `Operator` (`"operator"`) | *(leaf)* |
| `Punctuation` (`"punctuation"`) | `Expression` |
| `Annotation` (`"annotation"`) | *(leaf)* |
| `Tag` (`"tag"`) | *(leaf)* |
| `Attribute` (`"attribute"`) | `Directive`, `Pseudo` |
| `Constant` (`"constant"`) | `Builtin`, `Color`, `Atom` |
| `Escape` (`"escape"`) | *(leaf)* |
| `Markup` (`"markup"`) | `Expression`, `Cdata`, `Frontmatter` |

Child roles are accessed as nested members of the root object: `SyntaxRole.Keyword.Declaration`, `SyntaxRole.Variable.Parameter`, etc. The roles primer lives in [syntax-roles.md](syntax-roles.md); the full per-language catalog of role strings each built-in tokenizer emits lives in [languages.md](languages.md).

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `value` | `String` | The dotted path as a string (e.g. `"keyword.declaration"`). Also exposed via `span.role.value`. |

**Methods**

**`append(suffix)`**

```kotlin
public fun append(suffix: String): SyntaxRole
```

Returns a new role whose `value` is `this.value + "." + normalize(suffix)`. Used to build language-specific child roles on top of a known parent. The suffix is normalized by trimming and stripping outer dots; whitespace inside path segments is rejected.

**`rolesFromRoot()`**

```kotlin
public fun rolesFromRoot(): List<SyntaxRole>
```

Returns the chain of roles from the root through this exact role, root-first. For `SyntaxRole.of("constant.builtin.true")` returns `[constant, constant.builtin, constant.builtin.true]`. Used by `SyntaxTheme` for the parent-walk merge during style resolution.

**Companion members**

**`of(value)`**

```kotlin
public fun of(value: String): SyntaxRole
```

Validated factory for arbitrary role strings. Trims and strips outer dots. Throws `IllegalArgumentException` if the result is blank, contains empty path segments (e.g. `"a..b"`), or contains whitespace inside any segment. Returns one of the known nested constants when the normalized value matches, otherwise a `Custom` instance.

**Notes**

- Custom-role strings can use any dotted path; themes can style them via `SyntaxTheme.withRoleStyle(SyntaxRole.of("myql.builtin"), ...)`. Choose names that fit underneath an existing root role so themes that style the root pick them up automatically (e.g. `keyword.myql` rather than `myql.keyword`).
- The constants on the nested objects are themselves `SyntaxRole` instances, so `SyntaxRole.Keyword == SyntaxRole.of("keyword")` is `true`.
- `equals`/`hashCode`/`toString` are all final and value-based on `value`. Two roles with the same string are the same role regardless of which path created them.

**See also**: [`rolePathValuesFromRoot`](#rolepathvaluesfromroot), [`SyntaxTheme`](#syntaxtheme), [`SyntaxTokenSpan`](#syntaxtokenspan), [syntax-roles.md](syntax-roles.md), [languages.md](languages.md), [theming.md](theming.md).

---

### `rolePathValuesFromRoot`

Source: [SyntaxRole.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/role/SyntaxRole.kt)

```kotlin
public fun rolePathValuesFromRoot(value: String): List<String>
```

**Purpose.** Returns the root-to-exact dotted role path values for a role string.

**Description.** For `"constant.builtin.true"`, returns `["constant", "constant.builtin", "constant.builtin.true"]`. This is the string-level helper behind `SyntaxRole.rolesFromRoot()` and the theme parent-walk merge.

**Parameters**

| Name | Type | Description |
| --- | --- | --- |
| `value` | `String` | Dotted role path value to expand. |

**Returns** `List<String>`: parent role values ordered from root to exact.

**See also**: [`SyntaxRole.rolesFromRoot`](#syntaxrole), [`SyntaxTheme`](#syntaxtheme).

---

### `SyntaxTokenSpan`

Source: [SyntaxTokenSpan.kt](../syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/spans/SyntaxTokenSpan.kt)

```kotlin
public data class SyntaxTokenSpan(
    val start: Int,
    val endExclusive: Int,
    val role: SyntaxRole,
    val languageId: LanguageId,
)
```

**Purpose.** One emitted token. The unit of the engine's output.

**Description.** A token span carries its position in the original code, its role, and the language id that produced it. The `languageId` field is important for embedded content: when a Markdown tokenizer routes a fenced JavaScript block through the engine, the returned spans carry `languageId = LanguageId.JavaScript`, not `Markdown`. Theme language overrides key on this field, and host post-processing can `when` on it for language-specific work.

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `start` | `Int` | Inclusive UTF-16 start offset into the original code string. |
| `endExclusive` | `Int` | Exclusive UTF-16 end offset. Always `> start` after normalization. |
| `role` | `SyntaxRole` | The role to style. |
| `languageId` | `LanguageId` | The language id that produced this span. Equals `request.languageId` for top-level spans, or the embedded language for embedded spans. |

**Notes**

- Offsets are UTF-16 code units. Slice the original code with `code.substring(span.start, span.endExclusive)`.
- After engine normalization the span list is sorted by `start`, has no overlaps, and is clipped to `[0, code.length]`. Tokenizers can emit out-of-order, overlapping, or out-of-range spans; the engine sorts them out.

**See also**: [`SyntaxRole`](#syntaxrole), [`LanguageId`](#languageid), [span normalization](#span-normalization).

---

## Compose

Glue between engine output and Compose text APIs. Converts engine spans into the shapes Compose expects: `AnnotatedString` for read-only text, `TextFieldBuffer` styling for editable text.

### `SyntaxStyledSpan`

Source: [SyntaxStyledSpan.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxStyledSpan.kt)

```kotlin
public data class SyntaxStyledSpan(
    val start: Int,
    val endExclusive: Int,
    val style: SpanStyle,
)
```

**Purpose.** A token span with its theme-resolved `SpanStyle`, ready to apply to a Compose text surface.

**Description.** The output shape of [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans). Each entry already has its `SpanStyle` resolved against the theme, so the caller doesn't need to hold the theme any longer; apply the spans, done.

**Fields**

| Field | Type | Description |
| --- | --- | --- |
| `start` | `Int` | Inclusive offset into the target text. |
| `endExclusive` | `Int` | Exclusive end offset. |
| `style` | `SpanStyle` | The Compose span style to apply. |

**Notes**

- Offsets are UTF-16 code units.
- See [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans) for how spans are split at line breaks.

---

### `buildSyntaxStyledSpans`

Source: [SyntaxStyledSpan.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxStyledSpan.kt)

```kotlin
public fun buildSyntaxStyledSpans(
    code: String,
    spans: List<SyntaxTokenSpan>,
    theme: SyntaxTheme,
): List<SyntaxStyledSpan>
```

**Purpose.** Convert engine token spans into Compose text spans for editable buffers.

**Description.** Walks the input spans, resolves each one's `SpanStyle` via `theme.resolveSpanStyle(span)`, and splits the result at every `\n` and `\r\n` so no returned span crosses a line break. The split is required for `BasicTextField`: Compose's `TextFieldBuffer.addStyle` behaves badly when a styled range straddles a newline.

Spans whose start/end are out of range relative to `code.length` are clipped; empty or collapsed spans are dropped.

**Parameters**

| Name | Type | Description |
| --- | --- | --- |
| `code` | `String` | Original code the spans index into. Used for line-break detection during splitting. |
| `spans` | `List<SyntaxTokenSpan>` | Engine output to convert. Typically from `engine.tokenize(...)`. |
| `theme` | `SyntaxTheme` | Theme used to resolve each span's `SpanStyle`. |

**Returns** `List<SyntaxStyledSpan>`: line-split styled spans ready for an editable buffer.

**See also**: [`applySyntaxStyledSpans`](#textfieldbufferapplysyntaxstyledspans), [`buildSyntaxAnnotatedString`](#buildsyntaxannotatedstring) (for the non-splitting read-only counterpart).

---

### `TextFieldBuffer.applySyntaxStyledSpans`

Source: [SyntaxStyledSpan.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxStyledSpan.kt)

```kotlin
public fun TextFieldBuffer.applySyntaxStyledSpans(
    spans: List<SyntaxStyledSpan>,
)
```

**Purpose.** Apply styled spans to a `BasicTextField`'s buffer from inside an `outputTransformation` block.

**Description.** For each span, clips it to the buffer's current length and then calls `addStyle(span.style, span.start, span.endExclusive)` on the receiver buffer. The clipping handles the race between a keystroke and the tokenization snapshot the spans were produced from (the buffer may be a character shorter than the code the spans index into). Spans that fall outside the buffer's current length are silently dropped.

This is the canonical way to apply syntax styling to editable text. The full pattern is:

```kotlin
BasicTextField(
    state = state,
    outputTransformation = {
        val code = asCharSequence().toString()
        val tokens = engine.tokenize(code = code, languageLabel = languageLabel)
        val spans = buildSyntaxStyledSpans(code = code, spans = tokens, theme = theme)
        applySyntaxStyledSpans(spans)
    },
    // ...
)
```

**Parameters**

| Name | Type | Description |
| --- | --- | --- |
| `spans` | `List<SyntaxStyledSpan>` | Spans to apply. Typically the output of [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans). |

**Notes**

- Must be called inside an `outputTransformation` block (where the receiver is a `TextFieldBuffer`). Calling it outside that context doesn't compile.
- Don't apply unsplit spans from [`buildSyntaxAnnotatedString`](#buildsyntaxannotatedstring) here. Compose's buffer styling misbehaves when a styled range crosses a `\n`. Use `buildSyntaxStyledSpans` for the editable path.

**See also**: [building-an-editor.md](building-an-editor.md) for the full worked editable example.

---

### `buildSyntaxAnnotatedString`

Source: [SyntaxAnnotatedString.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxAnnotatedString.kt)

```kotlin
public fun buildSyntaxAnnotatedString(
    code: String,
    spans: List<SyntaxTokenSpan>,
    theme: SyntaxTheme,
): AnnotatedString
```

**Purpose.** Convert engine token spans into an `AnnotatedString` for read-only text.

**Description.** Walks the input spans, resolves each one's `SpanStyle` via `theme.resolveSpanStyle(span)`, and adds the style to an `AnnotatedString.Builder` over the same range. Unlike [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans), this function does **not** split spans at line breaks: a multi-line block comment or string is a single styled range covering all the lines it spans. That's what `AnnotatedString` consumers expect.

This is the non-Composable building block. From inside a Composable, prefer [`rememberSyntaxAnnotatedString`](#remembersyntaxannotatedstring), which wraps both tokenization and this builder in `remember` blocks with the correct keys.

**Parameters**

| Name | Type | Description |
| --- | --- | --- |
| `code` | `String` | The original text. Becomes the body of the returned `AnnotatedString`. |
| `spans` | `List<SyntaxTokenSpan>` | Engine output to apply. |
| `theme` | `SyntaxTheme` | Theme used to resolve each span's `SpanStyle`. |

**Returns** `AnnotatedString`: `code` annotated with theme-resolved span styles. Unsplit; multi-line spans stay as single ranges.

**Notes**

- For editable surfaces use [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans) + [`applySyntaxStyledSpans`](#textfieldbufferapplysyntaxstyledspans).
- If you call this from a Composable directly (instead of through `rememberSyntaxAnnotatedString`), pair it with `remember(code, spans, theme) { buildSyntaxAnnotatedString(...) }`. Missing `theme` from the key list is a silent bug: the cached string keeps the old theme's colors when the host swaps themes at runtime.

**See also**: [`rememberSyntaxAnnotatedString`](#remembersyntaxannotatedstring), [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans).

---

### `rememberSyntaxAnnotatedString`

Source: [SyntaxAnnotatedString.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxAnnotatedString.kt)

```kotlin
@Composable
public fun rememberSyntaxAnnotatedString(
    code: String,
    languageLabel: String?,
    engine: SyntaxTokenizer,
    theme: SyntaxTheme,
): AnnotatedString
```

**Purpose.** Composable wrapper around tokenization + [`buildSyntaxAnnotatedString`](#buildsyntaxannotatedstring) with the correct `remember` keys for both stages.

**Description.** Two `remember` blocks chained together:

- Tokenized spans are remembered on `(engine, code, languageLabel)`; they're recomputed when any of those change, and reused otherwise.
- The resulting `AnnotatedString` is remembered on `(code, spans, theme)`, restyling when the host swaps themes at runtime and reusing when nothing relevant has changed.

Use this instead of writing the `remember` chain by hand. The keys are the kind of detail that's easy to get right on day one and easy to get subtly wrong six months later when a theme toggle is added.

If `languageLabel` is `null` or blank, returns a plain `AnnotatedString(code)` without invoking `engine`. This matches the "no language picked yet" case (e.g. a language-picker UI before the user has chosen).

**Parameters**

| Name | Type | Description |
| --- | --- | --- |
| `code` | `String` | Text to tokenize and annotate. |
| `languageLabel` | `String?` | Raw label resolved by `engine`. Active built-in labels and extension labels work. `null`, blank, unknown, or disabled labels skip tokenization. |
| `engine` | `SyntaxTokenizer` | Engine used for tokenization. Construct/hoist this however suits your scope (`remember`, host `CompositionLocal`, DI). |
| `theme` | `SyntaxTheme` | Theme used to resolve role styles. Live changes restyle the cached spans without retokenizing. |

**Returns** `AnnotatedString` ready to drop into a `BasicText` or any consumer of `AnnotatedString`.

**Notes**

- Engine scope is yours: the function takes the engine as a parameter and doesn't construct one for you. See [building-an-editor.md](building-an-editor.md) for host-owned engine-sharing patterns.
- For editable surfaces, this isn't what you want: `BasicTextField` consumes spans through a `TextFieldBuffer` rather than an `AnnotatedString`. Use [`buildSyntaxStyledSpans`](#buildsyntaxstyledspans) + [`applySyntaxStyledSpans`](#textfieldbufferapplysyntaxstyledspans) instead.

**See also**: [`buildSyntaxAnnotatedString`](#buildsyntaxannotatedstring), [`SyntaxTokenizer`](#syntaxtokenizer).

---

## Theme

The theming model. The theme surface is deliberately narrow: foreground color + optional weight + optional style per role, with parent-fallback resolution. The host's Compose `TextStyle` still owns font family, size, line height, base color, and backgrounds.

### `SyntaxStyle`

Source: [SyntaxStyle.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxStyle.kt)

```kotlin
public data class SyntaxStyle(
    val color: Color = Color.Unspecified,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
) {
    public fun toSpanStyle(): SpanStyle
}
```

**Purpose.** A single role's foreground styling contribution.

**Description.** Three optional fields. Unspecified fields don't override values from the host text style or earlier cascade layers, so a `SyntaxStyle` that only sets `color` leaves font weight and style alone, and a default-constructed `SyntaxStyle()` contributes nothing; useful as a deliberate "no change" entry.

The narrow shape is intentional. Font family, size, line height, base color, and backgrounds belong with the host's design system, not with a syntax theme. Consumers who need full `SpanStyle` control should bypass the theme entirely and resolve styles themselves from raw `SyntaxTokenSpan`s.

**Fields**

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Unspecified` | Foreground color. `Color.Unspecified` means "don't override." |
| `fontWeight` | `FontWeight?` | `null` | Optional weight. `null` means "don't override." |
| `fontStyle` | `FontStyle?` | `null` | Optional style (italic/normal). `null` means "don't override." |

**Methods**

**`toSpanStyle()`**

```kotlin
public fun toSpanStyle(): SpanStyle
```

Converts this `SyntaxStyle` into a Compose `SpanStyle` with the same three fields set. Used internally by `SyntaxTheme` after style resolution.

**See also**: [`SyntaxTheme`](#syntaxtheme), [`SyntaxRoleStyles`](#syntaxrolestyles).

---

### `SyntaxRoleStyles`

Source: [SyntaxRoleStyles.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxRoleStyles.kt)

```kotlin
public typealias SyntaxRoleStyles = Map<SyntaxRole, SyntaxStyle>

public fun SyntaxRoleStyles(): SyntaxRoleStyles
public fun SyntaxRoleStyles(vararg styles: Pair<SyntaxRole, SyntaxStyle>): SyntaxRoleStyles
public fun SyntaxRoleStyles.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxRoleStyles
```

**Purpose.** Sparse map from roles to their style contributions. The building block of `SyntaxTheme.roleStyles` and per-language overrides.

**Description.** A plain `Map<SyntaxRole, SyntaxStyle>` under the typealias. Sparse by design; only include roles you actually want to style. Roles not in the map contribute nothing during resolution.

**Factory and helper functions**

**`SyntaxRoleStyles()`**

```kotlin
public fun SyntaxRoleStyles(): SyntaxRoleStyles
```

Returns an empty map. Equivalent to `emptyMap<SyntaxRole, SyntaxStyle>()` but reads better at call sites.

**`SyntaxRoleStyles(vararg styles)`**

```kotlin
public fun SyntaxRoleStyles(vararg styles: Pair<SyntaxRole, SyntaxStyle>): SyntaxRoleStyles
```

Constructs a map from `role to style` pairs:

```kotlin
SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF9A3412)),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF15803D)),
)
```

**`SyntaxRoleStyles.withRoleStyle(role, style)`**

```kotlin
public fun SyntaxRoleStyles.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxRoleStyles
```

Returns a copy of the receiver with `style` set for `role`. Equivalent to `this + (role to style)` but reads better in a chain.

**Notes**

- The typealias means a `Map<SyntaxRole, SyntaxStyle>` from anywhere (`mapOf(...)`, the Kotlin stdlib, an explicit `LinkedHashMap`) is already a `SyntaxRoleStyles`.
- There is no `baseStyle` concept. If you want a fallback color for everything, either style each root role (`Keyword`, `String`, `Number`, ...) explicitly or set the color in the host's Compose `TextStyle`.

**See also**: [`SyntaxStyle`](#syntaxstyle), [`SyntaxTheme`](#syntaxtheme).

---

### `SyntaxTheme`

Source: [SyntaxTheme.kt](../syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/compose/SyntaxTheme.kt)

```kotlin
public data class SyntaxTheme(
    val roleStyles: SyntaxRoleStyles = SyntaxRoleStyles(),
    val languageOverrides: Map<LanguageId, SyntaxRoleStyles> = emptyMap(),
) {
    public fun resolveSpanStyle(span: SyntaxTokenSpan): SpanStyle
    public fun resolveStyle(role: SyntaxRole): SyntaxStyle
    public fun resolveStyle(role: SyntaxRole, languageId: LanguageId): SyntaxStyle

    public companion object {
        public val DefaultLight: SyntaxTheme
        public val DefaultDark: SyntaxTheme
    }
}

public fun SyntaxTheme.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxTheme
public fun SyntaxTheme.withLanguageRoleStyle(languageId: LanguageId, role: SyntaxRole, style: SyntaxStyle): SyntaxTheme
public fun SyntaxTheme.withLanguageRoleStyles(languageId: LanguageId, styles: SyntaxRoleStyles?): SyntaxTheme
```

**Purpose.** The mapping from `(role, languageId)` to a Compose `SpanStyle`. The top-level theme value passed to Compose call sites.

**Description.** Holds two layers:

1. **Global `roleStyles`**: applies to every span regardless of language.
2. **Per-language `languageOverrides`**: sparse per-language maps that contribute on top of (and override) the global layer for spans whose `languageId` matches.

Resolution is an additive merge (not first-match): for a span with role `keyword.declaration` and `languageId = Kotlin`, the theme walks the role's parent chain root-to-exact (`keyword`, then `keyword.declaration`) against `roleStyles`, then walks the same chain against `languageOverrides[Kotlin]`, merging contributions as it goes. Later contributions win on individual fields (`color`, `fontWeight`, `fontStyle`). Unset roles contribute nothing.

The result is that themes can paint broad strokes globally (color every keyword red) and add language-specific refinements (make Kotlin declaration keywords bold) without duplicating styles. See [theming.md](theming.md) for worked examples and the full resolution policy walk-through.

**Fields**

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `roleStyles` | `SyntaxRoleStyles` | `SyntaxRoleStyles()` (empty) | Global role-to-style map. Applied to every span. |
| `languageOverrides` | `Map<LanguageId, SyntaxRoleStyles>` | `emptyMap()` | Per-language refinements. Each entry's value is layered on top of `roleStyles` for spans matching that language. |

**Methods**

**`resolveSpanStyle(span)`**

```kotlin
public fun resolveSpanStyle(span: SyntaxTokenSpan): SpanStyle
```

The hot path. Resolves the final Compose `SpanStyle` for a token span by running the parent-walk merge across both layers and calling `toSpanStyle()`. Used internally by `buildSyntaxAnnotatedString` and `buildSyntaxStyledSpans`. Hosts that bypass the helpers can call this directly.

**`resolveStyle(role)`**

```kotlin
public fun resolveStyle(role: SyntaxRole): SyntaxStyle
```

Returns the merged `SyntaxStyle` for a role, resolved against `roleStyles` only. Useful for tests, debug tooling, or for previewing what a theme will do without going through a real token span.

**`resolveStyle(role, languageId)`**

```kotlin
public fun resolveStyle(role: SyntaxRole, languageId: LanguageId): SyntaxStyle
```

Same as above, but also layers `languageOverrides[languageId]` on top of the global resolution.

**Companion members**

**`DefaultLight`** / **`DefaultDark`**

```kotlin
public val DefaultLight: SyntaxTheme
public val DefaultDark: SyntaxTheme
```

Foreground-only starter themes tuned for light and dark surfaces respectively. Each populates 16 root roles (`Keyword`, `String`, `Number`, `Comment`, `Function`, `Type`, `Property`, `Variable`, `Operator`, `Punctuation`, `Annotation`, `Tag`, `Attribute`, `Constant`, `Escape`, `Markup`) with color-only entries, no weight or style. Intended as a starting point hosts customize via the copy/override helpers.

**Copy/override helpers**

Four extension functions for non-destructive edits. They all return new themes; none mutate.

**`SyntaxTheme.withRoleStyle(role, style)`**

```kotlin
public fun SyntaxTheme.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxTheme
```

Replace (or add) one entry in the global `roleStyles`. Preserves `languageOverrides` and all other global roles.

**`SyntaxTheme.withLanguageRoleStyle(languageId, role, style)`**

```kotlin
public fun SyntaxTheme.withLanguageRoleStyle(
    languageId: LanguageId,
    role: SyntaxRole,
    style: SyntaxStyle,
): SyntaxTheme
```

Replace (or add) one entry inside `languageOverrides[languageId]`. Preserves all other languages and all global roles. Creates the language's override map if absent.

**`SyntaxTheme.withLanguageRoleStyles(languageId, styles)`**

```kotlin
public fun SyntaxTheme.withLanguageRoleStyles(
    languageId: LanguageId,
    styles: SyntaxRoleStyles?,
): SyntaxTheme
```

Replace `languageOverrides[languageId]` wholesale, or remove it entirely by passing `null`. Preserves all other languages.

The fourth helper, `SyntaxRoleStyles.withRoleStyle`, lives on the role-styles map rather than the theme; see [`SyntaxRoleStyles`](#syntaxrolestyles).

**Notes**

- `SyntaxTheme` doesn't expose a `CompositionLocal`. Hosts that want subtree-wide theme threading define their own local. See [theming.md](theming.md) for the recipe.
- The resolution path is allocation-light: roles are walked once root-to-exact and the merged `SyntaxStyle` is the only intermediate object.
- `Color.Unspecified` in `SyntaxStyle.color` and `null` in `fontWeight` / `fontStyle` mean "no contribution at this layer." A child layer with `Color.Unspecified` does not erase a parent's color; it just doesn't override.

**See also**: [`SyntaxStyle`](#syntaxstyle), [`SyntaxRoleStyles`](#syntaxrolestyles), [`SyntaxRole`](#syntaxrole), [theming.md](theming.md).

---

## Shared contracts

These behaviors hold across multiple symbols. They're documented once here so the per-symbol entries above don't have to repeat them.

### Span normalization

All output from `SyntaxTokenizer.tokenize(...)` is normalized before being returned to the caller:

- Sorted by `start` ascending.
- Overlaps resolved deterministically: shortest span wins, then deepest dotted role, then earliest emission index.
- Out-of-range offsets clipped to `[0, code.length]`.
- Empty spans (`start == endExclusive` after clipping) dropped.

Tokenizer implementations therefore don't need to worry about emitting overlapping or out-of-order spans. Emit the natural shape and let the engine sort it out.

### UTF-16 offsets

Every `start` / `endExclusive` field on public types (`SyntaxTokenSpan`, `SyntaxStyledSpan`) is a UTF-16 code-unit offset into a Kotlin `String`. Slice with `code.substring(start, endExclusive)`. This matters for content with non-BMP characters (some emoji, historic scripts): one user-visible character can take two code units, and code-point counting will diverge from offset arithmetic.

### Exception handling

The engine is defensive on purpose:

- A `Throwable` from any tokenizer (built-in or extension) is caught and replaced with an empty span list for that call. Hosts don't see tokenizer exceptions from `engine.tokenize(...)`.
- An unknown / unregistered / disabled language label returns an empty span list. No exception.
- A `null` or blank language label returns an empty span list. No exception.
- An empty `code` string returns an empty span list without invoking any tokenizer.

The contract: `engine.tokenize(code = code, languageLabel = languageLabel)` always returns a list, never throws. The list may be empty. Plain-text rendering is always a valid fallback.

### Embedded-language recursion

When a tokenizer encounters embedded content (HTML script/style, Markdown fenced blocks, JSX/TSX script/style regions, or a custom extension's own child regions), it can route the inner content back through the engine with `TokenizeRequest.tokenizeEmbedded(...)`, which re-applies the active label catalog. Recursion is capped at depth 3. A document with deeper nesting renders the deepest layer as plain text rather than continuing to recurse.

### Role value stability and language labels

The `value` string on each built-in `SyntaxRole` is intended to be stable. Hosts may persist or transmit role paths for theme keys, route segments, and role-based API payloads. Renaming a built-in role path is a breaking change.

`LanguageId.value` is the resolved identity the engine attaches to spans and the key used for language-specific theme overrides. It is not the recommended persisted source of truth for user-authored language labels or file-derived language choices.

If a host needs to save a language choice, persist the original label the user or file supplied (`bash`, `dotenv`, `xml`, etc.) and pass that label back to `SyntaxTokenizer` at tokenization time. That keeps alias routing and future tokenizer routing changes in one place. Alias resolution is a routing policy, not a persistence contract; aliases are only alternate labels for the same public language identity.
