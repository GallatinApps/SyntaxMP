# Adding a Built-In Language

This is the maintainer-facing companion for adding a **built-in** language tokenizer to SyntaxMP itself — the languages that ship in `SyntaxLanguageId.BuiltIns` and live under `syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/languages/`. It is not the path for host applications adding a custom extension; that audience belongs to the public [docs/language-extension.md](../language-extension.md) guide.

[AGENTS.md "Adding a New Language"](../../AGENTS.md#adding-a-new-language) is the canonical checklist for the mechanical steps (folder layout, registration, routing, fixture placement, doc updates). This doc covers the parts AGENTS.md doesn't: how to research the language, how to map its constructs onto SyntaxMP's scanner mechanics, and how to write fixtures that actually defend the contract you're claiming. Read AGENTS.md first; come here for the deeper "how to think about it" material.

## Reference Posture

SyntaxMP's primary references are official language documentation, language specifications, and hand-written SyntaxMP fixture snippets.

External highlighters may be consulted for behavior ideas, common edge cases, or language alias coverage, but they are references only. Do not copy implementation code, regular expressions, keyword lists, fixture text, or generated output into SyntaxMP. No per-fixture attribution note is required when no external content is copied.

| Reference action | SyntaxMP rule |
|---|---|
| Reading official docs or language specs | Allowed. Prefer this for syntax rules, literal forms, and keywords. |
| Writing hand-made SyntaxMP fixture snippets | Allowed. Keep snippets small and focused on scanner behavior. |
| Consulting external highlighters for behavior ideas | Allowed. Use them to identify cases, then implement scanner logic from scratch. |
| Copying code, regexes, keyword lists, fixture text, or generated output | Avoid. If it becomes unavoidable, stop and add explicit attribution with source, version or commit, license, and copied scope before committing. |

## Inspection Steps

### 1. Start With Primary References

Use official language documentation or specs for:

- string delimiters, raw string forms, interpolation, and escapes
- numeric literal prefixes, suffixes, separators, and exponents
- line and block comments
- declaration, control, and type keywords
- standard aliases and file extensions

When official docs are impractical or incomplete for a narrow scanner question, use existing SyntaxMP behavior and targeted external references as supporting context.

### 2. Compare Aliases

Compare language-level aliases and file extensions with the `BuiltInAliases` map in:

```text
syntaxmp/src/commonMain/kotlin/com/gallatinapps/syntaxmp/engine/language/SyntaxLanguageId.kt
```

Add missing aliases only when they are useful to SyntaxMP users. Normalize aliases to lowercase. Do not add aliases that imply unsupported dialects.

### 3. Map Keyword Categories By Meaning

Map tokens by the role SyntaxMP should expose, not by another tool's category name. Start the vocabulary audit in `languages/<language>/<Language>Lexicon.kt`. Tokenizers and scanners should consume lexicon values for semantic word tables rather than keeping private keyword, constant, directive, or alias sets in implementation files.

| Language concept | SyntaxMP target |
|---|---|
| control and declaration words | Language lexicon `*KeywordRoles` map, emitted as `SyntaxRole.Keyword` with a specific scope when available. |
| built-in callable/type/namespace names | Language lexicon `*BuiltinRoles` map, emitted with the mapped role (`function.builtin`, `type`, `variable.namespace`, etc.). |
| built-in literal values | Language lexicon `constants` set, emitted as `SyntaxRole.Constant`. |
| built-in type names | Language lexicon `typeKeywords` set or an existing type heuristic, emitted as `SyntaxRole.Type`. |
| annotation/decorator syntax | Existing annotation handling where the scanner supports it. |
| labels, selectors, and fields | Existing property, selector, or attribute scopes where the scanner supports them. |

Do not adopt a reference keyword list wholesale. Extra noisy tokens are worse than missing obscure tokens, especially because SyntaxMP does not do relevance scoring or full parsing.

### 4. Map Scanner Behavior

Identify which existing scanner features can express the language behavior:

- line comment scanning
- block comment scanning
- single, double, triple, raw, or heredoc string scanning
- escape spans inside strings
- interpolation spans inside strings
- numeric literal lexing
- annotations, decorators, or attributes
- nested component-language ranges
- Markdown fenced-code ranges

If a behavior needs a new scanner option, add the smallest option that matches the family of languages using it. Put language-specific vocabulary and dialect choices in the language package; shared scanner packages should receive those facts through profiles. If a behavior is too parser-like for v1, document the gap inline near the fixture (a `KNOWN GAP` comment) or in [`fixture-coverage.md`](fixture-coverage.md) instead of widening the scanner into a parser.

### 5. Write Focused Fixtures

Place fixture tests under:

```text
syntaxmp/src/commonTest/kotlin/com/gallatinapps/syntaxmp/languages/fixtures/<language>/<Language>FixtureTest.kt
```

Each language fixture should cover the behavior the scanner claims to support:

- comments where the language supports them
- strings for each supported quote or raw-string style
- escapes and interpolation when supported
- numeric forms across supported prefixes, exponents, separators, and suffixes
- representative keywords, constants, types, declarations, functions, properties, or attributes
- nested embedded-language ranges where the language owns them
- at least one negative case

Prefer `assertTokenAt` for targeted single-token expectations. Use `assertTokens` only for tiny canonical snippets where every emitted token should be stable.

### 6. Identify Negative Cases

Each fixture file should include at least one negative assertion:

- Use `assertNoTokenAt` when the rejected text is a named substring.
- Use `assertNoToken` when the rejected condition is broader than one substring.

Examples:

- A keyword inside a string must not be emitted as `Keyword`.
- A comment-like marker inside a string must not open a comment.
- A number-looking suffix that the language rejects must not be consumed as one number.
- Interpolation marker text in a raw string must stay string content if that language treats it as literal text.

### 7. Iterate And Record Divergence

Run the relevant tests. For each failure, decide whether the fixture is wrong or the scanner is wrong.

If the scanner has a known gap that should not block the current phase, document it near the fixture:

```kotlin
// KNOWN GAP: Kotlin backtick identifiers are still emitted with the generic
// identifier path. Fixture expectation reflects current behavior.
```

Known-gap comments should be specific enough to become an issue or follow-up task later.

## Fixture Template

```kotlin
package com.gallatinapps.syntaxmp.languages.fixtures.kotlin

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class KotlinFixtureTest {

    @Test
    fun `fun keyword is keyword declaration`() = assertTokenAt(
        language = "kotlin",
        code = "fun greet(): String = \"hi\"",
        substring = "fun",
        category = "Keyword",
        scope = "keyword.declaration",
    )

    @Test
    fun `String type is recognized`() = assertTokenAt(
        language = "kotlin",
        code = "fun greet(): String = \"hi\"",
        substring = "String",
        category = "Type",
        scope = "type",
    )

    @Test
    fun `fun inside a string is not a keyword`() = assertNoTokenAt(
        language = "kotlin",
        code = "val message = \"fun stuff\"",
        substring = "fun",
        category = "Keyword",
    )
}
```

## Attribution Checklist

Before calling a language inspection complete:

- Primary language references were reviewed for the behavior being implemented.
- Any external highlighter or grammar was used only as behavior context, not as source material.
- No copied code, regex, keyword list, fixture text, or generated output was introduced.
- If copied content was introduced, attribution was added before commit with the source, version or commit, license, and exact copied scope.
- Aliases were compared against the `BuiltInAliases` map in `SyntaxLanguageId.kt`.
- Keyword categories were mapped by meaning.
- At least one negative case is present.
- Known gaps are documented inline or in `fixture-coverage.md`.
- `:syntaxmp:jvmTest` passes.
