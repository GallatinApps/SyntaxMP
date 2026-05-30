# Embedded Languages

Some languages embed other languages: HTML hosts CSS and JavaScript, Markdown hosts whatever its fenced-code labels point at, and Vue/Svelte/Astro single-file components host both. SyntaxMP routes the common cases automatically. This doc covers what is routed, what is not, and what either fact means for theme authors and tokenizer extensions.

Embedded-language routing is engine-owned. Built-in hosts call back through the same tokenizer engine that handles top-level calls, and extension tokenizers can use the public `request.tokenizeEmbedded(code, languageLabel)` affordance when they need to embed a child language.

## What gets routed automatically

| Host language | What is routed | Default embedded language | Notes |
|---|---|---|---|
| HTML | `<script>` body | JavaScript | Omitted `lang` routes to JavaScript. Explicit `lang` values route through the engine resolver, so built-in labels such as `"ts"` / `"typescript"`, `"tsx"`, `"jsx"`, and extension aliases can route custom languages. Unknown explicit `lang` values produce no embedded highlighting; register an extension to handle them. |
| HTML | `<style>` body | CSS | Omitted `lang` routes to CSS. Explicit `lang` values route through the engine resolver, so built-in labels such as `"scss"`, `"sass"`, and `"less"` work, and extension aliases can route custom languages. Unknown explicit `lang` values produce no embedded highlighting; register an extension to handle them. |
| JSX | `<script>` / `<style>` bodies (same rules as HTML) | JS / CSS | Expression islands (`{ ... }`) route as JavaScript. |
| TSX | `<script>` / `<style>` bodies (same rules as HTML) | JS / CSS | Expression islands route as TypeScript. |
| Vue | `<script>` / `<style>` bodies (same rules as HTML) | JS / CSS | Expression islands (`{{ ... }}`, `v-...`) route as JavaScript. |
| Svelte | `<script>` / `<style>` bodies (same rules as HTML) | JS / CSS | Expression islands route as JavaScript. |
| Astro | `<script>` / `<style>` bodies (same rules as HTML) | JS / CSS | Expression islands route as JavaScript; `---`-delimited frontmatter routes as TypeScript. |
| Markdown | Fenced code body | Fence label's resolved language | Fence labels are matched against registered extensions first (by language id or `aliases`), then resolved through built-in aliases such as `ts` for TypeScript. |
| Markdown | HTML blocks | HTML | |
| MDX | Fenced code body | Fence label's resolved language | Same extension-then-built-in routing as Markdown. |
| MDX | JSX expression islands | JavaScript | |
| MDX | HTML blocks | HTML | |
| Dockerfile | `RUN <<EOF` and `RUN sh/bash <<EOF` heredoc bodies | Shell | Generic non-RUN heredocs stay as `String`. |

The routing depth is capped at three levels: a Markdown fence inside HTML inside JSX all the way down to a JavaScript expression will not recurse forever. Tokenizer exceptions in the routed embedded language are caught the same way as top-level ones; they degrade to no spans without crashing the host tokenization.

## `lang=` awareness in `<script>` and `<style>`

The built-in resolver looks at the tag name and the optional `lang` attribute:

```html
<script setup lang="ts">
  // tokenized as TypeScript
  const message: string = "hello"
</script>

<style lang="scss">
  /* tokenized as SCSS */
  .root { color: $brand; }
</style>
```

If `lang` is omitted, the default for the tag wins: `<script>` routes as JavaScript and `<style>` routes as CSS.

When `lang` is present, the value is routed through the same engine resolver as top-level labels and Markdown fences. Built-in aliases, exact extension ids, and extension aliases all work. Unknown explicit labels are not errors, but they produce no embedded spans. A known built-in label whose tokenizer is disabled for the engine also produces no embedded spans.

## What's deliberately not supported

| Case | Why |
|---|---|
| Tagged template literals (`` html`...` ``, `` gql`...` ``, `` css`...` ``) | Reliable detection needs more than lexical context: call-site type info is what tells you which tag triggers which embedded language. SyntaxMP renders these as ordinary template strings with interpolation. |
| HTML attribute scripts and styles (`onclick="..."`, `style="..."`) | Attribute-value embedded-language routing requires attribute-aware lexical plumbing the markup scanner does not carry. Attribute values stay string-like. |
| Template languages embedded in HTML (ERB, Jinja, PHP-in-HTML, Twig, Mustache, ...) | Each one is its own scanning problem and out of SyntaxMP's current scope. Tokenize the file with the template language alone (for example, `SyntaxLanguageId.Php`) where one exists, or with `SyntaxLanguageId.Html` for plain markup pieces. |
| PostgreSQL `LANGUAGE`-aware function bodies | Dollar-quoted strings (`$$ ... $$`, `$tag$ ... $tag$`) are recognized as `String`, but the body is not re-tokenized as the declared `LANGUAGE`. That needs statement-level parser context. |
| General shell heredocs outside Dockerfile RUN | Most heredocs do not carry a reliable embedded-language hint, and inferring one from the delimiter is error-prone. Bodies stay as `String`. |
| Makefile recipe lines as shell | Recipe lines mix Make variable expansion and shell syntax; treating them as plain text is closer to reality than partial routing. |
| Custom host routing rules | Built-in host routing rules are internal. Extensions can participate in existing Markdown fence and `<script>` / `<style lang="...">` routes, and extension tokenizers can call `request.tokenizeEmbedded(...)`, but there is no public API for adding new built-in host tags, new attribute routes, or custom tag defaults. |

If one of these is a deal-breaker for your use case, the usual move is a dedicated tokenizer for the combined language (see [docs/language-extension.md](language-extension.md)).

## What this means for theme authors

An embedded span carries the embedded language's roles and its embedded `languageId: SyntaxLanguageId` field. The roles are not rewritten as the host language's roles, and `SyntaxTokenSpan.languageId` reflects which tokenizer actually produced the span, not which top-level call kicked off tokenization.

The practical consequence: per-language theme overrides apply to the routed embedded language, not the host. An HTML page that embeds CSS will pick up your CSS language override automatically:

```kotlin
val theme = SyntaxTheme.DefaultDark
    .withLanguageRoleStyles(
        languageId = SyntaxLanguageId.Css,
        styles = SyntaxRoleStyles(
            SyntaxRole.Property to SyntaxStyle(color = Color(0xFFFF79C6)),
        ),
    )

BasicText(
    text = rememberSyntaxAnnotatedString(
        code = htmlSource,
        languageLabel = "html",
        engine = engine,
        theme = theme,
    ),
)
// The CSS override fires for spans inside <style> bodies even though
// the top-level language is HTML.
```

The same is true the other direction: theming `SyntaxLanguageId.Html` does not affect the CSS or JS spans inside `<style>` and `<script>`, because their `SyntaxTokenSpan.languageId` is `Css` or `JavaScript`, not `Html`.

## What this means for tokenizer extensions

Built-in hosts route their child labels through the same engine lookup as top-level labels: registered extension ids first, then extension `aliases`, then built-in aliases and ids.

- **Markdown and MDX fences route through the engine's extension lookup.** The fence label is matched against your registered extensions first (by exact language id or by `aliases`) before falling back to built-in aliases. An extension registered for `SyntaxLanguageId.fromString("myql")` is picked up automatically by a `myql` fence inside a Markdown or MDX document; if the same extension declares `aliases = setOf("mql")`, an `mql` fence routes to it too. No host code changes are needed.

- **Markup raw-text `lang=` values use the same lookup.** A `<script lang="myjs">` or `<style lang="mycss">` value can route to an extension that declares `aliases = setOf("myjs")` or `aliases = setOf("mycss")`. Unknown explicit labels produce no embedded spans, so register an extension when a custom label should highlight.

- **Extensions can replace a routed built-in embedded language's tokenizer everywhere.** Extension lookup runs ahead of built-ins at every routing layer, not just at the top level. An extension whose `languageId` is `SyntaxLanguageId.Css` handles the body of every `<style>` block in HTML, Vue, Svelte, Astro, JSX, and TSX, and a `SyntaxLanguageId.JavaScript` extension handles every `<script>` body. Same for `css` and `js` Markdown fences.

- **Extension tokenizers can embed another language.** When your own tokenizer finds a child region, call `request.tokenizeEmbedded(childCode, childLabel)`. The returned spans are relative to `childCode`, so offset them before returning them with the host spans:

```kotlin
val childSpans = request.tokenizeEmbedded(childCode, childLabel).map { span ->
    span.copy(
        start = childStart + span.start,
        endExclusive = childStart + span.endExclusive,
    )
}
```

If you need a built-in host (HTML, Vue, etc.) to add new routing mechanics, such as a new raw-text tag, attribute-value scripts, or a different default for `<script>` / `<style>`, the realistic path today is to fork the host tokenizer into an extension and supply custom scanner options.

## Where to go next

- [docs/language-extension.md](language-extension.md): building a custom tokenizer that can act as a routed embedded language for built-in hosts, or embed another language itself.
- [docs/theming.md](theming.md): language overrides and the resolution policy the theme uses for routed embedded spans.
- [docs/architecture.md](architecture.md): where embedded-language recursion sits in the broader pipeline and how the depth limit works.
- [docs/languages.md](languages.md): the roles each built-in embedded language emits, useful when shaping per-language overrides for embedded content.
