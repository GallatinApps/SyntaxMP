# SyntaxMP Theming

This is the reference for shaping SyntaxMP's visual output. It covers the role tree, the styling primitive, how to define a theme, the resolution policy, per-language overrides, host-owned theme threading, and the built-in starter themes.

The companion docs are [docs/syntax-roles.md](syntax-roles.md) (the roles primer: root and refinement constants, custom-role factories), [docs/languages.md](languages.md) (the per-language catalog of every role each built-in tokenizer emits), and the README's [Theming section](../README.md#theming) (the smallest possible usage example).

## What `SyntaxTheme` does and doesn't do

`SyntaxTheme` maps **roles** to **syntax styles**. A role is what a tokenizer says about a span (`keyword`, `string`, `variable.parameter`, `markup.diff.addition`); a syntax style is the foreground contribution applied to that span.

It deliberately does **not** own:

- font family
- font size or line height
- the surrounding text color used for plain text and for unstyled tokens
- backgrounds, surfaces, padding, or any visual chrome

Those belong to the host's Compose `TextStyle` and surrounding layout. A `SyntaxTheme` that doesn't style a particular role contributes nothing for that role, and the host's `TextStyle` remains the visible base.

## The role tree at a glance

`SyntaxRole` is a sealed class whose known roles live as nested constant objects on the class itself. There is no separate namespace.

```kotlin
SyntaxRole.Keyword                  // root role
SyntaxRole.Keyword.Control          // child refinement
SyntaxRole.Keyword.Declaration
SyntaxRole.String
SyntaxRole.String.Regex
SyntaxRole.Function
SyntaxRole.Function.Builtin
SyntaxRole.Variable
SyntaxRole.Variable.Parameter
SyntaxRole.Constant
SyntaxRole.Constant.Builtin
SyntaxRole.Markup
SyntaxRole.Markup.Expression
// …and roots for Number, Comment, Type, Property, Operator,
// Punctuation, Annotation, Tag, Attribute, Escape.
```

Each role carries a canonical dotted `value` string (`SyntaxRole.Keyword.Control.value` is `"keyword.control"`). Themes never compare against the strings directly; they key by `SyntaxRole` and the resolver handles the parent walk for you.

For exact or custom paths, construct via the factories rather than the (protected) constructor:

```kotlin
val custom = SyntaxRole.of("custom.myql.directive")
val refined = SyntaxRole.Keyword.append("sql")          // keyword.sql
val markupAddition = SyntaxRole.of("markup.diff.addition")
```

[docs/syntax-roles.md](syntax-roles.md) is the primer on what roles are and how the tree is shaped. The full per-language catalog of every role the built-in tokenizers emit lives in [docs/languages.md](languages.md).

## The styling primitive

`SyntaxStyle` is intentionally narrow:

```kotlin
data class SyntaxStyle(
    val color: Color = Color.Unspecified,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
)
```

A field at its default (unspecified `Color`, null `FontWeight`, null `FontStyle`) is a *no-op contribution*. `SyntaxStyle()` with no arguments contributes nothing. That's useful as a deliberate "leave this role alone" entry, e.g. when you want to declare that a refinement role should not be restyled on top of its parent.

`toSpanStyle()` converts to Compose's `SpanStyle` when the engine needs to apply spans; you rarely call it yourself.

## The role-styles map

`SyntaxRoleStyles` is a typealias for `Map<SyntaxRole, SyntaxStyle>`. Construct one with the varargs factory or a plain `mapOf`:

```kotlin
val styles: SyntaxRoleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9), fontWeight = FontWeight.SemiBold),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
    SyntaxRole.Number to SyntaxStyle(color = Color(0xFFAD3DA4)),
    SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF7A7F87), fontStyle = FontStyle.Italic),
)
```

Sparse maps are normal; set only the roles you care about. There is **no** base style: if you want a fallback color for everything that isn't otherwise styled, set the root roles (`SyntaxRole.Keyword`, `SyntaxRole.String`, `SyntaxRole.Number`, ...) explicitly, or let the host's Compose `TextStyle` color show through.

## Defining a theme

The primary construction path is a direct `SyntaxTheme(...)` call:

```kotlin
data class SyntaxTheme(
    val roleStyles: SyntaxRoleStyles = SyntaxRoleStyles(),
    val languageOverrides: Map<LanguageId, SyntaxRoleStyles> = emptyMap(),
)
```

A theme is a global role-styles map plus a sparse per-language override map. Use the global map for everything you want to apply everywhere; reach for a language override only when one language genuinely wants a different look.

```kotlin
val theme = SyntaxTheme(
    roleStyles = SyntaxRoleStyles(
        SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9), fontWeight = FontWeight.SemiBold),
        SyntaxRole.Type to SyntaxStyle(color = Color(0xFF6D28D9)),
        SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
        SyntaxRole.Number to SyntaxStyle(color = Color(0xFFAD3DA4)),
        SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF7A7F87), fontStyle = FontStyle.Italic),
        SyntaxRole.Function to SyntaxStyle(color = Color(0xFF1D4ED8)),
        SyntaxRole.Variable to SyntaxStyle(color = Color(0xFFBE185D)),
        SyntaxRole.Property to SyntaxStyle(color = Color(0xFFBE185D)),
        SyntaxRole.Operator to SyntaxStyle(color = Color(0xFF334155)),
        SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFF64748B)),
        SyntaxRole.Annotation to SyntaxStyle(color = Color(0xFFA16207)),
        SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF047857)),
        SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF0E7490)),
        SyntaxRole.Constant to SyntaxStyle(color = Color(0xFFB91C1C)),
        SyntaxRole.Escape to SyntaxStyle(color = Color(0xFF9A3412)),
        SyntaxRole.Markup to SyntaxStyle(color = Color(0xFF475569)),
    ),
    languageOverrides = mapOf(
        LanguageId.Kotlin to SyntaxRoleStyles(
            SyntaxRole.Keyword.Control to SyntaxStyle(fontWeight = FontWeight.Bold),
            SyntaxRole.Variable.Parameter to SyntaxStyle(color = Color(0xFF7DCFFF)),
        ),
        LanguageId.fromString("myql") to SyntaxRoleStyles(
            SyntaxRole.of("custom.myql.directive") to SyntaxStyle(
                color = Color(0xFF9333EA),
                fontWeight = FontWeight.SemiBold,
            ),
        ),
    ),
)
```

Language-override keys are `LanguageId` values, so they participate in the same canonical-id discipline as everything else: a built-in like `LanguageId.Kotlin` matches whatever the tokenizer puts on `SyntaxTokenSpan.languageId`, and a custom language registered through `LanguageId.fromString("myql")` matches when an extension tokenizer emits spans for that id.

## Resolution policy

Theme resolution is **not first-match.** It's an additive merge along the role's parent chain, applied globally first and then through any language override.

For each `SyntaxTokenSpan`, `SyntaxTheme.resolveSpanStyle(span)` does this:

1. Compute the parent chain of `span.role`, root to exact. For `keyword.declaration` that's `[keyword, keyword.declaration]`. For `constant.builtin.true` it's `[constant, constant.builtin, constant.builtin.true]`.
2. Walk the chain against `roleStyles`. Each match merges into a running `SyntaxStyle`: `fontWeight` and `fontStyle` use the new non-null value if present, otherwise keep the previous; `color` uses the new value if it's not `Color.Unspecified`, otherwise keeps the previous.
3. If `languageOverrides[span.languageId]` is present, walk the same chain against that language's overrides and merge into the running style the same way.
4. The merged `SyntaxStyle` becomes a Compose `SpanStyle`. If nothing matched, the result is an empty `SyntaxStyle()` and the host's `TextStyle` remains the visible base.

### Worked example 1: `keyword.declaration` in `kotlin`

Given the theme above and a `SyntaxTokenSpan` with `role = keyword.declaration`, `languageId = LanguageId.Kotlin`:

| Step | Source | Style contribution | Running result |
|---|---|---|---|
| 1 | global `keyword` | color `#3B73D9`, semibold | color `#3B73D9`, semibold |
| 2 | global `keyword.declaration` | (no entry) | unchanged |
| 3 | kotlin `keyword` | (no entry) | unchanged |
| 4 | kotlin `keyword.declaration` | (no entry) | unchanged |

Final span style: color `#3B73D9`, font weight semibold.

### Worked example 2: `keyword.control` in `kotlin`

Same theme. Token span: `role = keyword.control`, `languageId = LanguageId.Kotlin`.

| Step | Source | Style contribution | Running result |
|---|---|---|---|
| 1 | global `keyword` | color `#3B73D9`, semibold | color `#3B73D9`, semibold |
| 2 | global `keyword.control` | (no entry) | unchanged |
| 3 | kotlin `keyword` | (no entry) | unchanged |
| 4 | kotlin `keyword.control` | weight bold | color `#3B73D9`, **bold** |

Final span style: color `#3B73D9`, font weight bold. The language override sharpened the weight without restating the color.

### Worked example 3: `constant.builtin.true` (no language override needed)

Token span: `role = constant.builtin.true`, language doesn't matter. Theme:

```kotlin
roleStyles = SyntaxRoleStyles(
    SyntaxRole.Constant to SyntaxStyle(color = Color(0xFFB91C1C)),
    SyntaxRole.Constant.Builtin to SyntaxStyle(fontStyle = FontStyle.Italic),
)
```

| Step | Source | Style contribution | Running result |
|---|---|---|---|
| 1 | global `constant` | color `#B91C1C` | color `#B91C1C` |
| 2 | global `constant.builtin` | italic | color `#B91C1C`, italic |
| 3 | global `constant.builtin.true` | (no entry) | unchanged |

The italic is inherited by every `constant.builtin.*` span, including `constant.builtin.null`, `constant.builtin.false`, and custom dotted refinements, without you having to enumerate them.

## Per-language overrides in practice

Two worked overrides, one for a built-in language and one for a custom extension language. Both define the theme directly so the structure is obvious at a glance.

### Kotlin: types as global, control keywords bold, named arguments distinct

The built-in `KotlinTokenizer` emits named call arguments at `SyntaxRole.Variable.Parameter` and tags declaration and control words with the appropriate keyword refinements. A JetBrains-flavored Kotlin override looks like:

```kotlin
val theme = SyntaxTheme(
    roleStyles = SyntaxRoleStyles(
        SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9), fontWeight = FontWeight.SemiBold),
        SyntaxRole.Type to SyntaxStyle(color = Color(0xFF6D28D9)),
        SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
        SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF7A7F87), fontStyle = FontStyle.Italic),
    ),
    languageOverrides = mapOf(
        LanguageId.Kotlin to SyntaxRoleStyles(
            // Bold control keywords; inherit color from the global Keyword style.
            SyntaxRole.Keyword.Control to SyntaxStyle(fontWeight = FontWeight.Bold),
            // Named arguments at call sites get a distinct tint.
            SyntaxRole.Variable.Parameter to SyntaxStyle(color = Color(0xFF7DCFFF)),
            // Annotations italic without changing color.
            SyntaxRole.Annotation to SyntaxStyle(fontStyle = FontStyle.Italic),
        ),
    ),
)
```

Types are intentionally absent from the Kotlin override: `SyntaxRole.Type` from the global map is already what we want, and not restating it makes the intent clearer.

### Custom extension language registered as `LanguageId.fromString("myql")`

When an extension tokenizer emits roles for its own language id, language overrides key on that same id:

```kotlin
val myql = LanguageId.fromString("myql")

val theme = SyntaxTheme(
    roleStyles = SyntaxRoleStyles(
        SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9), fontWeight = FontWeight.SemiBold),
        SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
        SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF7A7F87), fontStyle = FontStyle.Italic),
    ),
    languageOverrides = mapOf(
        myql to SyntaxRoleStyles(
            // A custom dotted role the extension tokenizer emits.
            SyntaxRole.of("custom.myql.directive") to SyntaxStyle(
                color = Color(0xFF9333EA),
                fontWeight = FontWeight.SemiBold,
            ),
            // Restyle the shared comment role only for this language.
            SyntaxRole.Comment to SyntaxStyle(color = Color(0xFF6B7280)),
        ),
    ),
)
```

The custom role doesn't need to be registered anywhere; the tokenizer constructs it via `SyntaxRole.of("custom.myql.directive")` when emitting spans, and the theme constructs it the same way when describing the override.

## Tweaking an existing theme

For narrow edits like "I want `DefaultDark` with a different keyword color", four helper extensions return a new theme with one change applied, leaving everything else untouched:

```kotlin
fun SyntaxTheme.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxTheme
fun SyntaxTheme.withLanguageRoleStyle(languageId: LanguageId, role: SyntaxRole, style: SyntaxStyle): SyntaxTheme
fun SyntaxTheme.withLanguageRoleStyles(languageId: LanguageId, styles: SyntaxRoleStyles?): SyntaxTheme
fun SyntaxRoleStyles.withRoleStyle(role: SyntaxRole, style: SyntaxStyle): SyntaxRoleStyles
```

Use them when you want one or two tweaks on top of an existing theme:

```kotlin
val brand = SyntaxTheme.DefaultDark
    .withRoleStyle(
        role = SyntaxRole.Keyword,
        style = SyntaxStyle(color = Color(0xFF7F52FF), fontWeight = FontWeight.Bold),
    )

// Remove a previously-set language override entirely:
val cleared = brand.withLanguageRoleStyles(languageId = LanguageId.Kotlin, styles = null)
```

Each helper call allocates a fresh `SyntaxTheme` and one or two new maps, so a long chain creates intermediate themes that are immediately discarded. That cost is negligible for one-time theme setup, but for building a theme with many roles, direct `SyntaxTheme(...)` construction is clearer and lighter.

## App-owned theme threading

SyntaxMP does not ship a `CompositionLocal` for syntax themes. The library has no opinion about how host apps thread their theme through their composition tree, so it leaves the choice to you. The common pattern is a tiny per-app local:

```kotlin
val LocalAppSyntaxTheme = compositionLocalOf<SyntaxTheme> { SyntaxTheme.DefaultDark }

@Composable
fun AppSyntaxTheme(
    theme: SyntaxTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppSyntaxTheme provides theme) {
        content()
    }
}

@Composable
fun CodeSnippet(
    code: String,
    languageLabel: String?,
    engine: SyntaxTokenizer,
) {
    BasicText(
        text = rememberSyntaxAnnotatedString(
            code = code,
            languageLabel = languageLabel,
            engine = engine,
            theme = LocalAppSyntaxTheme.current,
        ),
    )
}
```

Apps that want a single theme can also pass `SyntaxTheme` directly to each call site without a local at all.

## Built-in starter themes

`SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` are SyntaxMP's starter themes. They:

- style each root role (`SyntaxRole.Keyword`, `SyntaxRole.String`, `SyntaxRole.Number`, ...) with foreground colors tuned for the light or dark surface;
- do **not** style child refinements (`keyword.control`, `string.regex`, etc.), since refined-role styling is what most apps want to customize first;
- do **not** paint backgrounds, set font family or size, or carry any per-language overrides;
- are intended as the start of a theme, not the finished product.

The two starter themes mirror each other's role set, so swapping based on the host's light/dark mode is a one-line change. For anything beyond a one-or-two-role tweak, define your own `SyntaxTheme(...)` directly; for the small-tweak case, the [helpers above](#tweaking-an-existing-theme) work fine.

## When you need a richer `SpanStyle`

`SyntaxStyle` covers foreground color, weight, and style. If you need `background`, `textDecoration`, `letterSpacing`, a custom brush, or any other `SpanStyle` field, skip the theme system and build spans yourself from `engine.tokenize(...)` output:

```kotlin
val tokens: List<SyntaxTokenSpan> = engine.tokenize(code = code, languageLabel = languageLabel)

val annotated = AnnotatedString.Builder(code).apply {
    tokens.forEach { token ->
        val style: SpanStyle = mySpanStyleFor(token.role, token.languageId)
        addStyle(style, token.start, token.endExclusive)
    }
}.toAnnotatedString()
```

Each `SyntaxTokenSpan` carries its role and `languageId`, so a `when` on either lets you key per-role or per-language styling.

## Where to go next

- [docs/syntax-roles.md](syntax-roles.md): the roles primer. Root and refinement constants, custom-role factories.
- [docs/languages.md](languages.md): per-language catalog of every role each built-in tokenizer emits, plus aliases, `LanguageId` constants, and embedded-language routing.
- [docs/language-extension.md](language-extension.md): adding a custom language so you have something to theme.
- [docs/building-an-editor.md](building-an-editor.md): applying the resolved spans to read-only renders and editable text fields.
- [docs/architecture.md](architecture.md): where theme resolution sits in the broader pipeline.
