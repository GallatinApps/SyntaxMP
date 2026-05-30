# Adding a Language Extension

This guide is for hosts that want SyntaxMP to highlight a language it doesn't ship out of the box. You register a `SyntaxLanguageExtension` and your tokenizer runs inside the same engine that drives the built-ins, with the same `SyntaxTokenSpan` output, the same normalization pass, and the same `SyntaxTheme` resolution.

If you're contributing a tokenizer *to the library itself* rather than adding one in a host app, the maintainer-facing SOP in [docs/internal/adding-a-built-in-language.md](internal/adding-a-built-in-language.md) is the deeper reference.

## When to add a language

Two paths:

- **Custom extension (this doc).** Your tokenizer lives in your application, registered on the engine your app constructs. No library change. Best for proprietary DSLs, niche languages, or per-deployment overrides.
- **New built-in.** Open a contribution against SyntaxMP itself so every consumer picks the language up. Worthwhile only when the language is broadly useful and there's room to keep it maintained.

This doc covers the extension path.

## The `SyntaxTokenizer` contract

```kotlin
fun interface SyntaxTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult
}

class SyntaxTokenizeRequest(
    val code: String,
    val languageId: SyntaxLanguageId,
) {
    fun tokenizeEmbedded(code: String, languageLabel: String): List<SyntaxTokenSpan>
}

data class SyntaxTokenizeResult(
    val spans: List<SyntaxTokenSpan> = emptyList(),
)

data class SyntaxTokenSpan(
    val start: Int,
    val endExclusive: Int,
    val role: SyntaxRole,
    val languageId: SyntaxLanguageId,
)
```

Four rules:

- **UTF-16 offsets, half-open.** `start..<endExclusive` over `request.code`. Spans that fall outside the code length, have `endExclusive <= start`, or overflow are clipped or dropped by the engine's normalizer; you don't need to defend against them yourself, but you should aim for cleanly bounded spans.
- **Exception-safe by design.** If your tokenizer throws, the engine catches the failure and returns an empty span list for that call. Buggy tokenizers can never crash text rendering, but a silently-throwing tokenizer also produces no highlighting, so add tests. If you want logging, wrap your tokenizer body in `try/catch` and return `SyntaxTokenizeResult()` on failure.
- **Stateless across calls.** The engine shares one tokenizer across compositions and threads. Keep per-call state on the stack; don't mutate instance fields.
- **Embedded routing is request-scoped.** Tokenizers called by `SyntaxTokenizerEngine` can call `request.tokenizeEmbedded(code, languageLabel)` to route a child region through the same engine. Requests you construct manually with `SyntaxTokenizeRequest(code, languageId)` return an empty list from that method because they are not bound to an engine.

The engine also runs a `normalizer` pass on whatever you return: spans may overlap, sit out of order, or be adjacent. At each character position the smallest / most-specific span wins, and adjacent runs of the same role *and* language are merged. This means you can emit a broad span and then narrower spans on top, and the normalizer will resolve precedence deterministically.

## Worked example: a small `myql` tokenizer

The example below highlights a tiny made-up SQL-like language: `--` line comments, single-quoted strings with `''` escape, a small keyword set, decimal numbers, and a language-specific directive prefix (`@@directive`) the host wants themed distinctly.

```kotlin
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine

// One canonical id for the language; share this constant everywhere the language
// is referenced (engine extension, language overrides in the theme, host UI).
val MyqlLanguage: SyntaxLanguageId = SyntaxLanguageId.fromString("myql")

// Custom dotted role for the @@directive form, anchored under the built-in
// `keyword` root so themes that style `SyntaxRole.Keyword` cover this role for
// free. A myql-specific override on `keyword.myql.directive` can refine the look
// without breaking the fallback.
val MyqlDirectiveRole: SyntaxRole = SyntaxRole.Keyword.append("myql.directive")

private val MyqlKeywords: Set<String> = setOf(
    "select", "from", "where", "and", "or", "not", "as", "limit",
)

val MyqlTokenizer = SyntaxTokenizer { request ->
    val code = request.code
    val languageId = request.languageId
    val spans = mutableListOf<SyntaxTokenSpan>()

    fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            spans += SyntaxTokenSpan(
                start = start,
                endExclusive = end,
                role = role,
                languageId = languageId,
            )
        }
    }

    var i = 0
    while (i < code.length) {
        val char = code[i]
        when {
            // Line comment: `-- ...` to end of line.
            char == '-' && i + 1 < code.length && code[i + 1] == '-' -> {
                val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                add(i, end, SyntaxRole.Comment)
                i = end
            }
            // Single-quoted string with `''` escape.
            char == '\'' -> {
                var j = i + 1
                while (j < code.length) {
                    if (code[j] == '\'') {
                        if (j + 1 < code.length && code[j + 1] == '\'') {
                            j += 2 // escaped quote
                        } else {
                            j += 1
                            break
                        }
                    } else {
                        j += 1
                    }
                }
                add(i, j, SyntaxRole.String)
                i = j
            }
            // Directive: `@@name` (letters and digits after `@@`).
            char == '@' && i + 1 < code.length && code[i + 1] == '@' -> {
                var j = i + 2
                while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) {
                    j += 1
                }
                add(i, j, MyqlDirectiveRole)
                i = j
            }
            // Decimal number (very loose: digits with optional decimal point).
            char.isDigit() -> {
                var j = i + 1
                while (j < code.length && (code[j].isDigit() || code[j] == '.')) {
                    j += 1
                }
                add(i, j, SyntaxRole.Number)
                i = j
            }
            // Identifier or keyword.
            char.isLetter() || char == '_' -> {
                var j = i + 1
                while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) {
                    j += 1
                }
                val word = code.substring(i, j).lowercase()
                val role = if (word in MyqlKeywords) {
                    SyntaxRole.Keyword
                } else {
                    SyntaxRole.Variable
                }
                add(i, j, role)
                i = j
            }
            else -> i += 1
        }
    }

    SyntaxTokenizeResult(spans)
}

val engine = SyntaxTokenizerEngine(
    extensions = listOf(
        SyntaxLanguageExtension(
            languageId = MyqlLanguage,
            aliases = setOf("mql"),
            tokenizer = MyqlTokenizer,
        ),
    ),
)
```

A few things the example is doing on purpose:

- **One canonical `SyntaxLanguageId` constant.** `SyntaxLanguageId.fromString("myql")` trims and lowercases its input but does not resolve aliases; it just creates an exact language id. Share the constant everywhere so theme overrides and tokenizer output agree on a single value.
- **Every span carries `languageId = request.languageId`.** Stamping each span with the resolved language id is how `SyntaxTheme.languageOverrides` keyed on `MyqlLanguage` knows to apply.
- **Anchor custom roles under existing roots when you can.** `SyntaxRole.Keyword.append("myql.directive")` produces a `keyword.myql.directive` role that automatically inherits any global `keyword` style through the resolver's parent walk. New custom languages get sensible default styling under `SyntaxTheme.DefaultLight` / `DefaultDark` and under any host theme that styles the built-in roots. Refine with a per-language override on the dotted path when you want a distinct look.
- **`SyntaxRole.of(...)` for genuinely standalone roles.** `SyntaxRole.of("custom.myql.directive")` produces a fully custom role with that exact dotted path. There is no parent-role fallback under any built-in root, so themes that don't explicitly style it contribute no spans. Use `SyntaxRole.of(...)` when you specifically want no cascade, typically when the extension ships its own theme.
- **Overlapping spans are fine.** The example happens to produce non-overlapping spans, but if your tokenizer found it cleaner to emit a broad `string` and then narrower escape spans inside, the normalizer would pick the narrow spans where they apply and the broad span everywhere else.

## Embedding another language from an extension

This example highlights `{{ ... }}` blocks in a tiny template language by routing their body to JavaScript. `request.tokenizeEmbedded(...)` returns child spans relative to the child string, so the extension offsets them into the parent document before returning.

```kotlin
val TinyTemplateLanguage: SyntaxLanguageId = SyntaxLanguageId.fromString("tiny-template")

val TinyTemplateTokenizer = SyntaxTokenizer { request ->
    val code = request.code
    val spans = mutableListOf<SyntaxTokenSpan>()
    var i = 0

    while (i < code.length) {
        val open = code.indexOf("{{", startIndex = i)
        if (open == -1) break

        val close = code.indexOf("}}", startIndex = open + 2)
        if (close == -1) {
            spans += SyntaxTokenSpan(
                start = open,
                endExclusive = code.length,
                role = SyntaxRole.Punctuation.Expression,
                languageId = request.languageId,
            )
            break
        }

        val bodyStart = open + 2
        val body = code.substring(bodyStart, close)

        spans += SyntaxTokenSpan(
            start = open,
            endExclusive = bodyStart,
            role = SyntaxRole.Punctuation.Expression,
            languageId = request.languageId,
        )
        request.tokenizeEmbedded(body, "javascript").forEach { child ->
            spans += child.copy(
                start = bodyStart + child.start,
                endExclusive = bodyStart + child.endExclusive,
            )
        }
        spans += SyntaxTokenSpan(
            start = close,
            endExclusive = close + 2,
            role = SyntaxRole.Punctuation.Expression,
            languageId = request.languageId,
        )

        i = close + 2
    }

    SyntaxTokenizeResult(spans)
}
```

The embedded call uses the engine's normal language lookup: extensions first, then built-ins. If JavaScript is disabled for that engine, or if the child label is unknown, the call returns an empty list.

## Registering aliases

`SyntaxLanguageExtension.aliases` is for labels that should resolve to the extension's `languageId`.
They work at the top-level engine boundary and in routed embedded-language labels such as Markdown fence
info strings and markup raw-text `lang=` values.

In the example above, registering `"mql"` means a Markdown fence opened with ` ```mql ` will route its body through the `myql` tokenizer:

```markdown
```mql
select * from users where id = @@current_user;
```
```

Top-level raw labels can be passed directly to `SyntaxTokenizerEngine.tokenize`. Use `engine.resolveLanguageId(label)` only when you need the canonical `SyntaxLanguageId` before tokenizing, such as for diagnostics or a language-specific UI affordance.

The same alias also works when a built-in host already exposes a raw-label route. For example,
`<script lang="mql">...</script>` or `<style lang="mql">...</style>` can route through the extension
if that markup context is appropriate for your custom tokenizer. SyntaxMP does not expose public
hooks for adding new host tags, new attribute routes, or custom tag defaults; those require
a custom host tokenizer.

## Overriding a built-in language

Extensions resolve **before** built-ins. Registering a `SyntaxLanguageExtension` whose `languageId` is a built-in `SyntaxLanguageId` causes your tokenizer to handle that language for this engine instance, bypassing the built-in tokenizer.

```kotlin
val engine = SyntaxTokenizerEngine(
    extensions = listOf(
        SyntaxLanguageExtension(
            languageId = SyntaxLanguageId.Kotlin,
            tokenizer = MyCustomKotlinTokenizer,
        ),
    ),
)
```

Useful when you need a domain-specific dialect or want to test an experimental tokenizer against the rest of your app without forking the library.

## Theming a custom language

Per-language overrides live on `SyntaxTheme.languageOverrides`. The map key is the same `SyntaxLanguageId` value your tokenizer stamps on each span, so the lookup is exact:

```kotlin
val theme = SyntaxTheme.DefaultDark
    .withLanguageRoleStyles(
        languageId = MyqlLanguage,
        styles = SyntaxRoleStyles(
            // Restyle the global `keyword` role only for myql.
            SyntaxRole.Keyword to SyntaxStyle(
                color = Color(0xFF7F52FF),
                fontWeight = FontWeight.Bold,
            ),
            // Style the custom directive role declared above.
            MyqlDirectiveRole to SyntaxStyle(
                color = Color(0xFF9333EA),
                fontWeight = FontWeight.SemiBold,
            ),
        ),
    )
```

Resolution still merges the global `roleStyles` first (so `SyntaxRole.String` from `DefaultDark` is the green it always was), then layers the language override on top. See [docs/theming.md](theming.md) for the full resolution policy.

## Testing your tokenizer

SyntaxMP doesn't expose a public fixture helper today, so test with `kotlin.test` against the engine. Construct the engine the same way your app does (extensions and all), tokenize a small snippet, and assert on the resulting span list:

```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MyqlTokenizerTest {

    private val engine = SyntaxTokenizerEngine(
        extensions = listOf(
            SyntaxLanguageExtension(
                languageId = MyqlLanguage,
                tokenizer = MyqlTokenizer,
            ),
        ),
    )

    @Test
    fun `select is keyword`() {
        val code = "select 1"
        val spans = engine.tokenize(code = code, languageLabel = MyqlLanguage.value)
        val select = spans.first { code.substring(it.start, it.endExclusive) == "select" }
        assertEquals(SyntaxRole.Keyword, select.role)
        assertEquals(MyqlLanguage, select.languageId)
    }

    @Test
    fun `directive uses custom role`() {
        val code = "select @@current_user"
        val spans = engine.tokenize(code = code, languageLabel = MyqlLanguage.value)
        val directive = spans.first { code.substring(it.start, it.endExclusive) == "@@current_user" }
        assertEquals(MyqlDirectiveRole, directive.role)
    }

    @Test
    fun `keywords inside a string stay a string`() {
        val code = "'select'"
        val spans = engine.tokenize(code = code, languageLabel = MyqlLanguage.value)
        val keywordSpans = spans.filter { it.role == SyntaxRole.Keyword }
        assertTrue(keywordSpans.isEmpty(), "no keyword spans expected inside a string")
    }
}
```

Cover the cases that matter for your tokenizer's contract. At minimum each role you emit, each literal form (comments, strings, numbers), and one negative assertion proving keywords/comment markers inside strings don't escape.

## Where to go next

- [docs/syntax-roles.md](syntax-roles.md): the roles primer. Root and refinement constants and the `of`/`append` factories for custom roles.
- [docs/languages.md](languages.md): the per-language catalog of roles SyntaxMP's built-in tokenizers emit. Reuse those role identities where they fit and your custom language will pick up everyone's global theming for free.
- [docs/theming.md](theming.md): the full theming surface, including the four copy/override helpers and the resolution policy your spans will go through.
- [docs/embedded-languages.md](embedded-languages.md): what SyntaxMP routes automatically (HTML script/style, Markdown fences, JSX/TSX/MDX/Vue/Svelte/Astro) and what it deliberately doesn't.
- [docs/internal/adding-a-built-in-language.md](internal/adding-a-built-in-language.md): the maintainer-facing SOP for contributing a built-in tokenizer back to the library.
