# SyntaxMP Roles

Roles are how SyntaxMP labels each token. This doc covers what a role is, the root and refinement constants, and the factories for custom paths. The per-language catalog of which roles each built-in tokenizer emits lives in [languages.md](languages.md).

## What a role is

Every token SyntaxMP emits, from every keyword to every string literal to every closing brace, carries a `SyntaxRole`. The role is what a `SyntaxTheme` looks up to decide how to style that token. Roles are dotted-path identifiers:

```
keyword
keyword.control
keyword.declaration
function
function.builtin
variable.parameter
markup.diff.addition
```

Roles are organized as a tree. `keyword.control` is a refinement of `keyword`; `function.builtin` is a refinement of `function`; `markup.diff.addition` is a refinement of `markup.diff`, which itself is a refinement of `markup`. A theme that styles `keyword` automatically covers every `keyword.*` refinement through `SyntaxTheme`'s parent-walk resolution, and you can layer a more-specific style on a refinement to override the parent. See [docs/theming.md](theming.md) for the full resolution policy.

Theme authors should key `SyntaxRoleStyles` by `SyntaxRole` constants, not raw strings. Use the typed constants (`SyntaxRole.Keyword.Control`, `SyntaxRole.Function.Builtin`, ...) when they exist, and use `SyntaxRole.of("custom.path")` or `SyntaxRole.Keyword.append("sql")` for paths that don't have a constant.

## The root roles

The sixteen root roles below are what the `DefaultLight` and `DefaultDark` starter themes style. A theme that sets all sixteen has a working foundation; refinements layer on top.

| Class | Role | What it covers |
|---|---|---|
| `SyntaxRole.Keyword` | `keyword` | Grammar and control words: `if`, `return`, `class`, `import`, `select`, `@media`. |
| `SyntaxRole.String` | `string` | String literals across all forms: quoted, raw, triple-quoted, heredoc, template. |
| `SyntaxRole.Number` | `number` | Numeric literals of any kind. |
| `SyntaxRole.Comment` | `comment` | Line and block comments. |
| `SyntaxRole.Function` | `function` | Callable names: function calls, declarations, builtins. |
| `SyntaxRole.Type` | `type` | Type-like names: primitive types, classes, structs, type aliases. |
| `SyntaxRole.Property` | `property` | Object properties, struct fields, configuration keys, dot-access members. |
| `SyntaxRole.Variable` | `variable` | Identifiers that don't fit a more specific role. |
| `SyntaxRole.Operator` | `operator` | Arithmetic, comparison, assignment, and language-specific operators (`=`, `?.`, `::`, `<=>`). |
| `SyntaxRole.Punctuation` | `punctuation` | Structural delimiters: braces, brackets, commas, semicolons. |
| `SyntaxRole.Annotation` | `annotation` | Annotations, decorators, attributes, preprocessor directives, metadata. |
| `SyntaxRole.Tag` | `tag` | Markup tag names and tag-like selectors. |
| `SyntaxRole.Attribute` | `attribute` | Markup attributes and CSS selector attributes. |
| `SyntaxRole.Constant` | `constant` | Constants and literal-like values that aren't strings or numbers. |
| `SyntaxRole.Escape` | `escape` | String escapes, interpolation delimiters, format specifiers. |
| `SyntaxRole.Markup` | `markup` | Document/Markdown/diff/component structural roles. |

## Refinements

Most roots also expose refinements as nested constants. Style a refinement to differentiate a specific kind of token from its parent; leave it unset and the parent's style applies.

| Class | Role | Where it's used |
|---|---|---|
| `SyntaxRole.Keyword.Control` | `keyword.control` | Control-flow keywords (`if`, `while`, `return`). |
| `SyntaxRole.Keyword.Declaration` | `keyword.declaration` | Declaration keywords (`class`, `fun`, `def`, `struct`). |
| `SyntaxRole.Keyword.Modifier` | `keyword.modifier` | Modifier keywords (`public`, `private`, `static`, `async`). |
| `SyntaxRole.Keyword.AtRule` | `keyword.at-rule` | CSS at-rules (`@media`, `@keyframes`, `@mixin`). |
| `SyntaxRole.String.Regex` | `string.regex` | Regex literals (JS, TS). |
| `SyntaxRole.String.Language` | `string.language` | Markdown fence language labels. |
| `SyntaxRole.String.Url` | `string.url` | URL targets in Markdown link references. |
| `SyntaxRole.Function.Builtin` | `function.builtin` | Standard-library callables (`println`, `print`, `count`). |
| `SyntaxRole.Function.Declaration` | `function.declaration` | Function declarations (Kotlin). |
| `SyntaxRole.Function.Member` | `function.member` | Dot-call member functions (Kotlin). |
| `SyntaxRole.Function.Macro` | `function.macro` | Macro calls (`println!`, `macro_rules!` in Rust). |
| `SyntaxRole.Property.Name` | `property.name` | Object keys in JSON, YAML, TOML. |
| `SyntaxRole.Property.Quoted` | `property.quoted` | Quoted identifiers (`"Title"` in SQL, `[note]` in SQLite). |
| `SyntaxRole.Property.Section` | `property.section` | Section headers in INI and TOML. |
| `SyntaxRole.Variable.Parameter` | `variable.parameter` | Sigil variables (`$name` in PHP/Ruby), named arguments (Kotlin), bind placeholders (SQLite). |
| `SyntaxRole.Variable.Namespace` | `variable.namespace` | Package, import, and module paths. |
| `SyntaxRole.Attribute.Directive` | `attribute.directive` | Framework directive attributes (`v-if`, `on:click`, `client:load`). |
| `SyntaxRole.Attribute.Pseudo` | `attribute.pseudo` | CSS pseudo classes and pseudo elements. |
| `SyntaxRole.Punctuation.Expression` | `punctuation.expression` | JSX/TSX expression-island braces. |
| `SyntaxRole.Constant.Builtin` | `constant.builtin` | Language-level literals (`true`, `false`, `null`, `nil`, `None`). |
| `SyntaxRole.Constant.Color` | `constant.color` | Hex colors in CSS values. |
| `SyntaxRole.Constant.Atom` | `constant.atom` | Atom-like constants in custom language extensions. |
| `SyntaxRole.Markup.Expression` | `markup.expression` | Component-template expression delimiters for custom tokenizer extensions. |
| `SyntaxRole.Markup.Frontmatter` | `markup.frontmatter` | Frontmatter fences in custom tokenizer extensions. |
| `SyntaxRole.Markup.Cdata` | `markup.cdata` | XML `<![CDATA[…]]>` markers. |

## Custom roles and language-specific paths

The role tree isn't closed. Two factories let you produce roles outside the built-in set:

```kotlin
// An exact custom role with an arbitrary dotted path.
SyntaxRole.of("custom.myql.directive")

// A refinement anchored under an existing root.
SyntaxRole.Keyword.append("sql")            // keyword.sql
SyntaxRole.Variable.append("yaml.anchor")   // variable.yaml.anchor
SyntaxRole.Markup.append("diff.addition")   // markup.diff.addition
```

`SyntaxRole.of(path)` and `<role>.append(suffix)` produce equal `SyntaxRole` values when the resulting paths match: `SyntaxRole.of("keyword.sql")` and `SyntaxRole.Keyword.append("sql")` are the same role. Use `append` when you have a parent root and want the parent's styling to cascade to your custom path for free; use `of` when you have a complete dotted path to construct.

## Per-language role catalog

For the exhaustive list of roles each built-in tokenizer emits, plus aliases, `SyntaxLanguageId` constants, and embedded-language routing, see [languages.md](languages.md).
