# Changelog

All notable changes to SyntaxMP will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). SyntaxMP uses semantic versioning, with the usual pre-1.0 caveat that minor releases may still refine public APIs while the library settles.

## [Unreleased]

## [0.3.0] - 2026-06-04

### Added

- Added `SyntaxTokenizer.languageIds` and `SyntaxTokenizer.languageLabels` so hosts can inspect the language ids and raw labels recognized by a tokenizer instance.

### Changed

- **Breaking:** Changed `SyntaxTokenizer.resolveLanguageId(...)` to resolve only labels active for that tokenizer instance. Unknown, disabled, blank, or null labels now resolve to `null` instead of creating custom `LanguageId` values.
- **Breaking:** Moved `SyntaxStyle`, `SyntaxRoleStyles`, and `SyntaxTheme` from `com.gallatinapps.syntaxmp.compose.theme` into `com.gallatinapps.syntaxmp.compose`.
- Built-in language aliases now participate in the tokenizer instance catalog, so disabled built-in aliases are not recognized unless an extension claims them.

### Internal

- Restricted demo deployment to non-prerelease published releases whose stable `vX.Y.Z` tag matches the exact Gradle project version.

## [0.2.0] - 2026-06-02

### Added

- Added `com.gallatinapps.syntaxmp:syntaxmp-tokenizer` as the publishable pure Kotlin tokenizer artifact for token-only and bring-your-own-renderer consumers.

### Changed

- **Breaking:** Kept `com.gallatinapps.syntaxmp:syntaxmp` as the Compose highlighter artifact and split the pure tokenizer layer into `syntaxmp-tokenizer`, with `syntaxmp` api-depending on it.
- **Breaking:** Flattened tokenizer packages out of the old `engine.*` namespace and renamed built-in language packages from `languages.*` to `builtins.*`.
- **Breaking:** Renamed the public tokenizer/language API cluster: `SyntaxTokenizerEngine` -> `SyntaxTokenizer`, `SyntaxTokenizer` -> `LanguageTokenizer`, `SyntaxTokenizeRequest` -> `TokenizeRequest`, `SyntaxLanguageId` -> `LanguageId`, and `SyntaxLanguageExtension` -> `LanguageExtension`.
- **Breaking:** Removed `SyntaxTokenizeResult`; language tokenizers now return `List<SyntaxTokenSpan>` directly.
- Updated the demo, benchmark harness, docs, and public API references for the split module and renamed APIs.

## [0.1.0] - 2026-05-30

### Added

- Initial public release of SyntaxMP.
- 39 built-in language tokenizers.
- Compose helpers for display text, editable text, custom languages, and syntax themes.
- Hosted Kotlin/Wasm demo.

[Unreleased]: https://github.com/GallatinApps/SyntaxMP/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/GallatinApps/SyntaxMP/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/GallatinApps/SyntaxMP/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/GallatinApps/SyntaxMP/releases/tag/v0.1.0
