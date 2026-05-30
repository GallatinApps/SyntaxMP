# Shared Scanner Coverage Matrix

This matrix records the behavior baseline and ownership map for SyntaxMP's shared scanners. Shared scanner mechanics live under `engine/scanners/`, language-owned scanners under `languages/<language>/`, and language vocabulary inside per-language `*Lexicon.kt` files; tokenizers own scanner options and direct scanner inputs. See [`AGENTS.md`](../../AGENTS.md) for the authoritative package-ownership rules.

Validation command:

```bash
./gradlew :syntaxmp:jvmTest :syntaxmp:compileKotlinJvm
```

Target no-language-name scanner grep:

```bash
rg -n "Kotlin|Scala|Swift|CSharp|Rust|Cpp|Groovy|Dart|Ruby|Perl|Php|Python|Scss|SCSS|Less|CssDialect|Vue|Svelte|Astro|Jsx|Tsx|Mdx|ComponentMarkup" \
  syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/engine/scanners/clike/CLikeScanner.kt \
  syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/engine/scanners/script/ScriptLikeScanner.kt \
  syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/engine/scanners/markup/MarkupScanner.kt
```

Language names remain valid in language tokenizer files, language lexicon files, fixture file names, docs, and language-owned role scope strings.

## Shared Scanner Rule

Shared scanners may reference generic syntax concepts and reusable lexical shapes. Shared scanners may not reference consuming language names. Language tokenizers own scanner options, direct scanner inputs, and language-specific roles. Language lexicons own keywords, constants, builtins, type names, directives, at-rules, raw-text tag names, embedded-language aliases, and role-specific word or prefix sets. `engine/scanners/markup/` is the single shared markup scanner home; markup raw-text label resolution lives there too.

## C-Like Scanner

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| C | `CLikeScanner` | `//` and `/* */` comments, preprocessor lines, quoted strings, C printf formats, C numeric suffixes. | Covered by `CFixtureTest.kt`. | C tokenizer owns preprocessor options, C number lexer, and printf format rules; C lexicon owns vocabulary. |
| C++ | `CLikeScanner` | C-style comments, preprocessor lines, quoted strings, C/C++ printf formats, raw strings, prefixed strings, numeric suffixes. | Covered by `CppFixtureTest.kt`. | C++ tokenizer owns raw/prefixed string rules, preprocessor options, number lexer, and format rules. |
| Objective-C | `CLikeScanner` | C-style comments, preprocessor lines, `@` annotations, quoted strings, format specifiers, Objective-C type/builtin names. | Covered by `ObjectiveCFixtureTest.kt`. | Objective-C tokenizer owns annotation/preprocessor options and format rules; Objective-C lexicon owns vocabulary. |
| Java | `CLikeScanner` | C-style comments, `@` annotations, quoted strings, Java numeric suffixes and separators. | Covered by `JavaFixtureTest.kt`. | Java tokenizer owns annotation options and Java number lexer; Java lexicon owns vocabulary. |
| Kotlin | `CLikeScanner` | C-style comments, `@` annotations, import directive paths, quoted/triple strings, `$name` and `${...}` interpolation. | Covered by `KotlinFixtureTest.kt`, including exact string/interpolation alternation. | Kotlin tokenizer owns annotation/import identifier options plus interpolation-capable string rules. |
| Swift | `CLikeScanner` | C-style comments, `@` annotations, quoted/multiline strings, repeated-hash raw strings, interpolation, wildcard `_`, uppercase call-as-type heuristic. | Covered by `SwiftFixtureTest.kt`. | Swift tokenizer owns annotation/identifier options, raw string rules, interpolation rules, and number lexer. |
| C# | `CLikeScanner` | C-style comments, `@` annotations, interpolated strings, verbatim strings, conservative raw string interpolation, numeric suffixes. | Covered by `CSharpFixtureTest.kt`. | C# tokenizer owns annotation options, verbatim/interpolated/raw string rules, and number lexer. |
| JavaScript | `CLikeScanner` | C-style comments, quoted/backtick strings, template interpolation, slash regex literals, JavaScript numbers and BigInt. | Covered by `JavaScriptFixtureTest.kt`. | JavaScript tokenizer owns template string rules, regex options, and number lexer. |
| TypeScript | `CLikeScanner` | JavaScript forms plus TypeScript keyword/type lexicon. | Covered by `TypeScriptFixtureTest.kt`. | TypeScript tokenizer owns the JavaScript-like literal and regex options; TypeScript lexicon owns vocabulary. |
| Dart | `CLikeScanner` | C-style comments, `@` annotations, quoted/triple strings, raw strings, interpolation in single and double quotes. | Covered by `DartFixtureTest.kt`. | Dart tokenizer owns annotation options and raw/triple/interpolated string rules. |
| Go | `CLikeScanner` | C-style comments, quoted/backtick strings, printf formats, Go numeric forms and imaginary suffixes. | Covered by `GoFixtureTest.kt`. | Go tokenizer owns backtick string rules, number lexer, and format rules. |
| Rust | `CLikeScanner` | C-style comments, quoted strings, raw and byte raw strings, macro suffix `!`, brace format specifiers, numeric suffixes. | Covered by `RustFixtureTest.kt`. | Rust tokenizer owns identifier macro options, raw string rules, number lexer, and brace format rules. |
| Scala | `CLikeScanner` | C-style comments, `@` annotations, backtick identifiers/strings, triple strings, interpolators, `$name`/`${...}` interpolation, format specifiers. | Covered by `ScalaFixtureTest.kt`. | Scala tokenizer owns annotation options, interpolator rules, backtick/triple string rules, and format rules. |
| Groovy | `CLikeScanner` | C-style comments, `@` annotations, quoted/triple strings, slashy and dollar-slashy strings, interpolation. | Covered by `GroovyFixtureTest.kt`. | Groovy tokenizer owns annotation options and slashy/dollar-slashy/interpolated string rules. |
| GLSL | `CLikeScanner` | C-style comments, preprocessor lines, quoted strings, numeric suffixes and type/builtin lexicon. | Covered by `GlslFixtureTest.kt`. | GLSL tokenizer owns preprocessor options and number lexer; GLSL lexicon owns vocabulary. |
| Protobuf | `CLikeScanner` | C-style comments, quoted strings, keyword/type lexicon, hex numbers. | Covered by `ProtobufFixtureTest.kt`. | Protobuf tokenizer owns number lexer over shared C-like defaults; Protobuf lexicon owns vocabulary. |

## Script-Like Scanner

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| Python | `ScriptLikeScanner` | Hash comments, quoted/triple strings, raw/f/b/u prefixes, f-string interpolation, percent formats, constants and builtins. | Covered by `PythonFixtureTest.kt`. | Python tokenizer owns prefixed/triple string rules, f-string interpolation, and percent formats; Python lexicon owns vocabulary. |
| Ruby | `ScriptLikeScanner` | Hash comments, quoted strings, `#{...}` interpolation, heredocs, percent literals, sigil variables, `?`/`!` suffixes. | Covered by `RubyFixtureTest.kt`, including exact string/interpolation alternation and operator-shadowing checks. | Ruby tokenizer owns heredoc, percent literal, interpolation, sigil, and suffix options; Ruby lexicon owns vocabulary. |
| PHP | `ScriptLikeScanner` | Hash and `//` comments, C-style block comments, `$` variables, dollar interpolation, heredoc/nowdoc, percent formats. | Covered by `PhpFixtureTest.kt`. | PHP tokenizer owns comment options, heredoc rules, dollar interpolation, and sigil options; PHP lexicon owns vocabulary. |
| Perl | `ScriptLikeScanner` | Hash comments, `$`/`@`/`%` variables, slash regex literals, dollar interpolation, heredocs, quote-like operators, percent formats. | Covered by `PerlFixtureTest.kt`, including non-slash delimiters and operator-shadowing checks. | Perl tokenizer owns heredoc, quote-like operator, regex, interpolation, and sigil options; Perl lexicon owns vocabulary. |

## CSS-Family Scanner

`CssScanner` is primary-language-owned under `languages/css/`. SCSS and Less import that CSS scanner directly while keeping their tokenizer options and lexicon data in their own language packages.

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| CSS | `CssScanner` | Block comments, strings with escapes, at-rules, selectors, properties, custom properties, pseudo selectors, important constants, values, colors, units. | Covered by `CssFixtureTest.kt`. | CSS tokenizer owns mechanics; CSS lexicon owns standard at-rules and named constants. |
| SCSS | `CssScanner` | CSS forms plus `$variables`, preprocessor at-keywords, `&` parent selector, `#{...}` interpolation in selectors, values, and strings. | Covered by `ScssFixtureTest.kt`. | SCSS tokenizer owns variable, interpolation, and parent-selector options; SCSS lexicon owns preprocessor keywords. |
| Less | `CssScanner` | CSS forms plus unknown `@name` variables while known CSS at-rules stay keywords. | Covered by `LessFixtureTest.kt`. | Less tokenizer owns the unknown-at-rule variable options; Less lexicon re-exports CSS at-rules and named constants. |

## Primary-Language Family Scanners

`SqlScanner` and `HclScanner` are primary-language-owned under `languages/sql/` and `languages/hcl/`. SQLite, PostgreSQL, and Terraform import those scanners directly, but they build their own scanner inputs from their own lexicons instead of copying a standard base options object.

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| SQL | `SqlScanner` | Line/block comments, quoted strings, quoted identifiers, numbers, SQL identifiers, keywords, types, constants, and built-in functions. | Covered by `SqlFixtureTest.kt`. | SQL tokenizer passes `SqlKeywordRoles`, `SqlConstants`, `SqlTypeKeywords`, and `SqlBuiltinRoles` directly; `SqlScannerOptions.Standard` covers default mechanics. |
| SQLite | `SqlScanner` | SQL forms plus SQLite keywords, bracket/backtick identifiers, SQLite bind parameters, and blob literals. | Covered by `SQLiteFixtureTest.kt`. | SQLite lexicon composes SQL vocabulary vals with SQLite words; SQLite tokenizer enables SQLite scanner options. |
| PostgreSQL | `SqlScanner` | SQL forms plus PostgreSQL keywords and dollar-quoted strings. | Covered by `PostgresqlFixtureTest.kt`. | PostgreSQL lexicon composes SQL vocabulary vals with PostgreSQL words; PostgreSQL tokenizer only enables dollar-quoted strings. |
| HCL | `HclScanner` | Comments, strings, interpolation markers, numbers, properties, block types, constants, and function calls. | Covered by `HclFixtureTest.kt`. | HCL tokenizer passes `HclConstants` directly; no HCL scanner options object exists. |
| Terraform | `HclScanner` | HCL forms plus Terraform declaration block keywords. | Covered by `TerraformFixtureTest.kt`. | Terraform lexicon composes HCL constants with Terraform declaration roles; Terraform tokenizer passes those scanner inputs directly. |

## Markup-Family Scanner

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| HTML | `MarkupScanner` | Tags, attributes, comments, declarations, CDATA, entities, and script/style raw-text embedded-language routing with `lang=` awareness. | Covered by `HtmlFixtureTest.kt`. | HTML tokenizer owns `HtmlRawTextTags`, chooses `MarkupScannerOptions.defaultRawTextLanguageForTag`, and preserves lowercase tag matching. |
| XML | `MarkupScanner` | XML-style tags, attributes, comments, declarations, processing instructions, CDATA, and entities. | Covered by `XmlFixtureTest.kt`. | XML tokenizer preserves tag-name case and leaves raw-text routing disabled through empty raw-text tags and the default no-op raw-text language function. |
| JSX | `MarkupScanner` | Markup comments/declarations, tags, attributes, entities inside markup, brace expressions inside markup, and JavaScript top-level expression routing. | Covered by `JsxFixtureTest.kt`. | JSX tokenizer owns expression language, starts-in-script mode, directive attributes, brace expression rules, raw-text language function choice, and `JsxRawTextTags`. |
| TSX | `MarkupScanner` | JSX forms plus TypeScript top-level expression routing outside markup. | Covered by `TsxFixtureTest.kt`. | TSX tokenizer owns TypeScript expression language, directive attributes, brace expression rules, raw-text language function choice, and `TsxRawTextTags`. |
| MDX | `MarkdownScanner` + `MarkupScanner` | Partitioned Markdown, ESM lines, JSX-style tag ranges, top-level expression ranges, fenced-code embedded languages, and embedded HTML inside Markdown ranges. | Covered by `MdxFixtureTest.kt`. | MDX tokenizer owns range partitioning, ESM keyword lexicon, brace/directive mechanics, raw-text language function choice, and `MdxRawTextTags`. |
| Vue | `MarkupScanner` | Markup tags, directive-like attributes, quoted attribute values, text `{{...}}` expressions, and script/style raw-text routing by `lang`. | Covered by `VueFixtureTest.kt`. | Vue tokenizer owns double-mustache, tag-expression, directive mechanics, raw-text language function choice, and `VueRawTextTags`. |
| Svelte | `MarkupScanner` | Markup tags, directive-like attributes, brace expressions, block markers, and script/style raw-text routing by `lang`. | Covered by `SvelteFixtureTest.kt`. | Svelte tokenizer owns brace expression mechanics, block markers, directive mechanics, raw-text language function choice, and `SvelteRawTextTags`. |
| Astro | `MarkupScanner` | Front matter routed as TypeScript, markup tags, brace expressions, and script/style raw-text routing by `lang`. | Covered by `AstroFixtureTest.kt`. | Astro tokenizer owns frontmatter language, brace expression mechanics, directive mechanics, raw-text language function choice, and `AstroRawTextTags`. |
