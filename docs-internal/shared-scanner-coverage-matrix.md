# Shared Scanner Coverage Matrix

This matrix records the behavior baseline and ownership map for SyntaxMP's shared scanners. Shared scanner mechanics live under `engine/scanners/`, language-owned scanners under `languages/<language>/`, and language vocabulary inside per-language `*Lexicon.kt` files; tokenizers own scanner options and direct scanner inputs. See [`AGENTS.md`](../AGENTS.md) for the authoritative package-ownership rules.

Validation command:

```bash
./gradlew :syntaxmp:jvmTest :syntaxmp:compileKotlinJvm
```

Target no-language-name scanner grep:

```bash
rg -n "Kotlin|Swift|CSharp|Rust|Cpp|Dart|Ruby|Php|Python|Jsx|Tsx|ComponentMarkup" \
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
| Java | `CLikeScanner` | C-style comments, `@` annotations, quoted strings, Java numeric suffixes and separators. | Covered by `JavaFixtureTest.kt`. | Java tokenizer owns annotation options and Java number lexer; Java lexicon owns vocabulary. |
| Kotlin | `CLikeScanner` | C-style comments, `@` annotations, import directive paths, quoted/triple strings, `$name` and `${...}` interpolation. | Covered by `KotlinFixtureTest.kt`, including exact string/interpolation alternation. | Kotlin tokenizer owns annotation/import identifier options plus interpolation-capable string rules. |
| Swift | `CLikeScanner` | C-style comments, `@` annotations, quoted/multiline strings, repeated-hash raw strings, interpolation, wildcard `_`, uppercase call-as-type heuristic. | Covered by `SwiftFixtureTest.kt`. | Swift tokenizer owns annotation/identifier options, raw string rules, interpolation rules, and number lexer. |
| C# | `CLikeScanner` | C-style comments, `@` annotations, interpolated strings, verbatim strings, conservative raw string interpolation, numeric suffixes. | Covered by `CSharpFixtureTest.kt`. | C# tokenizer owns annotation options, verbatim/interpolated/raw string rules, and number lexer. |
| JavaScript | `CLikeScanner` | C-style comments, quoted/backtick strings, template interpolation, slash regex literals, JavaScript numbers and BigInt. | Covered by `JavaScriptFixtureTest.kt`. | JavaScript tokenizer owns template string rules, regex options, and number lexer. |
| TypeScript | `CLikeScanner` | JavaScript forms plus TypeScript keyword/type lexicon. | Covered by `TypeScriptFixtureTest.kt`. | TypeScript tokenizer owns the JavaScript-like literal and regex options; TypeScript lexicon owns vocabulary. |
| Dart | `CLikeScanner` | C-style comments, `@` annotations, quoted/triple strings, raw strings, interpolation in single and double quotes. | Covered by `DartFixtureTest.kt`. | Dart tokenizer owns annotation options and raw/triple/interpolated string rules. |
| Go | `CLikeScanner` | C-style comments, quoted/backtick strings, printf formats, Go numeric forms and imaginary suffixes. | Covered by `GoFixtureTest.kt`. | Go tokenizer owns backtick string rules, number lexer, and format rules. |
| Rust | `CLikeScanner` | C-style comments, quoted strings, raw and byte raw strings, macro suffix `!`, brace format specifiers, numeric suffixes. | Covered by `RustFixtureTest.kt`. | Rust tokenizer owns identifier macro options, raw string rules, number lexer, and brace format rules. |
| Protobuf | `CLikeScanner` | C-style comments, quoted strings, keyword/type lexicon, hex numbers. | Covered by `ProtobufFixtureTest.kt`. | Protobuf tokenizer owns number lexer over shared C-like defaults; Protobuf lexicon owns vocabulary. |

## Script-Like Scanner

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| Python | `ScriptLikeScanner` | Hash comments, quoted/triple strings, raw/f/b/u prefixes, f-string interpolation, percent formats, constants and builtins. | Covered by `PythonFixtureTest.kt`. | Python tokenizer owns prefixed/triple string rules, f-string interpolation, and percent formats; Python lexicon owns vocabulary. |
| Ruby | `ScriptLikeScanner` | Hash comments, quoted strings, `#{...}` interpolation, heredocs, percent literals, sigil variables, `?`/`!` suffixes. | Covered by `RubyFixtureTest.kt`, including exact string/interpolation alternation and operator-shadowing checks. | Ruby tokenizer owns heredoc, percent literal, interpolation, sigil, and suffix options; Ruby lexicon owns vocabulary. |
| PHP | `ScriptLikeScanner` | Hash and `//` comments, C-style block comments, `$` variables, dollar interpolation, heredoc/nowdoc, percent formats. | Covered by `PhpFixtureTest.kt`. | PHP tokenizer owns comment options, heredoc rules, dollar interpolation, and sigil options; PHP lexicon owns vocabulary. |

## CSS-Family Scanner

`CssScanner` is primary-language-owned under `languages/css/`.

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| CSS | `CssScanner` | Block comments, strings with escapes, at-rules, selectors, properties, custom properties, pseudo selectors, important constants, values, colors, units. | Covered by `CssFixtureTest.kt`. | CSS tokenizer owns mechanics; CSS lexicon owns standard at-rules and named constants. |

## Primary-Language Family Scanners

`SqlScanner` is primary-language-owned under `languages/sql/`. SQLite and PostgreSQL import it directly, but build their own scanner inputs from their own lexicons. `IniScanner` and `ShellScanner` are primary-language-owned under `languages/ini/` and `languages/shell/`; separate public identities can reuse those scanner mechanics from their own tokenizer packages.

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| SQL | `SqlScanner` | Line/block comments, quoted strings, quoted identifiers, numbers, SQL identifiers, keywords, types, constants, and built-in functions. | Covered by `SqlFixtureTest.kt`. | SQL tokenizer passes `SqlKeywordRoles`, `SqlConstants`, `SqlTypeKeywords`, and `SqlBuiltinRoles` directly; `SqlScannerOptions.Standard` covers default mechanics. |
| SQLite | `SqlScanner` | SQL forms plus SQLite keywords, bracket/backtick identifiers, SQLite bind parameters, and blob literals. | Covered by `SQLiteFixtureTest.kt`. | SQLite lexicon composes SQL vocabulary vals with SQLite words; SQLite tokenizer enables SQLite scanner options. |
| PostgreSQL | `SqlScanner` | SQL forms plus PostgreSQL keywords and dollar-quoted strings. | Covered by `PostgresqlFixtureTest.kt`. | PostgreSQL lexicon composes SQL vocabulary vals with PostgreSQL words; PostgreSQL tokenizer only enables dollar-quoted strings. |
| INI | `IniScanner` | Section headers, key-value separators, comments, quoted values, booleans, null-like values, and numbers. | Covered by `IniFixtureTest.kt`. | INI owns the key-value scanner and lexicon. |
| Properties | `IniScanner` | Java-properties-style key-value files through the same key-value mechanics. | Covered by `PropertiesFixtureTest.kt`. | Properties has its own tokenizer package and public language id, delegating scanner mechanics to INI for V1. |
| Dotenv | `IniScanner` | Dotenv-style key-value files, `export` directives, comments, booleans, and numbers. | Covered by `DotenvFixtureTest.kt`. | Dotenv has its own tokenizer package and public language id, delegating scanner mechanics to INI for V1. |
| Shell | `ShellScanner` | Hash comments, control keywords, command words, dollar variables, substitutions, heredocs, strings, operators, and punctuation. | Covered by `ShellFixtureTest.kt`. | Shell owns the POSIX-shell-oriented scanner and lexicon. |
| Bash | `ShellScanner` | Bash currently uses the same shell lexical mechanics. | Covered by `BashFixtureTest.kt`. | Bash has its own tokenizer package and public language id, delegating scanner mechanics to Shell for V1. |
| Zsh | `ShellScanner` | Zsh currently uses the same shell lexical mechanics. | Covered by `ZshFixtureTest.kt`. | Zsh has its own tokenizer package and public language id, delegating scanner mechanics to Shell for V1. |

## Markup-Family Scanner

| Language | Scanner | Covered lexical forms | Fixture status | Tokenizer/lexicon-owned behavior |
|---|---|---|---|---|
| HTML | `MarkupScanner` | Tags, attributes, comments, declarations, CDATA, entities, and script/style raw-text embedded-language routing with `lang=` awareness. | Covered by `HtmlFixtureTest.kt`. | HTML tokenizer owns `HtmlRawTextTags`, chooses `MarkupScannerOptions.defaultRawTextLanguageForTag`, and preserves lowercase tag matching. |
| XML | `MarkupScanner` | XML-style tags, attributes, comments, declarations, processing instructions, CDATA, and entities. | Covered by `XmlFixtureTest.kt`. | XML tokenizer preserves tag-name case and leaves raw-text routing disabled through empty raw-text tags and the default no-op raw-text language function. |
| JSX | `MarkupScanner` | Markup comments/declarations, tags, attributes, entities inside markup, brace expressions inside markup, and JavaScript top-level expression routing. | Covered by `JsxFixtureTest.kt`. | JSX tokenizer owns expression language, starts-in-script mode, directive attributes, brace expression rules, raw-text language function choice, and `JsxRawTextTags`. |
| TSX | `MarkupScanner` | JSX forms plus TypeScript top-level expression routing outside markup. | Covered by `TsxFixtureTest.kt`. | TSX tokenizer owns TypeScript expression language, directive attributes, brace expression rules, raw-text language function choice, and `TsxRawTextTags`. |
