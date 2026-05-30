# SyntaxMP Theme Color Policy

This document records SyntaxMP's theme-color grouping policy before concrete starter-theme or tokenizer changes land. Fixture coverage remains authoritative in [`fixture-coverage.md`](fixture-coverage.md); emitted role definitions remain cataloged in [`syntax-roles.md`](../syntax-roles.md).

This document is the permanent theme-policy reference for built-in language role and color policy.

Terminology: `SyntaxCategory`, `SyntaxPalette`, and `SyntaxRoleStyle` are not public symbols. Where this document says "category," read the current root `SyntaxRole` (such as `SyntaxRole.Keyword`); where it says "scope," read a dotted `SyntaxRole.value` path (such as `keyword.control` or `markup.diff.addition`).

## Purpose

SyntaxMP themes are lexical styling policies, not semantic editor themes. The policy matrix below is therefore about durable root-role and dotted-role groupings first. Concrete light and dark color values are paired `SyntaxRoleStyles` implementations of those groupings and should be chosen only after the grouping is approved.

`SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` remain simple root-role starter themes for Plan 21. Language-specific polish should be recorded here first and evaluated later as demo overrides or optional named themes, not silently folded into the defaults.

## Audience Calibration

Every language row should name one comparison target:

| Target | Meaning |
|---|---|
| `JetBrains` | IntelliJ IDEA, Android Studio, Fleet-adjacent Kotlin expectations, or JetBrains-family defaults are the main familiarity anchor. |
| `Xcode` | Xcode conventions are the main familiarity anchor. |
| `VS Code` | VS Code and common TextMate-style syntax themes are the main familiarity anchor. |
| `cross-editor` | No single editor family should dominate; use common ecosystem expectations and avoid overfitting. |

Initial family calibration before inventory:

| Language family | Initial target | Notes |
|---|---|---|
| Kotlin and Compose-oriented Kotlin | `JetBrains` | Kotlin/Compose is the first calibration target. IntelliJ-like grouping should be explicit, especially for types, functions, properties, annotations, and named arguments. |
| Swift and Objective-C | `Xcode` | Swift should be compared primarily with Xcode. Objective-C can note Xcode expectations while preserving C-like lexical behavior. |
| Java, Scala, Groovy, and Gradle | `JetBrains` | JetBrains-family conventions are common for JVM languages, but each row should still document divergences. |
| JavaScript, TypeScript, JSX, TSX, MDX, Vue, Svelte, Astro, HTML, CSS, SCSS, and Less | `VS Code` | VS Code/TextMate-style expectations are the broadest visual reference for web and component languages. |
| C, C++, C#, Go, Rust, Dart, Python, Ruby, PHP, Perl, Lua, R, Elixir, Shell, PowerShell, SQL, data/config formats, markup, and diff | `cross-editor` | Later review may refine individual rows when one ecosystem target is clearly stronger. C# remains a policy question because Visual Studio/Rider/VS Code expectations differ and the Phase 1 target vocabulary does not include Visual Studio. |

## Group Vocabulary

Groups are `SyntaxStyle` policy slots: each group can eventually define color, weight, and font-style. Phase 1 names the slots; Phase 5 and Phase 8 decide concrete paired light/dark values.

| Group | Meaning | Typical examples |
|---|---|---|
| `G0` | Host base code text | Plain punctuation-adjacent identifiers; language-specific type names when the chosen editor target keeps them neutral. |
| `G1` | Keywords and control syntax | `fun`, `return`, `if`, `class`, `import`. |
| `G2` | Callable names | Function declarations, member calls, command names, constructor calls when styled as calls. |
| `G3` | Variables, properties, and fields | Local names, dot members, object keys treated as identifiers; enum entries follow current role decisions unless a later plan approves refinement. |
| `G4a` | Parameter declarations and call-site labels | Kotlin named arguments such as `modifier =`, function parameter declarations when styled distinctly. |
| `G4b` | Runtime variable references | Shell `$HOME`, PowerShell `$value`, template variables when lexically identifiable. |
| `G4c` | Bind placeholders | SQLite placeholders such as `:title` or `?`, and future PostgreSQL placeholders only if scanner support is approved. |
| `G5` | Types and type-like names | Classes, interfaces, scalar type names, modules when styled distinctly. |
| `G6` | Annotations and metadata | `@Composable`, decorators, attributes, preprocessor metadata, hash directives. |
| `G7` | Strings | Quoted, raw, multiline, heredoc, and coherent string body text. |
| `G8` | Numbers | Numeric literals and units when emitted as number spans. |
| `G9` | Constants and literals | `true`, `null`, atoms, enum members that emit `Constant` under the current policy. |
| `G10` | Comments | Line, block, and documentation comments. |
| `G11` | Markup tags and selectors | HTML/XML tags, CSS selectors, component tags. |
| `G12` | Markup attributes and selector details | HTML/XML attributes, directive attributes, CSS class/id selectors, pseudo selectors. |
| `G13a` | Operators | `+`, `=>`, `?:`, assignment operators, pipeline operators, namespace operators. |
| `G13b` | Structural punctuation | Braces, brackets, commas, semicolons, delimiters, diff markers when muted. |
| `G14` | Escapes and interpolation delimiters | `${`, `\n`, format placeholders, entity escapes when not better grouped as markup. |
| `G15` | Domain-specific structural markup | Markdown headings/lists/fences, diff hunk/header/add/delete groups, CDATA delimiters. |

Notes for inventory and mapping:

- The same emitted role can map to different groups by language when current role names are overloaded. For example, `variable.parameter` might represent Kotlin named arguments (`G4a`), shell/runtime variables (`G4b`), or SQL bind placeholders (`G4c`).
- The matrix may add `G16`, `G17`, and later groups only when a durable styling role does not fit the existing vocabulary.
- Unknown languages and unclassified text do not have a public role. They should render as host plain text/no spans unless a later phase approves a public theme behavior for them.

## Starter Theme Color History

`SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` root-role colors are recorded in three steps: the original pre-Phase 8 implementation, the Phase 5 proposed palette, and the decided current palette after demo review. Keep all three sets so future palette work can see the history instead of overwriting earlier decisions.

| Root role | Original light | Original dark | Proposed light | Proposed dark | Decided current light | Decided current dark | Notes |
|---|---|---|---|---|---|---|---|
| `Keyword` | `#C2410C` | `#FF9E64` | `#9A3412` | `#F97316` | `#1E40AF` | `#F97316` | Light mode moves from warm keyword orange to stronger blue for readability. |
| `String` | `#15803D` | `#9ECE6A` | `#15803D` | `#9ECE6A` | `#15803D` | `#9ECE6A` | Keep. |
| `Number` | `#0E7490` | `#7DCFFF` | `#0E7490` | `#7DCFFF` | `#0E7490` | `#7DCFFF` | Keep. |
| `Comment` | `#6B7280` | `#7A8494` | `#6B7280` | `#7A8494` | `#6B7280` | `#7A8494` | Keep regular-weight, non-italic. |
| `Function` | `#2563EB` | `#82AAFF` | `#1D4ED8` | `#82AAFF` | `#00796B` | `#82AAFF` | Light mode uses stronger teal/aquamarine to separate calls from numbers and keywords. |
| `Type` | `#7C3AED` | `#C792EA` | `#6D28D9` | `#C792EA` | `#7E22CE` | `#C792EA` | Light mode moves more clearly purple so type names stand apart from keyword blue. |
| `Property` | `#BE185D` | `#FF79C6` | `#BE185D` | `#FF79C6` | `#BE185D` | `#FF79C6` | Keep shared with `Variable`. |
| `Variable` | `#BE185D` | `#FF79C6` | `#BE185D` | `#FF79C6` | `#BE185D` | `#FF79C6` | Keep shared with `Property`. |
| `Operator` | `#64748B` | `#89DDFF` | `#334155` | `#89DDFF` | `#334155` | `#89DDFF` | Keep stronger than punctuation. |
| `Punctuation` | `#64748B` | `#A9B1D6` | `#64748B` | `#A9B1D6` | `#64748B` | `#A9B1D6` | Keep muted structural role. |
| `Annotation` | `#B58900` | `#FACC15` | `#A16207` | `#FACC15` | `#B45309` | `#FACC15` | Light mode uses stronger gold so annotations do not collapse into keywords/types. |
| `Tag` | `#047857` | `#7AA2F7` | `#047857` | `#7AA2F7` | `#166534` | `#7AA2F7` | Light mode uses darker green for tag and element-selector readability. |
| `Attribute` | `#0E7490` | `#7DCFFF` | `#0E7490` | `#7DCFFF` | `#1D4ED8` | `#7DCFFF` | Light mode uses stronger blue to separate attributes/class selectors from tags. |
| `Constant` | `#B91C1C` | `#FF757F` | `#B91C1C` | `#FF757F` | `#B91C1C` | `#FF757F` | Keep distinct from keyword, annotation, and escape. |
| `Escape` | `#C2410C` | `#FF9E64` | `#9A3412` | `#F97316` | `#1E40AF` | `#F97316` | Share with `Keyword` in starter themes. |
| `Markup` | `#475569` | `#B7C0D8` | `#475569` | `#B7C0D8` | `#475569` | `#B7C0D8` | Keep one structural root default. |

History observations:

- `Property` and `Variable` share colors across all three recorded palettes.
- `Operator` and `Punctuation` shared the original light color; proposed and decided palettes split light-mode operators stronger than punctuation.
- `Keyword` and `Escape` share colors across all three recorded palettes.
- `Type` remains globally distinct from host base text across all three recorded palettes.

Optional custom or language-aware themes should be documented separately.

## Current Demo Overrides

The demo currently uses language-specific overrides as showcase policy, not library defaults.

| Demo policy | Current behavior | Phase 1 interpretation |
|---|---|---|
| Kotlin `function` | Uses a blue/azure demo color, anchored by dark-mode `#649EEE`. | Example of Compose/Kotlin showcase polish. It should not become a default-theme change without a future optional-theme plan. |
| Kotlin `type` | Uses a muted near-base text color in the demo theme. | Example of a JetBrains-like language-specific policy. It should not become a default-theme change without Phase 5 approval. |
| Kotlin `constant` and `escape` | Map to the keyword style. | Example of a Kotlin-specific grouping that may or may not generalize. |
| Kotlin `constant.builtin` | Maps to the keyword style through a dotted-role override. | Role-level policy; not eligible for a root-role default unless Phase 5 finds a root-role equivalent. |
| Kotlin `variable.parameter` | Uses a muted near-base text color for Compose-style named arguments. | Example of a dotted-role named-argument policy that requires either a demo override or an optional language-aware theme. |
| CSV `punctuation` | Uses the demo accent with semibold weight. | Demo showcase override. It does not imply that punctuation should globally become an accent color. |
| Diff structure and line states | Demo themes style `markup.diff.header` / `markup.diff.hunk` as structural accents, `markup.diff.addition` green, and `markup.diff.deletion` red. | Demo showcase override using fixture-backed child roles. Defaults still inherit root `Markup`. |

The demo `Diagnostic` theme remains a visual audit aid for emitted roles and should not be treated as theme policy.

## Decision Rules

Use these rules when Phase 3 through Phase 6 classify recommendations:

| Recommendation | Use when |
|---|---|
| `Default candidate` | The desired behavior can be expressed with root-role-first `SyntaxRoleStyles`, benefits multiple language families, and does not depend on language id or narrow dotted roles. |
| `Optional bundled theme` | The desired behavior is durable and broadly useful, but it depends on language-specific or dotted-role policy that should not live in `DefaultLight` / `DefaultDark`. |
| `Demo only` | The behavior is useful for SyntaxMP's showcase, samples, or diagnostics but is too opinionated, incomplete, or host-specific for library API. |
| `None` | Current root-role starter behavior is sufficient and no library/demo override is recommended. |
| `Policy question` | The behavior is ambiguous, subjective, or blocked by missing role/fixture evidence. Record the question instead of inventing an answer. |

Default-theme candidates must stay compatible with the Plan 21 rule that `DefaultLight` and `DefaultDark` remain root-role starter themes. Any per-language override, narrow dotted-role override, or IDE-family-specific policy belongs in `Optional bundled theme`, `Demo only`, or `Policy question` until a later approved public API plan says otherwise.

## Role Compatibility Policy

`SyntaxRole.value` is the compatibility-sensitive public token identity. Dotted child roles are the intended refinement surface for future language nuance.

Root-role changes:

| Change | Compatibility expectation |
|---|---|
| Add a root role | Minor version at minimum, with migration notes for exhaustive role handling, custom themes, serialized role names, and snapshot tests. Consider a major version after 1.0 if root-role exhaustiveness has become a strong public contract. |
| Rename a root role | Major version after 1.0. Treat as a pre-1.0 migration only when the project explicitly accepts role churn. |
| Remove a root role | Major version after 1.0. Avoid unless the role is proven redundant and a migration path is documented. |
| Change a token's root role | Minor version at minimum because default themes and root-role custom themes can change visibly. Reserve patch releases for narrow bug fixes where the old role was clearly incorrect and recently introduced. |

Dotted-role changes:

| Change | Compatibility expectation |
|---|---|
| Add a dotted role under the same root role | Usually patch-compatible when narrow, because themes fall back to the parent/root role style. Use a minor version when the visible behavior is broad or the role becomes part of documented theme policy. |
| Rename a documented dotted role | Minor version at minimum, with old/new role notes. Avoid churn in stable themes. |
| Remove a documented dotted role | Minor version at minimum; consider major only if many themes are expected to depend on it. Prefer retaining support or documenting replacement roles when possible. |
| Add a dotted role that changes root role | Treat as a root-role change, not as a simple dotted-role addition. |

New root-role admission bar:

- The role appears across multiple unrelated language families.
- The role cannot be modeled cleanly as a dotted role under an existing root.
- The role deserves a typed root object and a simple starter-theme entry.
- Fixture-backed examples exist before implementation.
- Fallback behavior for older themes and root-role custom themes is documented.
- The migration impact is worth expanding the public role surface.

Future language polish should prefer dotted roles first. For example, `annotation.macro`, `function.macro`, `constant.enum`, `property.enum`, or `variable.label` should be evaluated before adding root roles such as `Macro`, `Enum`, or `Label`.

## Group Style Decisions

This table records the decided current starter-theme group styles for root-role defaults only. No group below requires a child-role default entry in `SyntaxTheme.DefaultLight` or `SyntaxTheme.DefaultDark`.

| Group | Decided light style | Decided dark style | Weight/style | Contrast notes | Decision source |
|---|---|---|---|---|---|
| `G0` | Host `TextStyle` | Host `TextStyle` | Host-owned | No SyntaxMP base style. | Phase 5 |
| `G1` | `#1E40AF` | `#F97316` | Regular | Keyword and escape starter color. | Phase 8 light contrast review |
| `G2` | `#00796B` | `#82AAFF` | Regular | Callable root style; light mode uses clearer teal/aquamarine. | Phase 8 light contrast review |
| `G3` | `#BE185D` | `#FF79C6` | Regular | Shared variable/property root style. | Phase 5 |
| `G4a` | Inherit `Variable` | Inherit `Variable` | Regular | Named arguments stay optional/demo. | Phase 5, defer split to Phase 6 |
| `G4b` | Inherit `Variable` | Inherit `Variable` | Regular | Runtime variables stay optional/demo. | Phase 5, defer split to Phase 6 |
| `G4c` | Inherit `Variable` | Inherit `Variable` | Regular | SQL bind placeholders stay optional/demo. | Phase 5, defer split to Phase 6 |
| `G5` | `#7E22CE` | `#C792EA` | Regular | Globally distinct type root style; light mode is more clearly purple. | Phase 8 light contrast review |
| `G6` | `#B45309` | `#FACC15` | Regular | Gold annotation style, separate from keyword. | Phase 8 light contrast review |
| `G7` | `#15803D` | `#9ECE6A` | Regular | String root style. | Phase 5 |
| `G8` | `#0E7490` | `#7DCFFF` | Regular | Number root style. | Phase 5 |
| `G9` | `#B91C1C` | `#FF757F` | Regular | Constant root style. | Phase 5 |
| `G10` | `#6B7280` | `#7A8494` | Regular | Comments stay muted without forcing italics. | Phase 5 |
| `G11` | `#166534` | `#7AA2F7` | Regular | Tag root style; light mode is darker than attributes. | Phase 8 light contrast review |
| `G12` | `#1D4ED8` | `#7DCFFF` | Regular | Attribute root style; light mode separates from tags. | Phase 8 light contrast review |
| `G13a` | `#334155` | `#89DDFF` | Regular | Operators stronger than punctuation. | Phase 5 |
| `G13b` | `#64748B` | `#A9B1D6` | Regular | Structural punctuation stays muted. | Phase 5 |
| `G14` | `#1E40AF` | `#F97316` | Regular | Escape shares with keyword in starters. | Phase 8 light contrast review |
| `G15` | `#475569` | `#B7C0D8` | Regular | One root markup style; child splits are optional. | Phase 5 |

## Light Mode Contrast Decision

Phase 8 demo review found the dark theme direction readable and accepted a higher-contrast light-mode direction. The demo app was the test bench for this contrast pass. The `Starter Theme Color History` table above intentionally keeps the original pre-Phase 8 values, the Phase 5 proposed values, and the decided current values side by side.

Decision notes:

- Audit light mode on multiple languages, not only Kotlin. Swift is the current cross-check: `struct`, `enum`, and `var` use keyword roles, while `@MainActor` uses `Annotation`; the light keyword/type/annotation colors need enough separation that these do not collapse visually.
- The decided light palette strengthens root colors for `Keyword`, `Function`, `Type`, `Annotation`, `Tag`, `Attribute`, and `Escape` instead of solving the issue with Kotlin-only overrides.
- `Type` is more distinctly purple from keyword blue. `Function` is more teal/aquamarine so Swift call/modifier names are easier to distinguish from numeric literals. `Tag` is darker green while `Attribute` is stronger blue so HTML/Astro/CSS tags and element selectors do not collapse into class/attribute selectors.
- Keep the dark palette mostly stable unless a specific contrast problem is found there.
- Keep `DefaultLight` free of language overrides. Kotlin/Compose IntelliJ-like polish can remain demo-owned or future optional-theme policy.
- For Kotlin light-mode demo polish, compare against JetBrains/IntelliJ light-theme behavior: declaration keywords such as `fun`, `class`, and `private` may want a darker blue-family color, Compose/function names may want a darker blue-green/teal-family color, and annotations such as `@Composable` may need a stronger yellow/gold than the current muted root annotation.
- Do not use demo tests to assert tokenizer role correctness. Use fixture tests for role changes and demo tests only for demo-owned theme policy.

## Phase 5 Decision Inputs

These tables roll the Phase 2-4 language inventory and visual-group mapping into a higher-level view for Phase 5. They are decision support only: they do not change tokenizer behavior, public API, or the root-role starter-theme rule.

Counting rules:

- `Languages using` counts direct language rows in the Language Matrix below. Delegated child language routing, such as Markdown fences or component `<script>`/`<style>` blocks, is not expanded into each host language count.
- `Families` gives a lightweight language-family read when it is helpful. `All families` means the root role crosses programming, scripting, markup/web, data/config, and shell/diff-style rows.
- Root role `Languages with current dotted roles` counts languages where that root role has at least one more-specific current dotted role such as `variable.parameter` or `keyword.control`.
- `Distinct current dotted roles`, `Child patterns`, and rows ending in `.<value>` / `.<kind>` are normalized patterns from the current policy matrix, not exhaustive literal token values.
- `Style-pressure read` summarizes the Phase 3/4 group mappings. It is not a final palette decision. A root role can be globally stable while still having optional language-aware child-role nuance.

### Reviewer Direction Before Phase 5

These decisions were recorded before the full Phase 5 recommendation pass. Phase 5 should translate them into formal default-theme grouping and proposed light/dark values without adding per-language overrides to `SyntaxTheme.DefaultLight` or `SyntaxTheme.DefaultDark`.

| Question | Direction |
|---|---|
| `Variable` and `Property` | Share one starter-theme style. The current baseline already does this, and the Phase 4.6 table shows high sharing pressure. |
| `Type` versus base | Keep `Type` distinct globally. Kotlin/JetBrains-style `type -> base` remains a language-aware or optional-theme policy, not a root-role default. |
| `Keyword` | Keep distinct. Avoid the previous overly-orange feeling when choosing proposed colors. |
| `Constant` | Keep distinct from `Keyword`, `Annotation`, and `Escape`. |
| `Annotation` | Keep distinct, especially for Kotlin and Swift readability. A muted yellow-gold is acceptable, but annotations should not collapse into keyword orange. |
| `Escape` | May share with `Keyword` in the simple starter themes. |
| `Operator` and `Punctuation` | Keep close in the theme, with operators slightly stronger and structural punctuation muted. |
| `Tag` and `Attribute` | Keep distinct in starter themes; markup and component languages depend on this distinction for scanning. |
| `Markup` and diff structural roles | Keep one root-role `Markup` default style. Richer Markdown or diff split colors belong in dotted-role optional themes or demo policy. |

### Active Root Role Usage Summary

Phase 4.6 refreshed this summary against the Plan 23 `SyntaxRole` model. The table below is the active Phase 5 input; superseded category/scope paths are listed separately afterward for migration context only.

| Root role | Languages using | Families | Languages with current dotted roles | Distinct current dotted roles | Style-pressure read | Phase 5 read |
|---|---:|---|---:|---:|---|---|
| `String` | 49 | all families | 6 | 4 | Stable unique mapping. | Global stable slot. |
| `Comment` | 44 | all families | 0 | 0 | Stable unique mapping. | Global stable slot. |
| `Number` | 40 | all families | 0 | 0 | Stable unique mapping. | Global stable slot. |
| `Keyword` | 39 | all families | 27 | 5 | Mostly unique; Kotlin/demo sharing is narrow. | Global slot; Plan 23 removed language-name keyword paths from active policy. |
| `Constant` | 35 | all families | 34 | 4 | Mostly unique; Kotlin/demo sharing is the main exception. | Common slot; child literals stay under `Constant` by default. |
| `Function` | 35 | all families | 18 | 5 | Stable unique mapping. | Global stable slot with a few useful dotted refinements. |
| `Property` | 35 | all families | 7 | 3 | High sharing pressure with `Variable`. | Share starter-theme style with `Variable`. |
| `Escape` | 34 | all families | 0 | 0 | Usually unique, but component delimiters and Kotlin share. | May share with `Keyword` in simple starter themes. |
| `Variable` | 28 | all families | 13 | 6 | Mixed: identifiers share with `Property`; parameters/runtime/binds split. | Root variable should share with `Property`; overloaded child roles stay optional/theme-specific. |
| `Type` | 27 | programming 14, data/config 9 | 0 | 0 | Mostly distinct; Kotlin optional policy shares with base. | Keep globally distinct from host base text. |
| `Annotation` | 17 | programming 11, scripting 3 | 0 | 0 | Stable unique mapping. | Programming-heavy, distinct from `Keyword` and `Constant`. |
| `Punctuation` | 15 | markup/web 10, data/config 5 | 2 | 1 | Mostly structural; JSX/TSX expression braces share with `Escape`. | Keep close to `Operator`, but slightly more muted. |
| `Attribute` | 12 | markup/web 11, data/config 1 | 6 | 3 | Stable unique mapping. | Markup/web-heavy, distinct from `Tag`. |
| `Tag` | 12 | markup/web 11, data/config 1 | 0 | 0 | Stable unique mapping. | Markup/web-heavy, distinct from `Attribute`. |
| `Operator` | 11 | all families | 0 | 0 | Stable unique mapping. | Sparse but stable; keep slightly stronger than punctuation. |
| `Markup` | 7 | markup/web 6, data/config 1 | 7 | 28 | Root mapping is narrow; child roles carry most nuance. | Keep one root default; split child markup only in optional/demo themes. |

### Active Dotted Role Usage Summary

This table includes current emitted dotted roles plus styleable parent prefixes that Plan 23's root-to-exact cascade can target. Exact role strings remain cataloged in [`syntax-roles.md`](../syntax-roles.md). Rows such as `markup.diff.<kind>` and `constant.builtin.<value>` are normalized patterns from the current policy matrix, not a claim that the literal `<kind>` or `<value>` text is emitted.

| Role / prefix | Root role | Languages using | Families | Child patterns | Phase 5 read |
|---|---|---:|---|---:|---|
| `constant.builtin` | `Constant` | 34 | all families | 2 | Styleable parent prefix; keep under `Constant` by default. |
| `constant.builtin.<value>` | `Constant` | 34 | all families | 0 | Built-in literal pattern; keep under `Constant` by default. |
| `keyword.control` | `Keyword` | 22 | programming 13, scripting 7, shell 2 | 0 | Keep as `Keyword` unless optional themes need nuance. |
| `keyword.declaration` | `Keyword` | 21 | programming 14, scripting 6, data/config 1 | 0 | Keep as `Keyword` unless optional themes need nuance. |
| `function.builtin` | `Function` | 21 | programming 10, scripting 7, data/config 3, shell 1 | 0 | Useful callable refinement; no starter-default split required. |
| `keyword.modifier` | `Keyword` | 10 | programming 10 | 0 | Keep as `Keyword` unless optional themes need nuance. |
| `variable.parameter` | `Variable` | 8 | programming 1, scripting 3, data/config 2, shell 2 | 0 | Overloaded by language; group as `G4a`, `G4b`, or `G4c` outside root defaults. |
| `variable.namespace` | `Variable` | 9 | programming 5, scripting 2, markup/web 2 | 0 | Useful but broader namespace policy remains deferred. |
| `markup.expression` | `Markup` | 4 | markup/web 4 | 0 | Component expression delimiter sharing pressure. |
| `property.name` | `Property` | 4 | data/config 4 | 0 | Plan 22 candidate for broader object/config key policy. |
| `attribute.directive` | `Attribute` | 3 | markup/web 3 | 0 | Markup/component nuance; optional-theme candidate. |
| `attribute.pseudo` | `Attribute` | 3 | markup/web 3 | 0 | Selector nuance; keep under `Attribute` by default. |
| `constant.color` | `Constant` | 3 | markup/web 3 | 0 | Constant subtype; optional themes can color-swatch it later. |
| `keyword.at-rule` | `Keyword` | 3 | markup/web 3 | 0 | Shared stylesheet at-rule role; keep under `Keyword` by default. |
| `string.regex` | `String` | 3 | programming 2, scripting 1 | 0 | String subtype; optional themes can distinguish regexes. |
| `property.quoted` | `Property` | 2 | data/config 2 | 0 | Quoted identifier/key nuance; keep under `Property`. |
| `property.section` | `Property` | 2 | data/config 2 | 0 | Section/table headers may become optional structural styling. |
| `punctuation.expression` | `Punctuation` | 2 | markup/web 2 | 0 | JSX/TSX expression delimiters can share with `Escape` in optional themes. |
| `string.language` | `String` | 2 | markup/web 2 | 0 | Fence language labels; keep under `String` by default. |
| `string.url` | `String` | 2 | markup/web 2 | 0 | Link destinations; keep under `String` by default. |
| `attribute.parent-selector` | `Attribute` | 1 | markup/web 1 | 0 | SCSS parent-selector nuance. |
| `constant.atom` | `Constant` | 1 | scripting 1 | 0 | Elixir atom nuance; optional-theme candidate. |
| `constant.builtin.important` | `Constant` | 1 | markup/web 1 | 0 | CSS important marker; optional-theme candidate. |
| `function.cmdlet` | `Function` | 1 | shell 1 | 0 | PowerShell callable nuance. |
| `function.declaration` | `Function` | 1 | programming 1 | 0 | Kotlin callable nuance. |
| `function.macro` | `Function` | 1 | programming 1 | 0 | Rust macro nuance; optional-theme candidate. |
| `function.member` | `Function` | 1 | programming 1 | 0 | Kotlin member-call nuance. |
| `keyword.wildcard` | `Keyword` | 1 | programming 1 | 0 | Swift wildcard marker; keep a policy question for Xcode-like styling. |
| `markup.cdata` | `Markup` | 1 | markup/web 1 | 0 | XML structural markup. |
| `markup.diff` | `Markup` | 1 | data/config 1 | 6 | Styleable diff parent prefix; optional themes can split additions/deletions. |
| `markup.frontmatter` | `Markup` | 1 | markup/web 1 | 0 | Astro structural markup. |
| `markup.heading` | `Markup` | 1 | markup/web 1 | 6 | Styleable Markdown heading parent prefix; no starter-default split required. |
| `markup.task` | `Markup` | 1 | markup/web 1 | 2 | Styleable Markdown task parent prefix; no starter-default split required. |
| Markdown `markup.*` | `Markup` | 1 | markup/web 1 | 19 | Markdown structural roles; keep one root default unless optional Markdown policy is approved. |
| `string.yaml` | `String` | 1 | data/config 1 | 1 | Styleable YAML string parent prefix. |
| `string.yaml.block-indicator` | `String` | 1 | data/config 1 | 0 | Structurally useful string subtype; Phase 5 decides whether style stays string-like. |
| `variable.css` | `Variable` | 1 | markup/web 1 | 1 | Styleable CSS variable parent prefix. |
| `variable.css.custom-property` | `Variable` | 1 | markup/web 1 | 0 | CSS custom-property reference role; declarations remain `property`. |
| `variable.splatting` | `Variable` | 1 | shell 1 | 0 | PowerShell runtime-variable nuance. |
| `variable.yaml` | `Variable` | 1 | data/config 1 | 2 | Styleable YAML variable parent prefix. |
| `variable.yaml.alias` | `Variable` | 1 | data/config 1 | 0 | YAML alias role; optional-theme candidate. |
| `variable.yaml.anchor` | `Variable` | 1 | data/config 1 | 0 | YAML anchor role; optional-theme candidate. |

### Superseded Historical Role Paths

These paths appeared in earlier Plan 21 inventory or docs but are not active Phase 5 decision inputs. They are retained here only to explain how the new role model maps old evidence.

| Historical path | Current replacement | Reason |
|---|---|---|
| `diff.*` | `markup.diff.*` | Diff is now a child family under root `markup`. |
| `function.css.<name>` | `function` | CSS/SCSS/Less function names use root `function`; CSS-specific policy should use the language axis. |
| `keyword.sql`, `keyword.sql.<word>` | `keyword` plus `SyntaxTokenSpan.languageId` | SQL-specific keyword styling should use language overrides, not language names in role paths. |
| `keyword.apache`, `keyword.apache.directive` | `keyword` plus `SyntaxLanguageId.ApacheConfig` | Apache-specific keyword styling should use the language axis. |
| `keyword.dns`, `keyword.dns.class`, `keyword.dns.directive` | `keyword` plus `SyntaxLanguageId.DnsZone` | DNS-specific keyword styling should use the language axis. |
| `keyword.dockerfile` | `keyword` plus `SyntaxLanguageId.Dockerfile` | Dockerfile-specific keyword styling should use the language axis. |
| `keyword.graphql`, `keyword.graphql.<word>` | `keyword` plus `SyntaxLanguageId.GraphQL` | GraphQL-specific keyword styling should use the language axis. |
| `keyword.makefile` | `keyword` plus `SyntaxLanguageId.Makefile` | Makefile-specific keyword styling should use the language axis. |
| `keyword.scss`, `keyword.scss.<word>` | `keyword.at-rule` for stylesheet directives, otherwise `keyword` | Shared at-rule semantics replace SCSS-language keyword paths. |
| `keyword.svelte`, `keyword.svelte.block`, `keyword.svelte.reactive` | `keyword` plus `SyntaxLanguageId.Svelte` | Svelte-specific keyword styling should use the language axis. |
| `keyword.terraform`, `keyword.terraform.<word>` | `keyword` plus `SyntaxLanguageId.Terraform` | Terraform-specific keyword styling should use the language axis. |
| `variable.less`, `variable.scss` | `variable` plus the relevant stylesheet language | Stylesheet variable-specific styling should use language overrides. |
| Makefile `variable.parameter` | `variable` plus `SyntaxLanguageId.Makefile` | Current Makefile scanning emits root `variable`; `variable.parameter` remains for named arguments, runtime variables, and SQL binds. |

## Language Matrix

Phase 2 filled this inventory from `scopes.md`, `fixture-coverage.md`, `SyntaxLanguageId.BuiltIns`, the owning tokenizer/scanner source where a doc mismatch surfaced, and the demo sample catalog.

Inventory notes:

- All 54 built-in language ids in `SyntaxLanguageId.BuiltIns` have `Covered` fixture-coverage rows.
- All 54 built-in language ids have registered demo samples in `DemoLanguageCatalog`.
- Demo samples were checked as showcase/discovery coverage only. No demo tests were changed or treated as tokenizer correctness assertions.
- Rows separate root roles from dotted/specific roles. Role names that are emitted by delegated child languages are summarized as child-language routing rather than repeated in every component-host row.
- Phase 3 group assignments live in the dedicated mapping tables below. Default-theme impact and override recommendations remain pending until Phase 5 and Phase 6.

| Language | Audience target | Current roles | Visual groups | Default-theme impact | Recommended override | Role gaps | Fixture gaps | Follow-up route |
|---|---|---|---|---|---|---|---|---|
| Apache config (`apacheconf`) | `cross-editor` | Generic: `keyword`, `tag`, `variable`, `string`, `operator`, `punctuation`, `comment`; dotted/specific: none. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.apache.directive` to root `keyword`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| Astro (`astro`) | `VS Code` | Generic: component `tag`, `attribute`, `string`, `escape`, `punctuation`; dotted/specific: `attribute.directive`, `markup.expression`, `markup.frontmatter`; child TS/JS/CSS/SCSS roles by frontmatter and raw-text routing. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 role gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| C (`c`) | `cross-editor` | Generic: `annotation`, `type`, `function`, `property`, `constant`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Enum members currently use root `constant`; Phase 4 rejected dotted enum refinements for Plan 21. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| C# (`csharp`) | `Policy question` | Generic: `annotation`, `type`, `function`, `property`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Audience target remains open across Visual Studio, Rider/JetBrains, and VS Code expectations. | Fixture row `Covered`; demo sample present. | Resolve target before Phase 6 optional-theme recommendations. |
| C++ (`cpp`) | `cross-editor` | Generic: `annotation`, `type`, `function`, `constant`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `function.builtin`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented namespace paths and rejected dotted enum refinements for Plan 21. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| CSS (`css`) | `VS Code` | Generic: `tag`, `attribute`, `property`, `variable`, `function`, `string`, `number`, `operator`, `punctuation`, `comment`; dotted/specific: `attribute.pseudo`, `variable.css.custom-property`, `keyword.at-rule`, `constant.color`, `constant.builtin.important`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 clarified custom-property declarations as `Property` and value references as `variable.css.custom-property`; Plan 23 collapsed CSS function names to root `function`. | Fixture row `Covered`; demo sample present. | Phase 5/6 styling only. |
| CSV (`csv`) | `cross-editor` | Generic: `punctuation`; dotted/specific: none. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | Demo accent punctuation remains showcase-only until Phase 6. |
| Dart (`dart`) | `cross-editor` | Generic: `annotation`, `type`, `function`, `property`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Enum members currently use root-role decisions; Phase 4 rejected dotted enum refinements for Plan 21. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| Diff (`diff`) | `cross-editor` | Generic: none; dotted/specific: `markup.diff.header`, `markup.diff.hunk`, `markup.diff.context`, `markup.diff.deletion`, `markup.diff.addition`, `markup.diff.no-newline`. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 renamed old `diff.*` roles under `markup.diff.*`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| DNS zone (`dns-zone`) | `cross-editor` | Generic: `keyword`, `type`, `property`, `variable`, `string`, `number`, `comment`; dotted/specific: none. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.dns.*` to root `keyword`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| Dockerfile (`dockerfile`) | `cross-editor` | Generic: `keyword`, `attribute`, `variable`, `string`, `number`, `operator`, `punctuation`, `comment`; dotted/specific: none; shell heredoc bodies can emit Shell child roles. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.dockerfile` to root `keyword`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| Elixir (`elixir`) | `cross-editor` | Generic: `type`, `function`, `property`, `annotation`, `operator`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `constant.atom`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented pipeline operators; no tokenizer action. | Fixture row `Covered`; demo sample present. | Phase 5 styling only. |
| GLSL (`glsl`) | `cross-editor` | Generic: `annotation`, `keyword`, `type`, `function`, `property`, `number`, `comment`; dotted/specific: `function.builtin`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| Go (`go`) | `cross-editor` | Generic: `type`, `function`, `property`, `constant`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `constant` declaration identifiers and `constant.builtin.<value>` need separate mapping review. | Fixture row `Covered`; demo sample present. | Phase 3 mapping question. |
| GraphQL (`graphql`) | `cross-editor` | Generic: `keyword`, `type`, `function`, `property`, `annotation`, `number`, `comment`; dotted/specific: `variable.parameter`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a GraphQL variable role here, not a named-argument policy. | Fixture row `Covered`; demo sample present. | Keep current role; map to `G4b`. |
| Groovy (`groovy`) | `JetBrains` | Generic: `type`, `function`, `property`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Parenthesis-free DSL calls can remain lexical `Variable`; callable-vs-property polish is deferred. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| HCL (`hcl`) | `cross-editor` | Generic: `type`, `property`, `variable`, `function`, `string`, `escape`, `number`, `comment`; dotted/specific: `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| HTML (`html`) | `VS Code` | Generic: `tag`, `attribute`, `string`, `escape`, `punctuation`, `comment`, `annotation`; dotted/specific: none; child scopes in `script`/`style` raw text. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| INI/properties (`ini`) | `cross-editor` | Generic: `property`, `string`, `number`, `keyword`, `comment`; dotted/specific: `property.section`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| Java (`java`) | `JetBrains` | Generic: `annotation`, `type`, `function`, `property`, `variable`, `constant`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented variables, namespace paths, and enum constants; Java field/property polish remains deferred. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| JavaScript (`javascript`) | `VS Code` | Generic: `type`, `function`, `property`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `variable.namespace`, `string.regex`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Object keys and properties share `property`; broader key refinement remains a Plan 22 policy question. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| JSON (`json`) | `cross-editor` | Generic: `string`, `number`, `punctuation`, `escape`; dotted/specific: `property.name`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| JSON5 (`json5`) | `cross-editor` | Generic: `string`, `number`, `comment`, `escape`; dotted/specific: `property.name`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| JSX (`jsx`) | `VS Code` | Generic: JavaScript host roles plus component `tag`, `attribute`, `string`, `escape`, `punctuation`; dotted/specific: `punctuation.expression`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 role gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| Kotlin (`kotlin`) | `JetBrains` | Generic: `annotation`, `type`, `function`, `property`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `function.builtin`, `function.declaration`, `function.member`, `variable.parameter`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Kotlin enum entries currently emit `Property`; `variable.parameter` is named-argument policy here; Phase 4 documented namespace paths. | Fixture row `Covered`; demo sample present. | Preserve demo override until Phase 6 decision. |
| Less (`less`) | `VS Code` | Generic: `tag`, `attribute`, `property`, `variable`, `function`, `string`, `number`, `operator`, `punctuation`, `comment`; dotted/specific: `keyword.at-rule`, `attribute.pseudo`, `constant.color`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `variable.less` to root `variable` and CSS function names to root `function`. | Fixture row `Covered`; demo sample present. | Phase 5/6 styling only. |
| Lua (`lua`) | `cross-editor` | Generic: `function`, `property`, `variable`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| Makefile (`makefile`) | `cross-editor` | Generic: `keyword`, `function`, `property`, `variable`, `operator`, `comment`; dotted/specific: none. | Phase 3 | Phase 5 | Phase 3/6 | Make variables now emit root `variable`, not `variable.parameter`; language-aware styling can use `SyntaxLanguageId.Makefile`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| Markdown (`markdown`) | `cross-editor` | Generic: `escape`; dotted/specific: `markup.heading.h1`-`markup.heading.h6`, `markup.quote`, `markup.list`, `markup.task.checked`, `markup.task.unchecked`, `markup.emphasis`, `markup.strikethrough`, `markup.thematic-break`, `markup.code`, `markup.link`, `markup.reference`, `markup.image`, `markup.table`, `markup.fence`, `string.language`, `string.url`; child language roles inside resolved fences. | Phase 3 | Phase 5 | Phase 3/6 | Phase 7 refined broad heading/task roles while preserving parent fallback through `markup.heading` and `markup.task`. | Fixture row `Covered`; demo sample present. | Phase 7 complete. |
| MDX (`mdx`) | `VS Code` | Generic: Markdown host roles plus component `tag`, `attribute`, `string`, `escape`, `punctuation`; dotted/specific: Markdown `markup.*`, `string.language`, `string.url`, plus `markup.expression`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 role gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| Objective-C (`objective-c`) | `Xcode` | Generic: `annotation`, `keyword`, `type`, `variable`, `property`, `function`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented comments and Objective-C/C built-in constants; selector-part polish remains deferred. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| Perl (`perl`) | `cross-editor` | Generic: `function`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `variable.parameter`, `variable.namespace`, `string.regex`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a sigil-variable scope here, not a named-argument policy; Phase 4 documented namespace paths. | Fixture row `Covered`; demo sample present. | Keep current scopes; map sigil variables to `G4b`. |
| PHP (`php`) | `cross-editor` | Generic: `type`, `function`, `property`, `annotation`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `variable.parameter`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a dollar-variable scope here, not a named-argument policy. | Fixture row `Covered`; demo sample present. | Keep current scope; map to `G4b`. |
| PostgreSQL (`postgresql`) | `cross-editor` | Generic: `keyword`, `type`, `string`, `operator`, `function`, `number`, `comment`; dotted/specific: `function.builtin`. | Phase 3 | Phase 5 | Phase 3/6 | PostgreSQL bind placeholders are not currently emitted; Phase 4 rejected adding docs or `G4c` mapping without scanner work. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| PowerShell (`powershell`) | `cross-editor` | Generic: `function`, `string`, `escape`, `operator`, `number`, `comment`; dotted/specific: `keyword.control`, `function.cmdlet`, `variable.parameter`, `variable.splatting`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` and `variable.splatting` are runtime-variable roles here, not named-argument policy. | Fixture row `Covered`; demo sample present. | Keep current roles; map to `G4b`. |
| Protobuf (`protobuf`) | `cross-editor` | Generic: `keyword`, `type`, `function`, `property`, `constant`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`. | Phase 3 | Phase 5 | Phase 3/6 | Enum members currently use root `constant`; Phase 4 rejected dotted enum refinements for Plan 21. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| Python (`python`) | `cross-editor` | Generic: `type`, `function`, `property`, `annotation`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| R (`r`) | `cross-editor` | Generic: `function`, `property`, `variable`, `operator`, `string`, `number`, `comment`; dotted/specific: `keyword.control`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented namespace/formula operators; no tokenizer action. | Fixture row `Covered`; demo sample present. | Phase 5 styling only. |
| Ruby (`ruby`) | `cross-editor` | Generic: `type`, `function`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `function.builtin`, `variable.parameter`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a sigil-variable scope here, not a named-argument policy. | Fixture row `Covered`; demo sample present. | Keep current scope; map to `G4b`. |
| Rust (`rust`) | `cross-editor` | Generic: `type`, `annotation`, `function`, `property`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `function.macro`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 rejected new enum/lifetime refinements for Plan 21; existing `function.macro` is sufficient for optional styling. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| Scala (`scala`) | `JetBrains` | Generic: `annotation`, `type`, `function`, `property`, `variable`, `constant`, `escape`, `string`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `function.builtin`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented annotations, symbol constants, and interpolation escapes; no tokenizer action. | Fixture row `Covered`; demo sample present. | Phase 5/6 styling only. |
| SCSS (`scss`) | `VS Code` | Generic: `tag`, `attribute`, `property`, `variable`, `function`, `string`, `number`, `operator`, `punctuation`, `escape`, `comment`; dotted/specific: `keyword.at-rule`, `attribute.parent-selector`, `attribute.pseudo`, `constant.color`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `variable.scss` to root `variable`, `keyword.scss.<word>` to shared `keyword.at-rule`, and CSS function names to root `function`. | Fixture row `Covered`; demo sample present. | Phase 5/6 styling only. |
| Shell (`shell`) | `cross-editor` | Generic: `function`, `variable`, `string`, `number`, `operator`, `comment`; dotted/specific: `keyword.control`, `function.builtin`, `variable.parameter`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a runtime-variable role here, not a named-argument policy. | Fixture row `Covered`; demo sample present. | Keep current role; map to `G4b`. |
| SQL (`sql`) | `cross-editor` | Generic: `keyword`, `type`, `function`, `string`, `number`, `comment`; dotted/specific: `function.builtin`, `property.quoted`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.sql.<word>` to root `keyword`; use `SyntaxTokenSpan.languageId` for SQL-specific styling. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| SQLite (`sqlite`) | `cross-editor` | Generic: `keyword`, `type`, `function`, `string`, `number`, `comment`; dotted/specific: `function.builtin`, `property.quoted`, `variable.parameter`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | `variable.parameter` is a bind-placeholder role here, not a named-argument or runtime-variable policy. | Fixture row `Covered`; demo sample present. | Keep current role; map to `G4c`. |
| Svelte (`svelte`) | `VS Code` | Generic: component `tag`, `attribute`, `keyword`, `string`, `escape`, `punctuation`; dotted/specific: `attribute.directive`, `markup.expression`; child JS/TS/CSS/SCSS roles by raw-text routing. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.svelte.*` to root `keyword`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| Swift (`swift`) | `Xcode` | Generic: `annotation`, `type`, `function`, `property`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `keyword.wildcard`, `function.builtin`, `variable.namespace`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Phase 4 documented namespace imports; property wrappers/hash directives remain current `annotation` policy. | Fixture row `Covered`; demo sample present. | Phase 5/6 styling only. |
| Terraform (`terraform`) | `cross-editor` | Generic: `keyword`, `type`, `property`, `function`, `string`, `escape`, `number`, `comment`; dotted/specific: `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Plan 23 collapsed `keyword.terraform.<word>` to root `keyword`. | Fixture row `Covered`; demo sample present. | No Plan 23 follow-up. |
| TOML (`toml`) | `cross-editor` | Generic: `string`, `number`, `comment`; dotted/specific: `property.name`, `property.section`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| TSX (`tsx`) | `VS Code` | Generic: TypeScript host roles plus component `tag`, `attribute`, `string`, `escape`, `punctuation`; dotted/specific: `punctuation.expression`. | Phase 3 | Phase 5 | Phase 3/6 | Pure TypeScript construct expansion stays in the TypeScript row. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| TypeScript (`typescript`) | `VS Code` | Generic: `type`, `function`, `property`, `variable`, `escape`, `string`, `number`, `comment`; dotted/specific: `keyword.declaration`, `keyword.control`, `keyword.modifier`, `variable.namespace`, `string.regex`, `constant.builtin.<value>`. | Phase 3 | Phase 5 | Phase 3/6 | Object/type keys and properties share `property`; broader key refinement remains a Plan 22 policy question. | Fixture row `Covered`; demo sample present. | No Plan 21 tokenizer action. |
| Vue (`vue`) | `VS Code` | Generic: component `tag`, `attribute`, `string`, `escape`, `punctuation`; dotted/specific: `attribute.directive`, `markup.expression`; child JS/TS/CSS/LESS/SCSS roles by raw-text routing. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 role gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| XML (`xml`) | `cross-editor` | Generic: `tag`, `attribute`, `string`, `escape`, `comment`, `annotation`; dotted/specific: `markup.cdata`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |
| YAML (`yaml`) | `cross-editor` | Generic: `string`, `number`, `type`, `punctuation`, `comment`; dotted/specific: `property.name`, `string.yaml.block-indicator`, `constant.builtin.<value>`, `variable.yaml.anchor`, `variable.yaml.alias`. | Phase 3 | Phase 5 | Phase 3/6 | No Phase 2 scope gap. | Fixture row `Covered`; demo sample present. | No Phase 2 action. |

## Phase 3 Visual Group Mapping

These mappings are mode-independent group assignments. They do not choose final light or dark colors. When a mapping is ambiguous, the row records a policy question instead of pretending the lexical tokenizer has semantic editor knowledge.

### Calibration Languages

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| Kotlin (`kotlin`) | `keyword.*`, `constant.builtin.*`, and demo-aligned `escape` -> `G1`; `function`, `function.builtin`, `function.declaration`, `function.member` -> `G2`; `property` and plain `variable` -> `G3`; `variable.namespace` import/package paths -> `G3` pending broader namespace policy; `variable.parameter` named arguments -> `G4a`; `type` -> `G0` for JetBrains-like Kotlin/Compose calibration; `annotation` -> `G6`; `string` -> `G7`; `number` -> `G8`; `comment` -> `G10`. | Limit: Kotlin enum entries currently emit `Property`; Phase 4 rejected enum refinements for Plan 21. IDE-like local variable versus property semantics are impossible without parser/semantic analysis. |
| Swift (`swift`) | `keyword.declaration`, `keyword.control`, and `keyword.modifier` -> `G1`; `function` and `function.builtin` -> `G2`; `property`, plain `variable`, projected-value variables, and `variable.namespace` imports -> `G3` pending broader namespace policy; `type` -> `G5`; `annotation` property wrappers and hash directives -> `G6`; `constant.builtin.*` -> `G9`; `escape` -> `G14`; `string` -> `G7`; `number` -> `G8`; `comment` -> `G10`; `keyword.wildcard` -> `G13b` unless Xcode calibration says wildcard markers should follow keywords. | Policy question: verify Xcode-like type styling before choosing whether Swift `type` should move from `G5` toward `G0`. |

### C-Like And JVM Families

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| C (`c`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` struct fields -> `G3`; `type` -> `G5`; `annotation` preprocessor lines -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: enum members currently map to `G9` through root `constant`; Phase 4 rejected dotted enum refinements for Plan 21. |
| C++ (`cpp`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `variable.namespace` namespace paths -> `G3` pending broader namespace policy; `type` -> `G5`; `annotation` preprocessor/attributes -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: Phase 4 rejected dotted enum refinements for Plan 21. Template/type-parameter precision is parser-adjacent unless a scanner can identify declaration contexts lexically. |
| C# (`csharp`) | `keyword.*` -> `G1`; `function` -> `G2`; `property` and `variable.namespace` known framework namespaces -> `G3`; `type` -> `G5`; `annotation` attributes -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Policy question: audience target remains unresolved across Visual Studio, Rider/JetBrains, and VS Code expectations. Exact IDE-like distinction between properties, methods, and fields is impossible with current lexical context. |
| Dart (`dart`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` and lexical field/getter variables -> `G3`; `type` -> `G5`; `annotation` -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` and enum/const identifiers when emitted as `Constant` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: Phase 4 rejected dotted enum refinements for Plan 21. Getter/field declarations that remain lexical variables cannot become IDE-accurate without parser state. |
| GLSL (`glsl`) | `keyword` -> `G1`; `function` and `function.builtin` -> `G2`; `property` swizzles -> `G3`; `type` -> `G5`; `annotation` preprocessor lines -> `G6`; `number` -> `G8`; `comment` -> `G10`. | GLSL has no core string-literal policy today. Distinguishing uniforms, varyings, and locals beyond keyword/type/property roles would require broader language context. |
| Go (`go`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property`, struct fields, and ordinary identifiers that stay lexical -> `G3`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Policy question: `const` declaration identifiers map to `G9`, but Phase 3 does not decide whether named constants should visually differ from built-in literals. |
| Groovy (`groovy`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` and parenthesis-free DSL identifiers that remain lexical -> `G3`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Policy question: Gradle-style DSL calls are intentionally lexical in places; IDE-like callable-vs-property distinction is not reliable without parsing/semantic conventions. |
| Java (`java`) | `keyword.*` -> `G1`; `function` -> `G2`; `property`, `variable`, `variable.namespace`, and field identifiers that remain lexical -> `G3`; `type` -> `G5`; `annotation` -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` and enum constants -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: Java field declarations currently lack a property-specific scanner context, so IDE-like field styling is partial. |
| Objective-C (`objective-c`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `variable`, selector parts, and `property` -> `G3`; `type` -> `G5`; `annotation` `@` forms and preprocessor lines -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: Xcode-like selector-part coloring is not available with current lexical roles and remains deferred. |
| Protobuf (`protobuf`) | `keyword` and `keyword.declaration` -> `G1`; `function` call-like identifiers -> `G2`; `property` fields/options -> `G3`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant` enum members -> `G9`; `comment` -> `G10`. | Limit: enum members currently map to `G9`; Phase 4 rejected dotted enum refinements for Plan 21. |
| Rust (`rust`) | `keyword.*` -> `G1`; `function` and `function.macro` -> `G2`; `property` -> `G3`; lifetimes/labels in `variable` -> `G4b` for now; `type` -> `G5`; `annotation` attributes -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` and enum variants when emitted as `Constant` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: `function.macro` can be styled by optional themes with the existing scope. Lifetimes/labels share `variable`; Phase 4 deferred narrower scopes. |
| Scala (`scala`) | `keyword.*` -> `G1`; `function` and `function.builtin` constructors/calls -> `G2`; `property` and `variable` -> `G3`; `type` -> `G5`; `annotation` -> `G6`; `string` -> `G7`; `constant.builtin.*` and symbol literals -> `G9`; `comment` -> `G10`; interpolation `escape` -> `G14`. | Limit: JetBrains-like neutral type styling could be evaluated later, but Plan 21 keeps Scala `type` in `G5`. |

### Scripting And Application Languages

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| Elixir (`elixir`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` -> `G3`; `type` module aliases -> `G5`; `annotation` module attributes -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.atom` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `operator` -> `G13a`; `escape` -> `G14`. | Limit: Nested interpolation body semantics remain out of contract. |
| Lua (`lua`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property`, `variable`, and `variable.namespace` built-in tables -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | Limit: Label-specific scopes were not approved in Plan 21. |
| Perl (`perl`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `variable.parameter` sigil variables -> `G4b`; `string` and `string.regex` -> `G7`; `number` -> `G8`; `comment` -> `G10`; interpolation/format `escape` -> `G14` when emitted. | Regexes may want distinct optional-theme styling later, but Phase 3 keeps them in string group `G7`. |
| PHP (`php`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` -> `G3`; `variable.parameter` dollar variables -> `G4b`; `type` -> `G5`; `annotation` attributes -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Distinguishing local variables, parameters, and fields is not available from the current lexical scope name. |
| Python (`python`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; `property` and `variable` -> `G3`; `type` built-in and uppercase class-like names -> `G5`; `annotation` decorators -> `G6`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | IDE-like imported-symbol, class-vs-constant, and instance-field distinctions are semantic and out of current tokenizer scope. |
| R (`r`) | `keyword.control` -> `G1`; `function` and `function.builtin` -> `G2`; `property` and `variable` -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `operator` -> `G13a`. | Limit: namespace/formula operators are lexical; no semantic package resolution is implied. |
| Ruby (`ruby`) | `keyword.*` -> `G1`; `function` and `function.builtin` -> `G2`; plain `variable` -> `G3`; `variable.parameter` sigil variables -> `G4b`; `type` uppercase constants -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Ruby constants that represent classes/modules map to `G5` through current `type`; other constant-like symbols stay `G9`. Finer semantic distinction is out of scope. |

### Data, Config, SQL, And Diff

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| Apache config (`apacheconf`) | `keyword` directives -> `G1`; `tag` section names -> `G11`; `variable` arguments -> `G3`; `string` -> `G7`; `comment` -> `G10`; `operator` -> `G13a`; `punctuation` -> `G13b`. | Directive arguments are lexical only; no path, hostname, or address semantic styling is attempted. |
| CSV (`csv`) | `punctuation` delimiters -> `G13b`; unhighlighted cell text remains `G0`. | Demo accent punctuation remains showcase-only until Phase 6. |
| Diff (`diff`) | All diff line roles (`markup.diff.header`, `markup.diff.hunk`, `markup.diff.context`, `markup.diff.deletion`, `markup.diff.addition`, `markup.diff.no-newline`) -> `G15`. | Policy question: additions/deletions often need separate green/red treatment. Phase 3 keeps them under `G15`; Phase 5 or an optional theme may split concrete styles without changing tokenizer roles. |
| DNS zone (`dns-zone`) | `keyword` directives/classes -> `G1`; `property` owner names and `variable` record data -> `G3`; `type` record types -> `G5`; `string` -> `G7`; `number` -> `G8`; `comment` -> `G10`. | DNS field roles are line-position lexical roles, not semantic DNS validation. |
| Dockerfile (`dockerfile`) | `keyword` instructions -> `G1`; `attribute` flags -> `G12` as attribute-like option names; `variable` variables -> `G4b`; `string` -> `G7`; `number` -> `G8`; `operator` -> `G13a`; `punctuation` -> `G13b`; `comment` -> `G10`; routed shell child spans use Shell mapping. | Limit: command flags do not fit the markup-specific wording of `G12` perfectly. Phase 4 rejected a new command-option group for Plan 21. |
| GraphQL (`graphql`) | `keyword` -> `G1`; `function` field calls -> `G2`; `property` selection fields -> `G3`; `variable.parameter` operation variables -> `G4b`; `type` -> `G5`; `annotation` directives -> `G6`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | Field call versus selection-field styling is lexical and context-limited. Schema-aware field/type resolution is out of scope. |
| HCL (`hcl`) | `function` -> `G2`; `property` and plain `variable` -> `G3`; `type` block names -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Limit: object key refinements remain a Plan 22 policy question. Generic block labels do not imply semantic resource types. |
| INI/properties (`ini`) | `keyword` directives -> `G1`; `property` and `property.section` -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | Section headers could be visually structural (`G15`), but Phase 5 keeps them with properties in starter defaults. |
| JSON (`json`) | `property.name` -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `punctuation` -> `G13b`; `escape` -> `G14`. | No semantic object-schema styling is attempted. |
| JSON5 (`json5`) | `property.name` -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`; punctuation, if emitted, -> `G13b`. | JSON5 special numeric names that emit constants remain `G9`, not `G8`. |
| Makefile (`makefile`) | `keyword` directives -> `G1`; `function` targets and recipe commands -> `G2`; `property` assignment names -> `G3`; `variable` Make variables -> `G4b`; `operator` -> `G13a`; `comment` -> `G10`. | Recipe command embedded-language routing remains deferred; current recipe commands are lexical call-like names only. |
| PostgreSQL (`postgresql`) | `keyword` -> `G1`; `function` and `function.builtin` -> `G2`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `comment` -> `G10`; `operator` casts/JSON operators -> `G13a`; bind placeholders, if added/documented later, -> `G4c`. | Language-aware function-body embedded-language routing and bind placeholders are outside current documented role set. |
| SQL (`sql`) | `keyword` -> `G1`; `function` and `function.builtin` -> `G2`; `property.quoted` -> `G3`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | Column/table identifier semantics are not available unless quoted/property context identifies them lexically. |
| SQLite (`sqlite`) | `keyword` -> `G1`; `function` and `function.builtin` -> `G2`; `property.quoted` -> `G3`; `variable.parameter` bind placeholders -> `G4c`; `type` -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | `variable.parameter` means bind placeholder only in this row. Do not generalize it to named arguments. |
| Terraform (`terraform`) | `keyword` -> `G1`; `function` -> `G2`; `property` -> `G3`; `type` block names -> `G5`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `escape` -> `G14`. | Object-expression key refinement remains a Plan 22 policy question. |
| TOML (`toml`) | `property.name` and `property.section` -> `G3`; `string` -> `G7`; `number` date/time and numeric values -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`. | Table headers may benefit from optional structural styling later, but root property inheritance is adequate for starter defaults. |
| YAML (`yaml`) | `property.name` -> `G3`; `variable.yaml.anchor` and `variable.yaml.alias` -> `G4b`; `type` tags -> `G5`; `string` and block scalar body text -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `punctuation` -> `G13b`; `string.yaml.block-indicator` -> `G15` as a structural-style candidate for Phase 5. | Limit: block scalar indicators are emitted as `String` but behave structurally. Phase 4 kept the current scope/category. |

### Markup, Stylesheet, And Component Languages

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| Astro (`astro`) | Component `tag` -> `G11`; `attribute` and `attribute.directive` -> `G12`; `string` -> `G7`; `escape` and expression delimiters -> `G14`; `punctuation` -> `G13b`; `markup.frontmatter` -> `G15`; frontmatter/script/style child spans use their child-language mappings. | Full framework semantics, prop types, and imported component resolution are out of scope. |
| CSS (`css`) | `keyword.at-rule` -> `G1`; `function` -> `G2`; `property` and custom-property declarations -> `G3`; `variable.css.custom-property` references -> `G3`; generic value `variable` -> `G3`; `string` -> `G7`; `number` -> `G8`; `constant.color` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `tag` selectors -> `G11`; `attribute` and `attribute.pseudo` selectors -> `G12`; `operator` -> `G13a`; `punctuation` -> `G13b`. | Limit: custom-property declarations versus references may deserve distinct optional-theme styling, but Phase 4 kept current roles. |
| HTML (`html`) | `tag` -> `G11`; `attribute` -> `G12`; `string` attribute values -> `G7`; `escape` entities -> `G14`; `punctuation` delimiters -> `G13b`; `comment` -> `G10`; `annotation` doctypes/processing forms -> `G6`; script/style child spans use child-language mappings. | Attribute-value embedded-language routing remains out of contract. |
| JSX (`jsx`) | JavaScript host scopes use JavaScript mapping; component/HTML `tag` -> `G11`; `attribute` -> `G12`; `string` -> `G7`; `escape` entities and `punctuation.expression` braces -> `G14`; tag punctuation -> `G13b`. | Imported component recognition is lexical uppercase/tag-position behavior only, not semantic module resolution. |
| Less (`less`) | `keyword.at-rule` -> `G1`; `function` -> `G2`; `property` and generic value `variable` -> `G3`; stylesheet variable references -> `G3` or `G4b` by theme policy; `string` -> `G7`; `number` -> `G8`; `constant.color` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `tag` selectors -> `G11`; `attribute` and `attribute.pseudo` selectors -> `G12`; `operator` -> `G13a`; `punctuation` -> `G13b`. | Starter defaults inherit root `variable`; Phase 6 can refine stylesheet-variable policy for optional themes. |
| MDX (`mdx`) | Markdown host scopes use Markdown mapping; JSX component `tag` -> `G11`; `attribute` -> `G12`; `string` -> `G7`; expression delimiters/entities -> `G14`; `markup.expression` -> `G14`; tag punctuation -> `G13b`; fenced child spans use child-language mappings. | Markdown/JSX boundary roles are lexical. JSX expression internals use JavaScript mapping where delegated. |
| SCSS (`scss`) | `keyword.at-rule` -> `G1`; `function` mixin/function calls -> `G2`; `property` and generic value `variable` -> `G3`; stylesheet variable references -> `G3` or `G4b` by theme policy; `string` -> `G7`; `number` -> `G8`; `constant.color` and `constant.builtin.*` -> `G9`; `comment` -> `G10`; `tag` selectors -> `G11`; `attribute`, `attribute.parent-selector`, and `attribute.pseudo` -> `G12`; `operator` -> `G13a`; `punctuation` -> `G13b`; interpolation `escape` -> `G14`. | Starter defaults inherit root `variable`; Phase 6 can refine stylesheet-variable policy for optional themes. |
| Svelte (`svelte`) | Component `tag` -> `G11`; `attribute` and `attribute.directive` -> `G12`; `string` -> `G7`; `escape` and `markup.expression` delimiters -> `G14`; `punctuation` -> `G13b`; root `keyword` for block/reactive markers -> `G1`; script/style child spans use child-language mappings. | Reactive-label semantics beyond lexical `$:` are out of scope. |
| TSX (`tsx`) | TypeScript host scopes use TypeScript mapping; component/HTML `tag` -> `G11`; `attribute` -> `G12`; `string` -> `G7`; `escape` entities and `punctuation.expression` braces -> `G14`; tag punctuation -> `G13b`. | Generic component versus type-argument ambiguity remains lexical and conservative. |
| Vue (`vue`) | Component `tag` -> `G11`; `attribute` and `attribute.directive` -> `G12`; `string` -> `G7`; `escape` entities and `markup.expression` mustache delimiters -> `G14`; `punctuation` -> `G13b`; script/style child spans use child-language mappings. | Directive semantics and imported component recognition remain lexical. |
| XML (`xml`) | `tag` -> `G11`; `attribute` -> `G12`; `string` attribute values and CDATA body text -> `G7`; `markup.cdata` delimiters -> `G15`; `escape` entities -> `G14`; `comment` -> `G10`; `annotation` processing instructions -> `G6`. | XML intentionally has no raw-text child-language routing. |

### Shells

| Language | Proposed mapping | Policy questions and limits |
|---|---|---|
| Shell (`shell`) | `keyword.control` -> `G1`; `function` and `function.builtin` -> `G2`; plain `variable` expansion contents/bare words -> `G3`; `variable.parameter` dollar variables -> `G4b`; `string` -> `G7`; `number` -> `G8`; `comment` -> `G10`; `operator` flags/operators -> `G13a`; `escape` substitution and expansion delimiters -> `G14`. | Shell command names, aliases, paths, and variables are lexical. Full shell parsing and command resolution are out of scope. |
| PowerShell (`powershell`) | `keyword.control` -> `G1`; `function` and `function.cmdlet` -> `G2`; `variable.parameter` and `variable.splatting` -> `G4b`; `string` -> `G7`; `number` -> `G8`; `constant.builtin.*` -> `G9`; `comment` -> `G10`; `operator` -> `G13a`; `escape` -> `G14`. | Command parameters currently emit as variables in some contexts. A command-option group would need broader policy approval. |

### Phase 3 Cross-Language Limits

| Limit | Mapping impact |
|---|---|
| Parser/semantic identity is unavailable. | Locals versus fields, imported symbols versus package paths, constructor calls versus ordinary functions, and type parameters versus type names are mapped only where lexical context already exposes them. |
| Dotted role names are sometimes overloaded. | `variable.parameter` maps to `G4a`, `G4b`, or `G4c` by language row; Phase 4 rejected narrower emitted roles for Plan 21. |
| Root-role defaults cannot express all mappings. | Kotlin `type -> G0`, Kotlin named arguments, diff additions/deletions, and component expression delimiters are optional-theme or demo candidates unless Phase 5 finds root-role equivalents. |
| Some role catalog rows appeared incomplete. | Phase 4 corrected confirmed `docs/syntax-roles.md` gaps and carried forward a broader exhaustive catalog sweep as separate documentation follow-up. |

## Role Notes

Phase 4 classified the policy gaps below. No tokenizer, scanner, theme, or public API change is approved by this phase.

| Item | Phase 4 classification | Decision and follow-up |
|---|---|---|
| `variable.parameter` overload | `Rejected: current role is sufficient` | Keep the emitted role strings for Plan 21. The policy matrix maps overloaded cases by language to `G4a`, `G4b`, or `G4c`; narrower role names need a later concrete theme/API reason. |
| Enum entries | `Rejected: current role is sufficient` | Preserve current role decisions: several languages emit enum members as `Constant`, while Kotlin enum entries currently emit `Property`. Do not add `constant.enum` or `property.enum` in Plan 21. |
| Object/config keys | `Policy question: needs design decision` | A cross-language `property.name` policy has high reach across data/config and object syntaxes. If approved, spin it into Plan 22 with fixtures rather than folding it into Phase 7. |
| Namespace/import paths | `Deferred: needs parser/semantic analysis` | Existing lexical `variable.namespace` spans are useful and documented where confirmed, but a consistent cross-language styling policy should wait until the scanner limits and examples are audited together. |
| Callable splits | `Deferred: needs parser/semantic analysis` | Existing lexical distinctions such as `function.declaration`, `function.member`, `function.builtin`, and `function.macro` remain usable. Constructor, DSL-call, command-resolution, and semantic callable consistency is deferred. |
| Type parameters | `Deferred: needs parser/semantic analysis` | Type-parameter-specific roles are not approved in Plan 21. Current `Type`/`Variable` roles remain sufficient until a fixture-backed lexical rule is proposed. |
| Labels, lifetimes, and runtime references | `Deferred: needs parser/semantic analysis` | Keep Kotlin labels, Rust lifetimes, Lua labels, shell variables, Make variables, and GraphQL variables in current roles. Grouping can differ by language without adding new role strings. |
| Command flags and options | `Rejected: current role is sufficient` | Keep existing `attribute`, `operator`, `variable`, and command/function roles. A command-option group is too narrow for the starter themes and too incomplete for a bundled policy. |
| Operators and punctuation | `Rejected: current role is sufficient` | Keep `operator` and `punctuation` as broad lexical roles. Phase 5 recommends separate but close starter styles without scanner changes. |
| CSS custom properties | `Rejected: current role is sufficient` | Current behavior is intentional: declarations are `property`; value references are `variable.css.custom-property`. `docs/syntax-roles.md` now states that distinction. |
| YAML block indicators | `Rejected: current role is sufficient` | Keep `string.yaml.block-indicator`; Phase 5 can decide whether its concrete style is string-like or structural. |
| Diff subgroups | `Plan 23 renamed role family` | Existing `markup.diff.*` roles are narrow enough for optional styling. No tokenizer behavior change is needed. |
| Regex strings | `Rejected: current role is sufficient` | Keep `string.regex` as a string subtype. Optional theme styling can diverge later without role work. |
| PostgreSQL bind placeholders | `Rejected: current role is sufficient` | PostgreSQL currently enables dollar-quoted strings but not SQL bind-parameter scanning. Do not document or map PostgreSQL placeholders as `G4c` until a scanner change is explicitly approved. |
| Java fields, Objective-C selector parts, Groovy DSL calls, shell command resolution | `Deferred: needs parser/semantic analysis` | These are IDE-polish candidates, not lexical role changes for Plan 21. Revisit only with focused fixtures and a separate implementation slice. |

### Phase 4 Role Catalog Corrections

These were documentation-only corrections for already emitted, fixture/source-backed roles.

| Area | Result |
|---|---|
| CSS custom properties | Corrected `docs/syntax-roles.md` wording so custom-property declarations are `property` and value references are `variable.css.custom-property`; also documented emitted CSS strings, variables, operators, and punctuation. |
| SCSS and Less inherited CSS roles | Documented inherited CSS selector/value roles including `tag`, `attribute`, `attribute.pseudo`, root `function`, constants, strings, numbers, operators, and punctuation where applicable. |
| Dockerfile | Corrected variables from the non-emitted `variable.parameter` row to generic `variable`, documented emitted `number`, `operator`, and `punctuation`, and noted shell child scopes inside suitable `RUN` heredocs. |
| Objective-C | Documented emitted `comment` and source-backed `constant.builtin.<value>`. |
| C++, Swift, Kotlin, Java, and Perl namespace paths | Documented confirmed `variable.namespace` import/package/namespace paths; Java also documents fixture-backed `variable` and `constant` rows. |
| Scala | Documented fixture-backed `annotation`, symbol-literal `constant`, and interpolation `escape` roles. |
| Elixir and R | Documented fixture-backed `operator` roles. |
| PostgreSQL bind placeholders | Rejected as not currently emitted/documented; no `docs/syntax-roles.md` row was added. |
| Broader exhaustive catalog audit | Carried forward as a documentation follow-up. Several shared scanners can emit broad generic `operator`, `punctuation`, and namespace roles beyond the Phase 2/3 mismatch list; that sweep should be deliberate and separate from theme grouping. |

## Policy Question Register

Current blocker assessment:

| Gate | Blocking questions |
|---|---|
| Before starting Phase 4 | None. The questions below were the intended input to Phase 4's role-gap review. |
| Before closing Phase 4 | Resolved by the Phase 4 role notes above. Confirmed catalog mismatches were corrected or explicitly rejected/deferred; no tokenizer implementation is approved by Phase 4. |
| Before Phase 5 default-theme recommendation | Resolved by the Phase 5 recommendation above. No default-theme blocker remains before a later palette implementation slice. |
| Before Phase 6 bundled-theme decision | Resolved: language-aware polish remains demo/host-owned for Plan 21; optional bundled themes and generators are deferred to a separate future plan. |
| Before Phase 7 implementation | Resolved: Phase 7 completed only the approved narrow Markdown heading-level/task-state refinement slice. |

Questions discovered through Phase 3:

| ID | Question | Current blocker status | Next handling |
|---|---|---|---|
| `Q1` | Should `docs/theme-color-policy.md` become permanent documentation? | Not blocking Phase 4. Blocks final Plan 21 maintenance cleanup. | Keep proposed `yes`; add the `AGENTS.md` maintenance hook in Phase 8 if accepted. |
| `Q2` | How opinionated should SyntaxMP be about language-aware themes? | Resolved for Plan 21 Phase 6. | Keep public defaults simple; keep language-aware polish in demo/hosts for now; defer optional bundled themes to a separate plan. |
| `Q3` | Should `DefaultLight` and `DefaultDark` ever include per-language overrides? | Resolved for Plan 21. | No per-language overrides in `DefaultLight` / `DefaultDark` for V1 unless a later explicit plan changes the contract. |
| `Q4` | C# audience target: Visual Studio, Rider/JetBrains, VS Code, or cross-editor? | Not blocking Plan 21. Blocks only a future C# polished theme. | Leave as a policy question for any future optional C# or editor-family theme plan. |
| `Q5` | Kotlin/Compose type styling: should Kotlin `type` map to base (`G0`) in language-aware themes? | Resolved for Plan 21. | Keep the JetBrains-like Kotlin behavior as demo/host polish; do not fold it into defaults. |
| `Q6` | Swift type styling: should Swift `type` remain distinct (`G5`) or move toward base (`G0`) under Xcode-like policy? | Not blocking Plan 21. Blocks only future Swift/Xcode polished theme work. | Verify against Xcode expectations in a later optional-theme plan. |
| `Q7` | Should `Variable` and `Property` share a base default style? | Resolved for Phase 5. | Share one starter-theme style. |
| `Q8` | Should global `Type` remain distinct or move closer to base? | Resolved for Phase 5. | Keep `Type` distinct globally; Kotlin `type -> base` remains optional/language-aware policy. |
| `Q9` | Should `Keyword`, `Escape`, `Constant`, and `Annotation` stay visually distinct or share warmer groups? | Resolved for Phase 5. | Keep `Keyword`, `Constant`, and `Annotation` distinct; `Escape` shares with `Keyword` in starter themes. |
| `Q10` | Should operators and structural punctuation remain visually grouped? | Resolved for Phase 5. | Keep them close, with operators slightly stronger and punctuation muted. |
| `Q11` | Should `variable.parameter` remain overloaded or split into narrower emitted roles? | Not blocking Phase 5. Phase 4 rejected a Plan 21 role split. | Keep current roles; map by language to `G4a`, `G4b`, or `G4c`. Reopen only with a later fixture-backed implementation plan. |
| `Q12` | Should enum entries gain `constant.enum` or `property.enum` refinements? | Not blocking Phase 5. Phase 4 rejected a Plan 21 enum refinement. | Preserve current role decisions unless a later theme/API plan proves durable value. |
| `Q13` | Should object/config keys consistently use `property.name` instead of plain `property`? | Not blocking Phase 5. Still a design question for broad role work. | Carry to Plan 22 if approved; too wide for Plan 21 Phase 7. |
| `Q14` | Should namespace/import/package paths consistently use a role such as `variable.namespace`? | Not blocking Phase 5. Cross-language policy deferred. | `docs/syntax-roles.md` documents confirmed rows; a broader namespace audit remains separate. |
| `Q15` | Should function declarations, member calls, constructors, built-ins, and macros be consistently split where lexical scanners can do so? | Not blocking Phase 5. Broader callable consistency deferred. | Keep existing lexical splits; parser-adjacent callable polish waits for a later plan. |
| `Q16` | Should type parameters get a narrower role than generic `Type` where lexical context identifies them? | Not blocking Phase 5. Deferred. | No type-parameter role work in Plan 21. |
| `Q17` | Are command flags/options a separate style group, or should they stay with attributes (`G12`) or variables? | Not blocking Phase 5. Phase 4 rejected a new command-option group for Plan 21. | Keep existing roles; optional command polish needs a later targeted plan. |
| `Q18` | Should YAML `string.yaml.block-indicator` map structurally (`G15`) or stay string-like (`G7`)? | Resolved for starter defaults. | Inherit `String` in defaults; optional data/config themes may style it structurally. |
| `Q19` | Should diff additions/deletions/hunks split into separate visual subgroups instead of all `G15`? | Resolved for Plan 21. | Defaults inherit root `Markup`; demo/showcase themes should split additions/deletions with existing `markup.diff.addition` and `markup.diff.deletion` roles. |
| `Q20` | Should regex string roles such as `string.regex` get distinct optional-theme styling? | Not blocking Phase 4. Blocks optional theme nuance only. | Keep in `G7` unless Phase 6 approves language-aware polish. |
| `Q21` | Should CSS custom-property declarations and references have separate visual policy? | Not blocking Phase 5. Phase 4 clarified docs and rejected new roles. | Declarations stay `property`; references stay `variable.css.custom-property`. |
| `Q22` | Should stylesheet variables map with runtime variables (`G4b`) or properties/identifiers (`G3`)? | Resolved for starter defaults. Blocks stylesheet polished-theme mapping only. | Current SCSS/Less variables inherit root `variable`; Phase 6 can refine optional themes. |
| `Q23` | Should TOML/INI section headers map as structural markup (`G15`) or property-like identifiers (`G3`)? | Resolved for starter defaults. Blocks data/config polished-theme nuance only. | Inherit `Property` in defaults; optional themes can revisit structural styling. |
| `Q24` | Should Rust lifetimes/labels, Lua labels, and similar lexical labels get narrower roles? | Not blocking Phase 5. Deferred. | Keep current roles until a parser-aware or clearly lexical role plan exists. |
| `Q25` | Should Rust `function.macro` remain callable (`G2`) or get macro-specific styling? | Not blocking Phase 5. Current role is sufficient. | Optional Rust theme styling can use existing `function.macro`; no scanner change needed. |
| `Q26` | Should Go `const` declaration identifiers visually differ from built-in literals? | Not blocking Phase 4. Blocks Go polished-theme nuance. | Phase 5/6 mapping review; role changes only if durable value is proven. |
| `Q27` | Should Gradle/Groovy parenthesis-free DSL calls get callable styling? | Not blocking Phase 5. Deferred. | Parser/semantic-adjacent unless a stable lexical rule is proposed later. |
| `Q28` | Should Java field declarations get property-like role? | Not blocking Phase 5. Deferred. | Java variables are documented; IDE-like field/property distinction waits for a later plan. |
| `Q29` | Should Objective-C selector parts get Xcode-like styling beyond generic variables/properties? | Not blocking Phase 5. Deferred. | Deeper selector parsing is outside Plan 21. |
| `Q30` | Should PostgreSQL bind placeholders be documented/mapped like SQLite `G4c` placeholders? | Not blocking Phase 5. Rejected for current behavior. | PostgreSQL bind placeholders are not currently emitted; no docs row or mapping was added. |
| `Q31` | Should shell command names, aliases, paths, and PowerShell command parameters gain narrower roles? | Not blocking Phase 5. Deferred. | Command resolution and parameter refinement need a later plan. |
| `Q32` | Are demo overrides allowed to remain as showcase-only policies when they do not match library defaults? | Resolved for Plan 21 Phase 6. | Yes. Demo overrides may remain when they showcase language-aware policy that is intentionally not in the starter defaults. |
| `Q33` | Which `docs/syntax-roles.md` rows are under-documented? | Not blocking Phase 5. Phase 4 corrected confirmed mismatches and carried forward a broader catalog sweep. | Keep future exhaustive catalog cleanup separate from theme grouping unless a new language/role slice touches the same rows. |
| `Q34` | Should demo samples be expanded where showcase coverage diverges from fixture coverage? | Not blocking Phase 4. Blocks demo polish only. | Track in Phase 8 or a demo-specific follow-up; demo tests must not assert tokenizer correctness. |

## Default And Bundled Theme Recommendations

Phase 5 recommends this starter-theme structure for implementation in a later palette slice:

- Keep `SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` as simple root-role-first starter themes.
- Include explicit global `SyntaxRoleStyles` entries for every current root role: `Keyword`, `String`, `Number`, `Comment`, `Function`, `Type`, `Property`, `Variable`, `Operator`, `Punctuation`, `Annotation`, `Tag`, `Attribute`, `Constant`, `Escape`, and `Markup`.
- Keep `languageOverrides = emptyMap()` in both library defaults.
- Do not add starter defaults for `constant.builtin`, `variable.parameter`, `markup.diff.*`, `string.regex`, `string.yaml.block-indicator`, or any other dotted role in Plan 21 Phase 5.
- Let dotted roles inherit root styles through the Plan 23 cascade. Optional or demo themes can use parent prefixes such as `markup.diff`, exact paths such as `markup.diff.addition`, or language-specific maps keyed by `SyntaxLanguageId`.
- Do not add a syntax base style. Host `TextStyle` remains the only source of base text styling.
- Keep comments regular-weight and non-italic in the starter defaults. Host themes can opt into italic comments with a custom `SyntaxStyle`.

Phase 5 default grouping decisions:

| Decision | Recommendation |
|---|---|
| `Property` / `Variable` | Share one starter style. |
| `Type` / host base text | Keep `Type` distinct globally; Kotlin `type -> base` remains demo or optional-theme policy. |
| `Keyword` / `Escape` | Share in starter themes. |
| `Keyword` / `Constant` / `Annotation` | Keep distinct. |
| `Operator` / `Punctuation` | Keep close, with operators stronger and punctuation muted. |
| `Tag` / `Attribute` | Keep distinct. |
| `Markup` child roles | Use one root `Markup` default; Markdown and diff child splits are optional-theme/demo policy. |
| `variable.parameter` overload | Do not split in defaults; language-aware themes can map Kotlin named arguments, shell variables, and SQL binds differently. |

Copy/override ergonomics expected from Plan 23:

- One global role change uses `theme.withRoleStyle(SyntaxRole.Function, SyntaxStyle(...))`.
- One precise child-role change uses `SyntaxRole.of("markup.diff.addition")` or a known constant where one exists.
- One language-specific change uses `theme.withLanguageRoleStyle(SyntaxLanguageId.Kotlin, SyntaxRole.Variable.Parameter, style)`.
- A sparse language override should contain only the roles that differ from the global starter theme. Parent role styles still cascade root-to-exact before language overrides apply.
- Hosts should pass raw labels to `SyntaxTokenizerEngine.tokenize(...)` or use `engine.resolveLanguageId(label)` when they need the canonical `SyntaxLanguageId` before tokenization; defaults should use `SyntaxLanguageId.*` constants.

Phase 6 records this optional bundled theme decision:

- `SyntaxTheme.DefaultLight` and `SyntaxTheme.DefaultDark` stay root-role-first starter themes for V1.
- The starter defaults keep `languageOverrides = emptyMap()` and do not ship child-role or per-language polish.
- SyntaxMP does not ship a full per-language bundled theme matrix in Plan 21.
- Demo/showcase themes may use child roles and language overrides to make representative languages look polished. Kotlin, diff, Markdown, CSV, and similar showcase policies can remain demo-owned when they would make the public defaults too opinionated.
- Optional named bundled themes, editor-family themes, and seed-color/theme-generator APIs are deferred to a separate future plan. That plan should define naming, contrast expectations, maintenance scope, language coverage, and whether generated themes are root-only or language-aware.

Phase 6 policy split:

| Policy area | Phase 5 recommendation | Later handling |
|---|---|---|
| Kotlin JetBrains polish | Do not fold into `DefaultLight` / `DefaultDark`. | Keep demo/host-owned for Plan 21; reconsider in a future optional named theme. |
| Swift/Xcode polish | Do not block starter themes. | Defer to a future Swift/Xcode or editor-family theme plan. |
| Diff additions/deletions | No child-role defaults. | Demo/showcase should split `markup.diff.addition` and `markup.diff.deletion`; future optional themes may do the same. |
| Markdown structural roles | No child-role defaults. | Demo/showcase may style structural roles; Phase 7 heading/task refinements are available for custom/demo/future optional themes. |
| Regex strings, YAML block indicators, CSS custom properties | Inherit root styles. | Optional language-aware themes may refine later. |
| C# audience target | Not needed for starter defaults. | Defer until a future optional C# or editor-family theme plan. |

Phase 6 role verification and scope follow-up:

| Item | Result |
|---|---|
| Diff additions/deletions | Verified in source and fixtures: Diff emits `markup.diff.addition` and `markup.diff.deletion`, so demo styling can target them without tokenizer work. |
| Markdown heading levels | Phase 7 complete: Markdown emits `markup.heading.h1` through `markup.heading.h6`, preserving fallback through `markup.heading` and `markup`. |
| Markdown task state | Phase 7 complete: Markdown emits `markup.task.checked` and `markup.task.unchecked`, preserving fallback through `markup.task` and `markup`. |
| Starter themes | Do not add default child-role styles for the new Markdown refinements; they exist for custom/demo/future optional themes. |

Implementation notes for the later palette slice:

- Preserve the current baseline snapshot in this document when changing code.
- Apply the Phase 5 proposed light/dark root-role values to `DefaultLight` and `DefaultDark`.
- Keep `languageOverrides = emptyMap()` in both library defaults.
- Add focused theme tests for the approved root-role map, language override precedence, and root-to-exact cascade behavior.
- Add demo/showcase styling for existing diff addition/deletion roles so the demo does not render diffs as one undifferentiated markup color.
- Validate contrast on representative light and dark host surfaces before landing exact colors.
- Re-check red/green dependence for constants/strings and diff optional themes; do not rely on hue alone for any future diagnostic or stateful styling.

## Maintenance

Future built-in languages should add or update a row in the Language Matrix during the same slice that updates `scopes.md` and `fixture-coverage.md`. `AGENTS.md` records this maintenance hook.

## Audit Log

- 2026-05-24 - Plan 21 Phase 1 draft: created the theme-policy skeleton, current starter-theme baseline, initial audience calibration, proposed group vocabulary, demo override snapshot, and policy-decision rules. Plan 21 completion is pending later phases.
- 2026-05-24 - Plan 21 Phase 2 draft: filled the built-in language scope inventory, recorded generic versus dotted/specific scopes, confirmed fixture rows and demo sample registration for all 54 built-in languages, and flagged doc/scope mismatches for later review.
- 2026-05-24 - Plan 21 Phase 3 draft: added mode-independent visual group mappings for every built-in language, kept final color/default-theme decisions deferred, and recorded semantic limits plus policy questions for overloaded scopes, enum refinements, command flags, and incomplete scope catalog rows.
- 2026-05-24 - Plan 21 Phase 3 bookkeeping: added a policy-question register with blocker gates and updated the Plan 21 phase text so Phase 4 explicitly owns `docs/syntax-roles.md` confirmation and documentation-only catalog corrections.
- 2026-05-24 - Plan 21 Phase 4 draft: classified scope-gap questions, rejected/deferred Plan 21-inappropriate tokenizer changes, corrected confirmed `docs/syntax-roles.md` catalog gaps, and recorded that no Phase 4 scope blocker remains before Phase 5.
- 2026-05-24 - Plan 21 Phase 4.5 draft: added category-level and dotted-scope usage summaries, including language-family notes, subscoping counts, and Phase 3/4 style-sharing pressure to guide the Phase 5 default-theme recommendation.
- 2026-05-24 - Plan 21 Phase 5 direction note: recorded reviewer direction for default starter grouping questions and captured the requirement to preserve current baseline columns while adding proposed light/dark columns for theme recommendations.
- 2026-05-24 - Category/scope compatibility note: documented the new-category admission bar and versioning expectations for category and scope changes after V1.
- 2026-05-24 - Plan 21 Phase 5 rewrite note: deferred the default-theme recommendation until Plan 23 settles the unified role/palette/theme model and recorded that final starter-theme proposals should be role-based, not old category-slot recommendations.
- 2026-05-25 - Plan 21 Phase 4.6 inventory refresh: updated active root and dotted-role decision tables for the settled Plan 23 role model, moved superseded language-name role paths to a historical migration table, corrected CSS/SCSS/Less function roles to root `function`, and corrected Makefile variables to root `variable`.
- 2026-05-25 - Plan 21 Phase 5 recommendation: recorded the root-role-first starter theme structure, proposed light/dark values, no child-role defaults, no language overrides in library defaults, Plan 23 override ergonomics, and deferred optional language-aware/diff/Markdown polish to Phase 6 or later theme plans.
- 2026-05-25 - Plan 21 Phase 6 decision: kept `DefaultLight` and `DefaultDark` as simple root-role-first starter themes with no language overrides for V1, deferred optional bundled themes and theme generators to a future plan, kept polished child-role/language-aware styling in demo/showcase policy, verified diff addition/deletion roles are already emitted, and recorded Markdown heading-level/task-state role refinements for Phase 7.
- 2026-05-25 - Plan 21 Phase 7 Markdown role refinement: implemented and documented `markup.heading.h1` through `markup.heading.h6` plus `markup.task.checked` and `markup.task.unchecked`, with fixture coverage and parent-role fallback preserved for custom, demo, and future optional themes.
- 2026-05-25 - Plan 21 Phase 8 palette and demo follow-through: applied the approved Phase 5 root-role starter colors to `DefaultLight` and `DefaultDark`, kept both defaults free of language overrides, added demo-owned diff addition/deletion styling, expanded starter/demo theme tests, made `docs/theme-color-policy.md` permanent, and added the `AGENTS.md` maintenance hook.
- 2026-05-25 - Plan 21 Phase 8 demo diff follow-up: extended the demo-owned diff override to style `markup.diff.header` and `markup.diff.hunk`, so `---` / `+++` file header lines and hunk ranges are visually distinct without adding child-role styles to the library defaults.
- 2026-05-25 - Plan 21 Phase 8 demo Kotlin follow-up: tuned the demo-owned Kotlin override so function roles use a muted azure while `type` and `variable.parameter` use the demo text-primary color for JetBrains-like Kotlin/Compose readability.
- 2026-05-25 - Plan 21 Phase 8 demo Kotlin color tuning: adjusted the dark Kotlin function color to `#649EEE` from reviewer RGB direction and muted the Kotlin near-base text override to avoid bright-white named arguments/types in the demo.
- 2026-05-25 - Plan 21 Phase 8 light-mode contrast decision: recorded original, proposed, and decided current starter-theme colors side by side; accepted the higher-contrast light palette after demo review while keeping dark-mode values stable.
- 2026-05-25 - Plan 21 Phase 8 Swift projected-value follow-up: documented that Swift projected-value variables such as `$note` map to the existing `variable` / `G3` policy; no new theme role or host base-text color policy was added.
