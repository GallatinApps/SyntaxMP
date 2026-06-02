<div align="center">

# SyntaxMP

**Kotlin Multiplatform syntax highlighting for Compose.**

Purpose-built lexical tokenizers, role-based theming, and drop-in Compose text helpers. 39 built-in languages, no JS runtime, no regex grammars, no platform code.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.11.0-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![Platforms](https://img.shields.io/badge/Platforms-JVM%20%7C%20Android%20%7C%20iOS%20%7C%20Wasm-blue)](#)
[![Demo](https://img.shields.io/badge/demo-demo.syntaxmp.com-blue)](https://demo.syntaxmp.com)
[![Version](https://img.shields.io/badge/version-0.2.0-blue)](#)

</div>

<p align="center">
  <a href="https://demo.syntaxmp.com/"><strong>→ Check Out The Web Demo! ←</strong></a>
</p>

---

## Contents

- [Highlights](#highlights)
- [Supported languages](#supported-languages)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Theming](#theming)
- [Choosing a language subset](#choosing-a-language-subset)
- [Adding your own language](#adding-your-own-language)
- [Documentation](#documentation)
- [FAQ](#faq)
- [Changelog](#changelog)
- [License](#license)

---

## Highlights

- **Pure Kotlin / KMP-clean.** Common code only. No platform shims, no JS bridge, no native parsers, no regex grammars.
- **Predictable token model.** Each token has one dotted `SyntaxRole` value (`keyword`, `keyword.control`, `variable.parameter`, ...), plus the non-null `LanguageId` that produced it. Themes match roles with progressive parent fallback.
- **Small, opinionated theme surface.** A `SyntaxStyle` is just color + weight + style. Host `TextStyle` owns font family, size, line height, base color, and backgrounds.
- **No global state, no auto-detection.** You pass a raw language label such as `"kotlin"` or `"kt"`; the engine resolves built-in aliases and extension aliases. The engine is a pure function of `(code, languageLabel)`. Easy to test, safe to share.
- **Primitives, not wrappers.** You compose `rememberSyntaxAnnotatedString` + `BasicText` for read-only views, or `buildSyntaxStyledSpans` + `applySyntaxStyledSpans` for `BasicTextField` editors. Engine and theme scoping is the host's choice.

---

## Supported languages

39 built-in languages, all driven by shared scanners, scanner options, and explicit vocabulary inputs. Pass a language label such as a built-in id or any true alias for the same public language identity (`js`, `ts`, `env`, `bash`, `zsh`, `kts`, `pgsql`, `sqlite3`, ...).

<table>
  <tr>
    <td valign="top">
      • Bash<br>
      • C<br>
      • C#<br>
      • C++<br>
      • CSS<br>
      • CSV<br>
      • Dart<br>
      • Diff / patch<br>
      • Dockerfile<br>
      • Dotenv<br>
    </td>
    <td valign="top">
      • Go<br>
      • GraphQL<br>
      • HTML<br>
      • INI<br>
      • Java<br>
      • JavaScript<br>
      • JSON<br>
      • JSX<br>
      • Kotlin<br>
      • Makefile<br>
    </td>
    <td valign="top">
      • Markdown<br>
      • PHP<br>
      • PostgreSQL<br>
      • PowerShell<br>
      • Properties<br>
      • Protobuf<br>
      • Python<br>
      • Ruby<br>
      • Rust<br>
      • Shell<br>
    </td>
    <td valign="top">
      • SQL<br>
      • SQLite<br>
      • Swift<br>
      • TOML<br>
      • TSX<br>
      • TypeScript<br>
      • XML<br>
      • YAML<br>
      • Zsh<br>
    </td>
  </tr>
</table>

Built-in language constants and the built-in set live on [`LanguageId`](syntaxmp-tokenizer/src/commonMain/kotlin/com/gallatinapps/syntaxmp/language/LanguageId.kt).

**Embedded languages** are wired up internally for HTML script/style blocks, Markdown fenced code, and the script/style regions of JSX and TSX. See [docs/embedded-languages.md](docs/embedded-languages.md) for the full routing table and what's deliberately out of scope.

---

## Installation

SyntaxMP targets **JVM**, **Android**, **iOS arm64**, **iOS simulator arm64**, and **web through Kotlin/Wasm**.

### Version catalog (`gradle/libs.versions.toml`)

```toml
[versions]
syntaxmpVersion = "0.2.0"

[libraries]
syntaxmp = { module = "com.gallatinapps.syntaxmp:syntaxmp", version.ref = "syntaxmpVersion" }
```

### Module `build.gradle.kts`

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.syntaxmp)
        }
    }
}
```

The `syntaxmp` coordinate is the Compose highlighter and pulls in the pure tokenizer layer transitively. Token-only consumers that build their own renderer can depend on `com.gallatinapps.syntaxmp:syntaxmp-tokenizer` directly; see [docs/architecture.md](docs/architecture.md#modules-and-coordinates).

---

## Quick start

SyntaxMP has two Compose paths, depending on whether the text is displayed or editable.

**Display highlighted text.** `rememberSyntaxAnnotatedString` builds an `AnnotatedString` you can drop into `BasicText`. The Composable handles its own `remember` chain, so theme changes restyle without retokenizing:

```kotlin
@Composable
fun CodeSnippet(
    code: String,
    languageLabel: String?,
    engine: SyntaxTokenizer,
    theme: SyntaxTheme,
) {
    BasicText(
        text = rememberSyntaxAnnotatedString(
            code = code,
            languageLabel = languageLabel,
            engine = engine,
            theme = theme,
        ),
        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
    )
}
```

**Editable text.** `buildSyntaxStyledSpans` plus the `TextFieldBuffer.applySyntaxStyledSpans` extension drops into a `BasicTextField` `outputTransformation`:

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
            val spans = buildSyntaxStyledSpans(code = code, spans = tokens, theme = theme)
            applySyntaxStyledSpans(spans)
        },
        textStyle = TextStyle(fontFamily = FontFamily.Monospace),
    )
}
```

Construct the engine once for the scope that owns your syntax configuration, and pass it through your app's existing wiring (a host-defined `staticCompositionLocalOf`, DI, or a one-surface `remember`). SyntaxMP deliberately doesn't ship that wiring.

See [docs/building-an-editor.md](docs/building-an-editor.md) for engine sharing, line splitting, caching, and large-document guidance.

---

## Theming

`SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` are starter themes, but most apps should define a theme that fits their own editor surface. SyntaxMP themes only syntax roles: color, optional weight, and optional style. Font family, size, line height, and backgrounds stay in your app's `TextStyle` and layout.

```kotlin
val roleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9)),
    SyntaxRole.Operator to SyntaxStyle(color = Color(0xFF4B5563)),
    SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFF6B7280)),
    SyntaxRole.Function to SyntaxStyle(color = Color(0xFF6F42C1)),
    SyntaxRole.Type to SyntaxStyle(color = Color(0xFFB45309)),
    SyntaxRole.Property to SyntaxStyle(color = Color(0xFF0F766E)),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
    SyntaxRole.Number to SyntaxStyle(color = Color(0xFFAD3DA4)),
    SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF22863A)),
    SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF6F42C1)),
    SyntaxRole.Comment to SyntaxStyle(
        color = Color(0xFF7A7F87),
        fontStyle = FontStyle.Italic,
    ),
)

val theme = SyntaxTheme(roleStyles = roleStyles)

BasicText(
    text = rememberSyntaxAnnotatedString(
        code = code,
        languageLabel = "kotlin",
        engine = engine,
        theme = theme,
    ),
    style = TextStyle(fontFamily = FontFamily.Monospace),
)
```

See [docs/theming.md](docs/theming.md) for the full role tree, resolution policy, all four copy/override helpers, and worked per-language overrides.

---

## Choosing a language subset

By default the engine enables all 39 built-ins. Shrink the surface (smaller construction cost, fewer code paths reachable) by passing a `Set<LanguageId>`:

```kotlin
val enabledLanguages = setOf(
    LanguageId.Kotlin,
    LanguageId.Json,
    LanguageId.Markdown,
    LanguageId.Shell,
)
val engine = SyntaxTokenizer(builtInLanguages = enabledLanguages)
```

Labels resolving to a disabled language return `emptyList()`. The engine never throws "unknown language."

---

## Adding your own language

Implement `LanguageTokenizer`, wrap it in a `LanguageExtension`, register the extension on the engine, and your tokenizer runs alongside the built-ins:

```kotlin
val myql = LanguageId.fromString("myql")

val engine = SyntaxTokenizer(
    extensions = listOf(
        LanguageExtension(
            languageId = myql,
            aliases = setOf("mql"),
            tokenizer = myqlTokenizer,
        ),
    ),
)
```

Extensions resolve before built-ins, so you can override a built-in too. See [docs/language-extension.md](docs/language-extension.md) for full working examples.

---

## Documentation

- [docs/architecture.md](docs/architecture.md): the pipeline a snippet travels through, where state lives, cross-platform posture, strengths and limits.
- [docs/api.md](docs/api.md): per-symbol API reference for every public type, function, and extension SyntaxMP ships.
- [docs/theming.md](docs/theming.md): the full theming reference. Role tree, resolution policy, copy/override helpers, per-language overrides.
- [docs/syntax-roles.md](docs/syntax-roles.md): the roles primer. What a `SyntaxRole` is, the root and refinement constants, and how custom roles work.
- [docs/languages.md](docs/languages.md): per-language catalog of every role each built-in tokenizer emits, plus aliases, `LanguageId` constants, and embedded-language routing.
- [docs/language-extension.md](docs/language-extension.md): adding a custom language via `LanguageExtension`, with a worked tokenizer and a testing recipe.
- [docs/building-an-editor.md](docs/building-an-editor.md): building an editable code surface with `BasicTextField`. Engine sharing, line splitting, caching, and large-document guidance.
- [docs/embedded-languages.md](docs/embedded-languages.md): what SyntaxMP routes automatically for HTML, Markdown, JSX, and TSX, and what it deliberately doesn't.

---

## FAQ

**How does it know what language my code is?**

It doesn't. You tell it. Pass a language label such as `"kotlin"` or `"kt"`, or get back no spans. Auto-detection is a separate problem with different correctness and performance tradeoffs, and is out of scope here.

**What if the language isn't recognized?**

The engine returns `emptyList()`. `rememberSyntaxAnnotatedString` falls back to a plain unstyled `AnnotatedString` when the label is `null` or blank, without ever invoking the engine. The engine itself never throws for unknown or unregistered languages.

**My language isn't supported. What should I do?**

You can add project-local support with `LanguageExtension`, including aliases and overrides for built-ins. If you want a language added as a built-in, search existing issues first. If there is no issue, open one with the language name, why it belongs in the built-in set, common labels or extensions, and a few representative snippets that should highlight well. PRs are welcome, especially when they start from a working extension, but built-in additions are not guaranteed: the core set stays focused on broadly useful languages so the library remains maintainable.

**Can I tokenize large files?**

Yes, within limits, and the limit is usually Compose text rendering rather than SyntaxMP. Tokenization itself is fast: a single-pass, full-document lexical scan with no regex or grammar runtime. For read-only display (`rememberSyntaxAnnotatedString` + `BasicText`) that scales to large files. Editing is the real constraint: `BasicTextField` doesn't virtualize text layout, so every keystroke re-measures the whole document. That base layout cost is unavoidable, but the styling cost is not: applying styled spans only to the visible range and tokenizing off the main thread keep large editable documents usable. See [docs/building-an-editor.md](docs/building-an-editor.md) for windowing, off-thread tokenization, caching, and large-document guidance.

**Token color looks wrong. Is that a bug?**

Maybe. Scanners are heuristic. Before filing it, check (a) what `span.role.value` and `span.languageId.value` the wrong span actually got, and (b) whether the issue is the scanner choosing the wrong role, or the theme styling that role in an unexpected way. The fixes differ.

**Why is `SyntaxStyle` so restrictive?**

The narrow shape is deliberate. Color + weight + style covers the visual decisions that should live with the syntax theme; font family, size, line height, and surfaces belong with your app's design system. If you need full `SpanStyle` control for a token, resolve styles yourself from raw `SyntaxTokenSpan`s and skip the theme system.

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## License

Copyright 2026 Gallatin Applications LLC.

SyntaxMP is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).

The demo app bundles JetBrains Mono font files under the SIL Open Font License, Version 1.1. See [syntaxmp-demo/THIRD_PARTY_NOTICES.md](syntaxmp-demo/THIRD_PARTY_NOTICES.md).
