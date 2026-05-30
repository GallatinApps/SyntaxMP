# Built-in languages

This document lists the languages SyntaxMP ships with built-in tokenizers for. Each section gives the canonical `SyntaxLanguageId` constant, the label aliases that resolve to that language through `SyntaxTokenizerEngine.resolveLanguageId(languageLabel)`, any embedded languages the tokenizer can route into for embedded content, and the full table of roles the tokenizer can emit.

For background on what roles are and how the tree is shaped, see [syntax-roles.md](syntax-roles.md).

## How to read these sections

Each section below lists, for one built-in language:

- **Language id**: the canonical `SyntaxLanguageId` constant.
- **Aliases**: every label that resolves to this language.
- **Embedded languages**: when present, the other languages this tokenizer can route into for embedded content.
- **Prose notes**: anything special about how this language tokenizes (embedded-language routing, frontmatter handling, raw-text regions, etc.).
- **Role table**: every role the tokenizer can emit, what triggers it, and a short example.

The role table is exhaustive: structural roles like `operator`, `punctuation`, and `variable` appear alongside the language-distinctive ones. If you're shaping a theme for one language only, focus on that section. If you're styling broadly applicable roles (e.g. `keyword` everywhere), the [root roles table](syntax-roles.md#the-root-roles) in the roles primer is usually enough.

Languages are listed alphabetically.

---

## Apache Config

- **Language id**: `SyntaxLanguageId.ApacheConf`
- **Aliases**: `apache`, `apacheconf`, `apache-config`, `htaccess`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | Directive names | `ServerName` |
| `tag` | Block names | `VirtualHost` |
| `variable` | Directive arguments | `*:80` |
| `string` | Quoted directive arguments | `"example.test"` |
| `operator` | Argument operators | `/` |
| `punctuation` | Block delimiters | `<` |
| `comment` | Comments | `# note` |

## Astro

- **Language id**: `SyntaxLanguageId.Astro`
- **Aliases**: `astro`
- **Embedded languages**: TypeScript (frontmatter), JavaScript / TypeScript (`<script>` based on `lang`), CSS / SCSS / Less (`<style>` based on `lang`)

Astro components mix `---`-delimited TypeScript frontmatter, component markup, and embedded `<script>` / `<style>` blocks.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `class` |
| `attribute.directive` | Astro directives (`client:`, `set:`, `is:`) and component directives (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:`) | `client:load` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters | `/>` |
| `comment` | Markup comments | `<!-- note -->` |
| `markup.expression` | Component expression delimiters | `{` |
| `markup.frontmatter` | Astro frontmatter fences | `---` |

## C

- **Language id**: `SyntaxLanguageId.C`
- **Aliases**: `c`, `h`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Preprocessor lines | `#include <stdio.h>` |
| `keyword.declaration` | Declaration keywords | `struct` |
| `keyword.control` | Control-flow keywords | `return` |
| `type` | Type keywords | `int` |
| `function.builtin` | Known C library calls | `printf` |
| `function` | Calls and definitions | `score` |
| `property` | Struct fields | `title` |
| `constant` | Enum members | `NOTE_DRAFT` |
| `constant.builtin.<value>` | Built-in constants | `NULL` |
| `escape` | String escapes and printf format specifiers | `%s` |
| `string` | Strings | `"ok"` |
| `number` | Numeric literals | `0xFF` |
| `operator` | Arithmetic, comparison, assignment, and pointer operators | `=`, `+`, `>` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `/* note */` |

## C#

- **Language id**: `SyntaxLanguageId.CSharp`
- **Aliases**: `cs`, `c#`, `csharp`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Bracket attributes | `[Serializable]` |
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `return` |
| `keyword.modifier` | Modifiers | `public` |
| `type` | Type keywords, uppercase names, and known framework types | `string` |
| `variable.namespace` | Known framework namespaces | `Math` |
| `function` | Calls and methods | `Score` |
| `property` | Dot-access members | `Count` |
| `constant.builtin.<value>` | Literals | `true` |
| `escape` | Interpolated string delimiters | `{` |
| `string` | Quoted, verbatim, interpolated, and raw strings | `"ok"` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, and null-coalescing operators | `=`, `??`, `?.` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `// note` |

## C++

- **Language id**: `SyntaxLanguageId.Cpp`
- **Aliases**: `cc`, `c++`, `cpp`, `cxx`, `hpp`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Preprocessor lines and standard attributes | `[[nodiscard]]` |
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `return` |
| `keyword.modifier` | Modifiers | `public` |
| `type` | Type keywords and uppercase names | `int` |
| `function.builtin` | Known standard calls | `move` |
| `function` | Calls and methods | `score` |
| `variable.namespace` | Namespace declarations and using paths | `std::chrono` |
| `constant` | Enum members | `Draft` |
| `constant.builtin.<value>` | Built-in constants | `nullptr` |
| `escape` | String escapes and printf format specifiers | `%s` |
| `string` | Quoted, prefixed, and raw strings | `"ok"` |
| `number` | Numeric literals | `1'000` |
| `operator` | Arithmetic, comparison, assignment, and pointer operators | `=`, `->`, `<<` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `// note` |

## CSS

- **Language id**: `SyntaxLanguageId.Css`
- **Aliases**: `css`

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Type selectors | `button` |
| `attribute` | Class/id selectors and attributes | `.card` |
| `attribute.pseudo` | Pseudo classes and elements | `hover` |
| `property` | Property names and custom property declarations | `color`, `--accent` |
| `variable.css.custom-property` | CSS custom property references | `--accent` |
| `keyword.at-rule` | CSS at-rules | `@media` |
| `function` | CSS function names | `calc` |
| `constant.color` | Hex colors in values | `#38a3ff` |
| `constant.builtin.important` | Important marker | `!important` |
| `variable` | Unclassified value identifiers | `sans-serif` |
| `string` | Quoted strings | `"status"` |
| `number` | Numbers and units | `12px` |
| `operator` | Operators in selectors and values | `*` |
| `punctuation` | Braces, brackets, commas, colons, and semicolons | `{` |
| `comment` | Block comments | `/* note */` |

## CSV

- **Language id**: `SyntaxLanguageId.Csv`
- **Aliases**: `csv`

| Role | Where emitted | Example |
|---|---|---|
| `punctuation` | Commas between fields | `,` |

## Dart

- **Language id**: `SyntaxLanguageId.Dart`
- **Aliases**: `dart`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `if` |
| `keyword.modifier` | Modifiers | `final` |
| `annotation` | Metadata annotations | `@override` |
| `type` | Types and uppercase names | `String` |
| `function.builtin` | Built-in callables | `print` |
| `function` | Calls and definitions | `score` |
| `property` | Dot-access members | `length` |
| `constant.builtin.<value>` | Literals | `null` |
| `escape` | String interpolation delimiters | `${` |
| `string` | Quoted, triple, and raw strings | `"ok"` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, and null-aware operators | `=`, `??`, `?.` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `// note` |

## Diff

- **Language id**: `SyntaxLanguageId.Diff`
- **Aliases**: `diff`, `patch`

| Role | Where emitted | Example |
|---|---|---|
| `markup.diff.header` | File header lines | `diff --git a b` |
| `markup.diff.hunk` | Hunk headers | `@@ -1 +1 @@` |
| `markup.diff.context` | Unchanged context lines | ` unchanged` |
| `markup.diff.deletion` | Removed lines | `-old` |
| `markup.diff.addition` | Added lines | `+new` |
| `markup.diff.no-newline` | No-newline marker lines | `\ No newline at end of file` |

## DNS Zone

- **Language id**: `SyntaxLanguageId.DnsZone`
- **Aliases**: `dns`, `zone`, `bind`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | Zone directives | `$ORIGIN` |
| `keyword` | DNS classes | `IN` |
| `type` | Record types | `AAAA` |
| `property` | Record owner names | `www` |
| `variable` | Record data | `mail.example.test.` |
| `string` | Quoted TXT data | `"v=spf1 ~all"` |
| `number` | TTLs, priorities, and serials | `3600` |
| `comment` | Comments | `; note` |

## Dockerfile

- **Language id**: `SyntaxLanguageId.Dockerfile`
- **Aliases**: `docker`, `dockerfile`, `containerfile`
- **Embedded languages**: Shell (`RUN` heredoc bodies shaped as shell scripts)

Heredoc markers and closing delimiters stay Dockerfile strings.

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | Docker instructions | `FROM` |
| `attribute` | Instruction flags | `--mount` |
| `variable` | Variables | `$HOME` |
| `string` | Quoted strings and heredoc bodies | `"hello"` |
| `number` | Numeric literals | `1000` |
| `operator` | Command and option operators | `+` |
| `punctuation` | Brackets, braces, commas, colons, and parens | `[` |
| `comment` | Comments | `# note` |

## Elixir

- **Language id**: `SyntaxLanguageId.Elixir`
- **Aliases**: `elixir`, `ex`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `defmodule` |
| `keyword.control` | Control keywords | `case` |
| `type` | Module aliases | `MyModule` |
| `function.builtin` | Built-in callables | `inspect` |
| `function` | Calls | `score` |
| `property` | Dot-access members | `name` |
| `annotation` | Attributes | `@moduledoc` |
| `constant.atom` | Atoms | `:ok` |
| `constant.builtin.<value>` | Built-in constants | `true` |
| `operator` | Pipeline and symbolic operators | `\|>` |
| `escape` | Interpolation delimiters | `#{` |
| `string` | Strings | `"Scores jobs"` |
| `number` | Numeric literals | `2` |
| `comment` | Comments | `# note` |

## GLSL

- **Language id**: `SyntaxLanguageId.Glsl`
- **Aliases**: `glsl`, `vert`, `frag`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Preprocessor lines | `#version 300 es` |
| `keyword` | Shader qualifiers and control flow | `uniform` |
| `type` | Shader scalar/vector/matrix types | `vec3` |
| `function.builtin` | Built-in callable names | `texture` |
| `function` | User functions | `main` |
| `property` | Member access | `rgb` |
| `number` | Numeric literals | `1.0` |
| `operator` | Arithmetic, comparison, and assignment operators | `=`, `+`, `*` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Uniform/varying/attribute identifiers | `viewport` |
| `comment` | Line and block comments | `// note` |

## Go

- **Language id**: `SyntaxLanguageId.Go`
- **Aliases**: `go`, `golang`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `func` |
| `keyword.control` | Control-flow keywords | `if` |
| `type` | Built-in and uppercase types | `int` |
| `function.builtin` | Built-in callables | `make` |
| `function` | Calls and definitions | `score` |
| `property` | Struct fields and dot-access members | `Title` |
| `constant` | Identifiers introduced by `const` | `Draft` |
| `constant.builtin.<value>` | Literals | `nil` |
| `escape` | Format verbs inside strings | `%03d` |
| `string` | Quoted and raw strings | `` `raw` `` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, and channel operators | `=`, `:=`, `<-` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `// note` |

## GraphQL

- **Language id**: `SyntaxLanguageId.GraphQl`
- **Aliases**: `graphql`, `gql`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | GraphQL keywords | `query` |
| `type` | Type-like names | `Note` |
| `function` | Field calls with arguments | `note` |
| `property` | Selection fields | `title` |
| `variable.parameter` | Variables | `$id` |
| `annotation` | Directives | `@include` |
| `constant.builtin.<value>` | Literals | `true` |
| `number` | Numeric literals | `10` |
| `string` | Block strings | `"""text"""` |
| `punctuation` | Braces, brackets, commas | `{` |
| `comment` | Comments | `# note` |

## Groovy

- **Language id**: `SyntaxLanguageId.Groovy`
- **Aliases**: `groovy`, `gvy`, `gy`, `gradle`, `gradle.groovy`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `if` |
| `type` | Type keywords and uppercase names | `String` |
| `function.builtin` | Built-in callables | `println` |
| `function` | Calls and definitions | `score` |
| `property` | Dot-access members | `name` |
| `escape` | GString interpolation delimiters | `${` |
| `string` | Quoted, triple, slashy, and dollar-slashy strings | `"hello"` |
| `constant.builtin.<value>` | Literals | `true` |
| `number` | Numeric literals | `1G` |
| `operator` | Arithmetic, comparison, assignment, and safe-navigation operators | `=`, `?.`, `?:` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Identifiers and parameters | `count` |
| `comment` | Comments | `// note` |

## HCL

- **Language id**: `SyntaxLanguageId.Hcl`
- **Aliases**: `hcl`

| Role | Where emitted | Example |
|---|---|---|
| `type` | Generic blocks | `server` |
| `property` | Attribute names | `host` |
| `variable` | Unclassified identifiers and labels | `web` |
| `function` | Function calls in expressions | `upper` |
| `string` | Quoted strings | `"localhost"` |
| `escape` | Interpolation delimiters | `${`, `}` |
| `constant.builtin.<value>` | Boolean and null values | `false` |
| `number` | Numeric literals | `8080` |
| `operator` | Assignment and arithmetic operators | `=`, `+` |
| `punctuation` | Braces, brackets, commas | `{` |
| `comment` | Comments | `// note` |

## HTML

- **Language id**: `SyntaxLanguageId.Html`
- **Aliases**: `html`, `htm`
- **Embedded languages**: JavaScript (`<script>` bodies), CSS (`<style>` bodies)

Attribute values are not delegated to child tokenizers.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Element names | `section` |
| `attribute` | Attribute names | `class` |
| `string` | Attribute values, CDATA bodies | `"note"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Named and numeric entities | `&amp;` |
| `punctuation` | Tag delimiters | `</` |
| `comment` | HTML comments | `<!-- note -->` |
| `annotation` | Doctype and processing forms | `<!doctype html>` |
| `markup.cdata` | CDATA section markers when present (rare in HTML; common in inline SVG) | `<![CDATA[` |

## INI

- **Language id**: `SyntaxLanguageId.Ini`
- **Aliases**: `ini`, `properties`, `env`, `dotenv`

| Role | Where emitted | Example |
|---|---|---|
| `property.section` | Section headers | `[database]` |
| `property` | Keys | `name` |
| `string` | String-like values | `value` |
| `number` | Numeric values | `8080` |
| `constant.builtin.<value>` | Boolean and null-like values | `true` |
| `keyword` | Supported directives | `export` |
| `operator` | Key-value separators (`=`, `:`) | `=` |
| `punctuation` | Brackets around section headers | `[` |
| `comment` | Comments | `# note` |

## Java

- **Language id**: `SyntaxLanguageId.Java`
- **Aliases**: `java`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Annotations | `@Override` |
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `return` |
| `keyword.modifier` | Modifiers | `public` |
| `type` | Primitive, uppercase, and known library types | `int` |
| `variable.namespace` | Package/import/module paths and known library namespaces | `Math` |
| `function` | Calls and methods | `label` |
| `property` | Dot-access members | `max` |
| `variable` | Fields and local identifiers | `names` |
| `constant` | Enum constants | `DRAFT` |
| `constant.builtin.<value>` | Literals | `null` |
| `escape` | String escapes and format specifiers | `%s` |
| `string` | Quoted strings and text blocks | `"ok"` |
| `number` | Numeric literals | `1L` |
| `operator` | Arithmetic, comparison, assignment, and bitwise operators | `=`, `+`, `<<` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## JavaScript

- **Language id**: `SyntaxLanguageId.JavaScript`
- **Aliases**: `js`, `javascript`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `function` |
| `keyword.control` | Control-flow keywords | `if` |
| `keyword.modifier` | Modifiers | `async` |
| `type` | Known global constructor types | `Array` |
| `variable.namespace` | Known global namespaces | `JSON` |
| `function` | Calls and declarations | `map` |
| `property` | Dot-access members and object keys | `log` |
| `variable` | Identifiers | `item` |
| `string.regex` | Regex literals | `/needle/` |
| `escape` | Template-string interpolation delimiters | `${` |
| `string` | Strings and template strings | `` `count` `` |
| `constant.builtin.<value>` | Literals | `undefined` |
| `number` | Numeric literals | `1n` |
| `operator` | Arithmetic, comparison, assignment, optional-chaining, nullish-coalescing, and spread operators | `===`, `?.`, `??`, `...` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## JSON

- **Language id**: `SyntaxLanguageId.Json`
- **Aliases**: `json`

| Role | Where emitted | Example |
|---|---|---|
| `property.name` | Object keys | `"name"` |
| `string` | String values | `"value"` |
| `number` | Numeric values | `42` |
| `constant.builtin.<value>` | JSON literals | `true` |
| `operator` | Key/value `:` separator | `:` |
| `punctuation` | Braces, brackets, commas | `{` |
| `escape` | String escapes | `\n` |

## JSON5

- **Language id**: `SyntaxLanguageId.Json5`
- **Aliases**: `json5`

| Role | Where emitted | Example |
|---|---|---|
| `property.name` | Quoted or unquoted object keys | `name` |
| `string` | Single- or double-quoted strings | `'value'` |
| `number` | Decimal, hex, and signed values | `0x2A` |
| `constant.builtin.<value>` | JSON5 literals | `NaN` |
| `operator` | Key/value `:` separator and signed-number sign | `:`, `-` |
| `punctuation` | Braces, brackets, commas | `{` |
| `variable` | Unquoted identifier keys treated as bare identifiers | `Infinity` |
| `comment` | Line and block comments | `// note` |
| `escape` | String escapes | ` ` |

## JSX

- **Language id**: `SyntaxLanguageId.Jsx`
- **Aliases**: `jsx`
- **Embedded languages**: JavaScript (top-level code and curly-brace expression islands)

JSX is JavaScript with embedded component markup. Top-level code emits JavaScript roles (see [JavaScript](#javascript)); component tags switch to the markup roles below.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `className` |
| `attribute.directive` | Component-style directive attributes when present (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:` prefixes) | `on:click` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters | `/>` |
| `comment` | Markup comments in JSX text regions | `<!-- note -->` |
| `punctuation.expression` | JSX expression delimiters | `{` |

## Kotlin

- **Language id**: `SyntaxLanguageId.Kotlin`
- **Aliases**: `kt`, `kts`, `gradle.kts`

| Role | Where emitted | Example            |
|---|---|--------------------|
| `annotation` | Annotations | `@Composable`      |
| `keyword.declaration` | Declaration keywords | `fun`              |
| `keyword.control` | Control-flow keywords | `if`               |
| `keyword.modifier` | Modifiers | `data`             |
| `type` | Uppercase types and classes | `String`           |
| `function.builtin` | Known built-ins | `println`          |
| `function.declaration` | Function declarations | `score`            |
| `function.member` | Dot-call member functions | `padding`          |
| `function` | Calls | `score`            |
| `property` | Dot-access members | `count`            |
| `variable.parameter` | Named arguments in calls | `modifier`         |
| `variable` | Identifiers | `job`              |
| `variable.namespace` | Import and package paths | `java.util.Locale` |
| `escape` | String interpolation delimiters | `${`               |
| `constant.builtin.<value>` | Literals | `true`             |
| `string` | Quoted and triple strings | `"ok"`             |
| `number` | Numeric literals | `1`                |
| `operator` | Arithmetic, comparison, assignment, safe-call, and Elvis operators | `=`, `?.`, `?:`    |
| `punctuation` | Braces, brackets, commas, semicolons | `;`                |
| `comment` | Comments | `// note`          |

## Less

- **Language id**: `SyntaxLanguageId.Less`
- **Aliases**: `less`

| Role | Where emitted | Example |
|---|---|---|
| `variable` | Less variables | `@brand` |
| `keyword.at-rule` | CSS at-rules | `@media` |
| `attribute` | Selectors and mixin selectors | `.rounded` |
| `attribute.pseudo` | Pseudo classes and elements | `hover` |
| `tag` | Type selectors inherited from CSS scanning | `button` |
| `property` | Property names | `border-radius` |
| `function` | CSS function names | `calc` |
| `constant.color` | Hex colors in values | `#38a3ff` |
| `constant.builtin.<value>` | CSS named constants and markers | `transparent` |
| `variable` | Unclassified value identifiers | `sans-serif` |
| `string` | Quoted strings | `"label"` |
| `number` | Numbers and units | `4px` |
| `operator` | Operators in selectors and values | `*` |
| `punctuation` | Braces, brackets, commas, colons, and semicolons | `{` |
| `comment` | Block comments | `/* note */` |

## Lua

- **Language id**: `SyntaxLanguageId.Lua`
- **Aliases**: `lua`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `function` |
| `keyword.control` | Control-flow keywords | `if` |
| `function.builtin` | Built-in callables | `print` |
| `function` | Calls | `insert` |
| `property` | Dot-access members | `enabled` |
| `variable.namespace` | Built-in library tables | `table` |
| `variable` | Local names | `jobs` |
| `string` | Quoted and long-bracket strings | `"scan"` |
| `escape` | String escapes | `\n` |
| `constant.builtin.<value>` | Built-in literals | `nil` |
| `number` | Numeric literals | `4` |
| `operator` | Arithmetic, comparison, assignment, concatenation, and length operators | `..`, `==`, `#` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Line and block comments | `--[[ note ]]` |

## Makefile

- **Language id**: `SyntaxLanguageId.Makefile`
- **Aliases**: `make`, `makefile`, `mk`

| Role | Where emitted | Example |
|---|---|---|
| `function` | Targets and recipe commands | `build` |
| `property` | Assignment names | `OUT` |
| `keyword` | Make directives | `include` |
| `variable` | Make variables | `$(OUT)` |
| `operator` | Assignment operators | `:=` |
| `comment` | Comments | `# note` |

## Markdown

- **Language id**: `SyntaxLanguageId.Markdown`
- **Aliases**: `md`, `markdown`
- **Embedded languages**: any registered language (fenced-code bodies, via fence-language label)

Fence markers and labels keep the Markdown roles listed below.

| Role | Where emitted | Example |
|---|---|---|
| `markup.heading.h1` | Level-1 ATX and setext heading markers | `#`, `===` |
| `markup.heading.h2` | Level-2 ATX and setext heading markers | `##`, `---` |
| `markup.heading.h3` | Level-3 ATX heading markers | `###` |
| `markup.heading.h4` | Level-4 ATX heading markers | `####` |
| `markup.heading.h5` | Level-5 ATX heading markers | `#####` |
| `markup.heading.h6` | Level-6 ATX heading markers | `######` |
| `markup.quote` | Block quote markers | `>` |
| `markup.list` | List markers | `-` |
| `markup.task.checked` | Checked GFM task list markers | `[x]`, `[X]` |
| `markup.task.unchecked` | Unchecked GFM task list markers | `[ ]` |
| `markup.emphasis` | Emphasis delimiters | `**` |
| `markup.strikethrough` | GFM strikethrough delimiters | `~~` |
| `markup.thematic-break` | Thematic break markers | `---` |
| `markup.code` | Inline code delimiters | `` ` `` |
| `markup.link` | Link delimiters and destinations | `[title](url)` |
| `markup.reference` | Link reference labels | `[id]:` |
| `markup.image` | Image marker | `!` |
| `markup.table` | GFM table pipes and delimiter rows | `| --- |` |
| `markup.fence` | Fence delimiters | three backticks |
| `string.language` | Fence language labels | `kotlin` |
| `string.url` | Link reference destinations | `https://example.com` |
| `escape` | Backslash escapes and Markdown entities | `\*`, `&amp;` |

## MDX

- **Language id**: `SyntaxLanguageId.Mdx`
- **Aliases**: `mdx`
- **Embedded languages**: Markdown (prose), JavaScript (ESM `import` / `export` at top level), any registered language (fenced code via Markdown's fence-language routing)

MDX is Markdown with embedded JSX components. Plain prose emits the [Markdown](#markdown) roles; component markup uses the markup roles below.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `className` |
| `attribute.directive` | Component-style directive attributes when present (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:` prefixes) | `v-if` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters | `/>` |
| `comment` | Markup comments inside component regions | `<!-- note -->` |
| `markup.expression` | Component expression delimiters | `{`, `}` |

## Objective-C

- **Language id**: `SyntaxLanguageId.ObjectiveC`
- **Aliases**: `objc`, `objective-c`, `objectivec`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | `@` forms and preprocessor lines | `@interface` |
| `keyword.declaration` | Declaration keywords | `typedef` |
| `keyword` | Other keywords | `return` |
| `type` | Objective-C and C type names | `NSString` |
| `variable` | Parameters and identifiers | `copy` |
| `property` | Dot-access members | `name` |
| `function.builtin` | Known built-in calls | `NSLog` |
| `function` | Calls | `score` |
| `constant.builtin.<value>` | Objective-C and C literals | `nil` |
| `escape` | String escapes and printf format specifiers | `%@` |
| `string` | String literals | `@"text"` |
| `number` | Numeric literals | `10` |
| `operator` | Arithmetic, comparison, assignment, and pointer operators | `=`, `+`, `->` |
| `punctuation` | Braces, brackets, commas, semicolons, selector colons | `;` |
| `comment` | Comments | `// note` |

## Perl

- **Language id**: `SyntaxLanguageId.Perl`
- **Aliases**: `pl`, `perl`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `sub` |
| `keyword.control` | Control-flow keywords | `if` |
| `function.builtin` | Built-in callables | `print` |
| `function` | Calls and definitions | `score` |
| `variable.parameter` | Sigil variables | `$name` |
| `variable.namespace` | Package and module paths | `Demo` |
| `string.regex` | Regex literals | `/needle/` |
| `escape` | Interpolated variables and format specifiers inside strings | `%02d` |
| `string` | Strings, heredocs, and quote-like operators | `"ok"` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, and Perl-specific operators | `=`, `=~`, `//` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Bareword identifiers | `count` |
| `comment` | Comments | `# note` |

## PHP

- **Language id**: `SyntaxLanguageId.Php`
- **Aliases**: `php`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `function` |
| `keyword.control` | Control-flow keywords | `if` |
| `type` | Type names | `string` |
| `function.builtin` | Built-in callables | `strlen` |
| `function` | Calls and definitions | `score` |
| `variable.parameter` | Dollar variables | `$name` |
| `property` | Member access | `count` |
| `annotation` | PHP 8 attributes | `#[Route]` |
| `escape` | Interpolated variables and format specifiers inside strings | `%02d` |
| `string` | Strings, heredocs, and nowdocs | `"ok"` |
| `constant.builtin.<value>` | Literals | `null` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, concatenation, and null-safe operators | `=`, `.`, `??`, `?->` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Bareword identifiers and class-constant names | `Status` |
| `comment` | Comments | `// note` |

## PostgreSQL

- **Language id**: `SyntaxLanguageId.Postgresql`
- **Aliases**: `pgsql`, `postgres`, `postgresql`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | SQL and PostgreSQL keywords | `RETURNING` |
| `type` | SQL and PostgreSQL type names | `jsonb` |
| `function.builtin` | Built-in functions | `now` |
| `function` | Calls | `calculate_score` |
| `property` | Identifier-after-`.` (column/table member access) | `id` |
| `property.quoted` | Double-quoted identifiers | `"Title"` |
| `variable` | Unclassified identifiers | `users` |
| `string` | Single-quoted and dollar-quoted strings | `$body$...$body$` |
| `constant.builtin.<value>` | Boolean, null, and date/time literals | `true` |
| `number` | Numeric literals | `1` |
| `operator` | Casts, JSON operators, and arithmetic/comparison operators | `::`, `->>`, `=` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Line and block comments | `-- note` |

## PowerShell

- **Language id**: `SyntaxLanguageId.PowerShell`
- **Aliases**: `ps`, `ps1`, `powershell`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.control` | PowerShell keywords | `function` |
| `function.cmdlet` | Verb-Noun cmdlets at statement position | `Write-Host` |
| `function` | Call-like identifiers | `Invoke` |
| `variable.parameter` | Dollar variables and scoped variables | `$env:USER` |
| `variable.splatting` | Splatting variables | `@Options` |
| `constant.builtin.<value>` | Built-in constants | `$true` |
| `string` | Single, double, and here-strings | `@"... "@` |
| `escape` | Backtick escapes and subexpression delimiters | `` `n `` |
| `operator` | Dash operators and symbolic operators | `-like` |
| `number` | Numeric literals | `1` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Line and block comments | `<# note #>` |

## Protobuf

- **Language id**: `SyntaxLanguageId.Protobuf`
- **Aliases**: `proto`, `protobuf`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `message` |
| `keyword` | Other Protobuf keywords | `syntax` |
| `type` | Scalar types and uppercase names | `string` |
| `function` | Call-like identifiers | `Get` |
| `property` | Field names | `title` |
| `constant` | Enum members | `ACTIVE` |
| `string` | Quoted strings | `"proto3"` |
| `number` | Field numbers | `1` |
| `operator` | `=`, comparison, and arithmetic operators | `=` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `variable` | Unclassified identifiers | `foo` |
| `comment` | Line and block comments | `// note` |

## Python

- **Language id**: `SyntaxLanguageId.Python`
- **Aliases**: `py`, `python`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `def` |
| `keyword.control` | Control-flow keywords | `if` |
| `type` | Built-in types and uppercase class-like names | `Job` |
| `function.builtin` | Built-in callables | `len` |
| `function` | Calls and definitions | `score` |
| `property` | Dot-access members | `name` |
| `annotation` | Line-start decorators | `@dataclass` |
| `variable` | Identifiers | `job` |
| `escape` | F-string delimiters and percent format specifiers | `{` |
| `string` | Strings, f-strings, prefixed triples, and docstrings | `"""Doc"""` |
| `constant.builtin.<value>` | Built-in constants | `None` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, walrus, and matrix-multiply operators | `=`, `:=`, `**`, `@` |
| `punctuation` | Braces, brackets, commas, colons | `:` |
| `comment` | Comments | `# note` |

## R

- **Language id**: `SyntaxLanguageId.R`
- **Aliases**: `r`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.control` | Control-flow keywords | `if` |
| `function.builtin` | Built-in callables | `sum` |
| `function` | Calls | `function` |
| `property` | `$` member access | `count` |
| `variable` | Identifiers | `score` |
| `operator` | Assignment, formula, and namespace operators | `<-`, `::` |
| `string` | String literals | `"ok"` |
| `escape` | String escapes | `\n` |
| `constant.builtin.<value>` | Built-in constants | `TRUE` |
| `number` | Numeric literals | `1` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `# note` |

## Ruby

- **Language id**: `SyntaxLanguageId.Ruby`
- **Aliases**: `rb`, `ruby`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `if` |
| `type` | Built-in classes and uppercase constants | `Job` |
| `function.builtin` | Built-in callables | `puts` |
| `function` | Calls and definitions | `score` |
| `variable.parameter` | Sigil variables | `@name` |
| `variable` | Plain identifiers | `count` |
| `escape` | Interpolation delimiters | `#{` |
| `string` | Strings, heredocs, and percent literals | `"hello"` |
| `constant.builtin.<value>` | Literals | `true` |
| `number` | Numeric literals | `0` |
| `operator` | Arithmetic, comparison, assignment, and safe-navigation operators | `=`, `<=>`, `&.` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `# note` |

## Rust

- **Language id**: `SyntaxLanguageId.Rust`
- **Aliases**: `rs`, `rust`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `fn` |
| `keyword.control` | Control-flow keywords | `match` |
| `keyword.modifier` | Modifiers | `pub` |
| `type` | Type keywords and uppercase names | `i32` |
| `annotation` | Hash-bracket attributes | `#[derive(Debug)]` |
| `function.macro` | Macro calls | `println!` |
| `function` | Calls and definitions | `score` |
| `property` | Dot-access members and enum entries | `count` |
| `variable` | Lifetimes and labels | `'a` |
| `constant.builtin.<value>` | Literals | `true` |
| `escape` | Format placeholders inside strings | `{}` |
| `string` | Quoted and raw strings | `"ok"` |
| `number` | Numeric literals | `1_u32` |
| `operator` | Arithmetic, comparison, assignment, range, and reference operators | `=`, `..`, `&`, `?` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## Scala

- **Language id**: `SyntaxLanguageId.Scala`
- **Aliases**: `scala`, `sc`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | Declaration keywords | `class` |
| `keyword.control` | Control-flow keywords | `if` |
| `keyword.modifier` | Modifiers | `final` |
| `type` | Type names and known standard types | `String` |
| `function.builtin` | Built-in callables | `println` |
| `function` | Calls and constructors | `score` |
| `property` | Dot-access members | `enabled` |
| `annotation` | Annotations | `@deprecated` |
| `variable` | Identifiers, including backtick identifiers | `` `reading label` `` |
| `constant` | Symbol literals | `'name` |
| `constant.builtin.<value>` | Literals | `true` |
| `escape` | Interpolation delimiters | `${` |
| `string` | Plain, interpolated, raw, and triple strings | `"ok"` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, and symbolic operators | `=`, `=>`, `|>` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## SCSS

- **Language id**: `SyntaxLanguageId.Scss`
- **Aliases**: `scss`, `sass`

| Role | Where emitted | Example |
|---|---|---|
| `variable` | SCSS variables | `$primary` |
| `keyword.at-rule` | SCSS directives | `@mixin` |
| `keyword.at-rule` | CSS at-rules | `@media` |
| `tag` | Type selectors inherited from CSS scanning | `button` |
| `attribute` | Class/id selectors and attributes | `.card` |
| `attribute.parent-selector` | Parent selector | `&` |
| `attribute.pseudo` | Nested pseudo selectors | `hover` |
| `property` | Property names | `color` |
| `function` | Mixin/function calls by call shape | `button` |
| `constant.color` | Hex colors in values | `#38a3ff` |
| `constant.builtin.<value>` | CSS named constants and markers | `transparent` |
| `variable` | Unclassified value identifiers | `sans-serif` |
| `string` | Quoted strings | `"icon"` |
| `number` | Numbers and units | `12px` |
| `operator` | Operators in selectors and values | `*` |
| `punctuation` | Braces, brackets, commas, colons, and semicolons | `{` |
| `escape` | Interpolation delimiters | `#{` |
| `comment` | Block comments | `/* note */` |

## Shell

- **Language id**: `SyntaxLanguageId.Shell`
- **Aliases**: `sh`, `bash`, `zsh`, `shell`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.control` | Shell control keywords | `if` |
| `function.builtin` | Built-in commands | `echo` |
| `function` | Command-like words in tests and recipes | `grep` |
| `variable.parameter` | Dollar variables | `$HOME` |
| `escape` | String escapes, substitutions, and braced expansion delimiters | `${` |
| `variable` | Expansion contents and bare words | `default` |
| `string` | Quoted strings and heredoc bodies | `"home"` |
| `number` | Numeric literals | `1` |
| `operator` | Shell operators and flags | `-` |
| `punctuation` | Braces, brackets, parens, semicolons | `;` |
| `comment` | Comments | `# note` |

## SQL

- **Language id**: `SyntaxLanguageId.Sql`
- **Aliases**: `sql`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | SQL keywords | `SELECT` |
| `type` | SQL type names | `integer` |
| `function.builtin` | Built-in functions | `count` |
| `function` | Calls | `calculate_score` |
| `property` | Identifier-after-`.` (column/table member access) | `name` |
| `property.quoted` | Quoted identifiers | `"Title"` |
| `variable` | Unclassified identifiers | `users` |
| `variable.parameter` | Bind placeholders | `?` |
| `string` | Single-quoted strings | `'note'` |
| `number` | Numeric literals | `10` |
| `constant.builtin.<value>` | Boolean and null values | `null` |
| `operator` | Arithmetic, comparison, assignment, and concatenation operators | `=`, `||`, `<>` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Line and block comments | `-- note` |

## SQLite

- **Language id**: `SyntaxLanguageId.Sqlite`
- **Aliases**: `sqlite`, `sqlite3`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | SQL and SQLite keywords | `AUTOINCREMENT` |
| `type` | SQL and SQLite type names | `INTEGER` |
| `function.builtin` | Built-in functions | `json_extract` |
| `function` | Calls | `calculate_score` |
| `property` | Identifier-after-`.` (column/table member access) | `name` |
| `property.quoted` | Double-quoted, bracket-quoted, and backtick-quoted identifiers | `[note items]` |
| `variable` | Unclassified identifiers | `users` |
| `variable.parameter` | SQLite bind parameters | `:title` |
| `string` | Single-quoted strings and blob literals | `X'53514C'` |
| `number` | Numeric literals | `10` |
| `constant.builtin.<value>` | Boolean, null, and current date/time values | `current_timestamp` |
| `operator` | Arithmetic, comparison, assignment, and concatenation operators | `=`, `||`, `<>` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Line and block comments | `-- note` |

## Svelte

- **Language id**: `SyntaxLanguageId.Svelte`
- **Aliases**: `svelte`
- **Embedded languages**: JavaScript / TypeScript (`<script>` based on `lang`), CSS / SCSS / Less (`<style>` based on `lang`)

Svelte components mix component markup with directive attributes, control blocks (`{#if}`, `{#each}`), and reactive labels.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `class` |
| `attribute.directive` | Component-style directive attributes (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:` prefixes) | `on:click` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters | `/>` |
| `comment` | Markup comments | `<!-- note -->` |
| `markup.expression` | Plain expression delimiters | `{`, `}` |
| `keyword` | Svelte block markers (`#`, `/`, `:` first chars inside `{…}`) | `{#if`, `{:else`, `{/each` |
| `keyword` | Svelte reactive label prefix in `<script>` bodies | `$:` |

## Swift

- **Language id**: `SyntaxLanguageId.Swift`
- **Aliases**: `swift`

| Role | Where emitted | Example |
|---|---|---|
| `annotation` | Attributes and hash directives | `@MainActor`, `#Preview` |
| `keyword.declaration` | Declaration keywords | `func` |
| `keyword.control` | Control-flow keywords | `guard` |
| `keyword.modifier` | Modifiers | `private` |
| `keyword.wildcard` | Wildcard parameter marker | `_` |
| `type` | Uppercase types and classes | `String` |
| `function.builtin` | Known built-ins | `print` |
| `function` | Calls and declarations | `score` |
| `property` | Dot-access members | `name` |
| `variable` | Identifiers and projected-value variables | `job`, `$note` |
| `variable.namespace` | Import paths | `Foundation` |
| `escape` | String interpolation delimiters | `\(` |
| `constant.builtin.<value>` | Literals | `true` |
| `string` | Quoted, multiline, and raw strings | `"ok"` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, optional, and nil-coalescing operators | `=`, `?.`, `??` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## Terraform

- **Language id**: `SyntaxLanguageId.Terraform`
- **Aliases**: `terraform`, `tf`

| Role | Where emitted | Example |
|---|---|---|
| `keyword` | Terraform block declarations | `resource` |
| `type` | Block names | `local_file` |
| `property` | Attribute names | `filename` |
| `function` | Function calls in expressions | `jsonencode` |
| `string` | Quoted strings | `"note.md"` |
| `escape` | Interpolation delimiters | `${`, `}` |
| `constant.builtin.<value>` | Boolean and null values | `true` |
| `number` | Numeric literals | `1` |
| `operator` | Assignment and arithmetic operators | `=`, `+` |
| `punctuation` | Braces, brackets, commas | `{` |
| `variable` | Unclassified identifiers (block labels, references) | `web` |
| `comment` | Comments | `# note` |

## TOML

- **Language id**: `SyntaxLanguageId.Toml`
- **Aliases**: `toml`

| Role | Where emitted | Example |
|---|---|---|
| `property.name` | Keys and dotted keys | `versions.compose` |
| `property.section` | Table headers | `[versions]` |
| `string` | Basic, literal, and multiline strings | `"1.0"` |
| `escape` | String escapes | `\n` |
| `number` | Numbers and datetime-like values | `2026-05-20` |
| `constant.builtin.<value>` | Boolean values | `false` |
| `operator` | Key/value `=` separator | `=` |
| `punctuation` | Brackets, braces, commas | `[` |
| `comment` | Comments | `# note` |

## TSX

- **Language id**: `SyntaxLanguageId.Tsx`
- **Aliases**: `tsx`
- **Embedded languages**: TypeScript (top-level code and curly-brace expression islands)

TSX is the TypeScript counterpart to [JSX](#jsx). Top-level code emits TypeScript roles (see [TypeScript](#typescript)); component tags switch to the markup roles below.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `className` |
| `attribute.directive` | Component-style directive attributes when present (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:` prefixes) | `on:click` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value; operators inside type arguments (`:`, `\|`, `&`, `=`) | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters; type-argument delimiters and separators | `/>` |
| `comment` | Markup comments in TSX text regions | `<!-- note -->` |
| `punctuation.expression` | TSX expression delimiters | `{` |
| `type` | Uppercase identifiers inside type arguments | `User` |
| `variable` | Lowercase identifiers inside type arguments | `id` |

## TypeScript

- **Language id**: `SyntaxLanguageId.TypeScript`
- **Aliases**: `ts`, `typescript`

| Role | Where emitted | Example |
|---|---|---|
| `keyword.declaration` | JS and TS declaration keywords | `interface` |
| `keyword.control` | Control-flow keywords | `if` |
| `keyword.modifier` | Modifiers | `readonly` |
| `type` | Uppercase type names and known global constructor types | `Job` |
| `variable.namespace` | Known global namespaces | `JSON` |
| `function` | Calls and declarations | `score` |
| `property` | Dot-access members | `enabled` |
| `variable` | Identifiers and lowercase type names | `string` |
| `string.regex` | Regex literals | `/needle/` |
| `escape` | Template-string interpolation delimiters | `${` |
| `string` | Strings and template strings | `"ok"` |
| `constant.builtin.<value>` | Literals | `undefined` |
| `number` | Numeric literals | `1` |
| `operator` | Arithmetic, comparison, assignment, optional-chaining, nullish-coalescing, spread, and non-null assertion operators | `===`, `?.`, `??`, `...`, `!` |
| `punctuation` | Braces, brackets, commas, semicolons | `;` |
| `comment` | Comments | `// note` |

## Vue

- **Language id**: `SyntaxLanguageId.Vue`
- **Aliases**: `vue`
- **Embedded languages**: JavaScript / TypeScript (`<script>` based on `lang`), CSS / SCSS / Less (`<style>` based on `lang`)

Vue single-file components combine template markup, embedded `<script>` blocks, and embedded `<style>` blocks. Template directives (`v-…`, `@…`) emit `attribute.directive`; Mustache expressions (`{{ … }}`) emit `markup.expression`.

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Component and element names | `Chart` |
| `attribute` | Non-directive attribute names | `class` |
| `attribute.directive` | Component directive attributes (`@`, `:`, `#`, `v-`, `on:`, `bind:`, `class:`, `use:`, `transition:` prefixes) | `@click`, `v-if` |
| `string` | Attribute values | `"Daily"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Markup entities in text and attributes | `&amp;` |
| `punctuation` | Tag delimiters | `/>` |
| `comment` | Markup comments | `<!-- note -->` |
| `markup.expression` | Mustache (`{{ … }}`) and in-tag (`{ … }`) expression delimiters | `{{`, `}}` |

## XML

- **Language id**: `SyntaxLanguageId.Xml`
- **Aliases**: `xml`, `svg`

| Role | Where emitted | Example |
|---|---|---|
| `tag` | Element names | `vector` |
| `attribute` | Attribute names | `android:width` |
| `string` | Attribute values, CDATA bodies | `"24dp"` |
| `operator` | `=` between attribute name and value | `=` |
| `escape` | Named and numeric entities | `&amp;` |
| `punctuation` | Tag delimiters | `</` |
| `markup.cdata` | CDATA section markers | `<![CDATA[`, `]]>` |
| `comment` | XML comments | `<!-- note -->` |
| `annotation` | Processing instructions | `<?xml version="1.0"?>` |

## YAML

- **Language id**: `SyntaxLanguageId.Yaml`
- **Aliases**: `yml`, `yaml`

| Role | Where emitted | Example |
|---|---|---|
| `property.name` | Mapping keys | `name` |
| `string` | Quoted strings and plain scalar text | `"value"` |
| `string.yaml.block-indicator` | Block scalar indicators | `|-` |
| `number` | Numeric plain scalars | `3` |
| `constant.builtin.<value>` | Boolean and null-like values | `true` |
| `variable.yaml.anchor` | Anchors | `&defaults` |
| `variable` | Plain identifier-shaped scalars | `default` |
| `variable.yaml.alias` | Aliases | `*defaults` |
| `type` | Tags | `!Config` |
| `punctuation` | Mapping and list markers | `:` |
| `comment` | Comments | `# note` |
