package com.gallatinapps.syntaxmp.demo.model.samples

internal val MarkdownSample = """
    # Browser Indexing Notes

    The first scan should feel **instant** for small libraries and _honest_
    for larger folders. Inline code like `ReadableTextMaxBytes` should sit
    beside [reference links][policy] and escaped punctuation like \*literal\*.

    <span data-owner="docs">SyntaxMP</span> also highlights inline HTML.

    ## Checklist

    - [x] Restore library grants before scanning
    - [x] Hash editable text files after extraction
    - [ ] Keep image entries metadata-only
    1. Reconcile missing files
    2. Rebuild search rows

    > Keep user-authored files as the source of truth.
    > Nested details can stay in the same quote block.

    | Kind | Limit | Action |
    | :--- | ----: | :----- |
    | Markdown | 256 KB | rich edit |
    | Plain text | 2 MB | chunked preview |

    ```kotlin
    val policy = LargeDocumentPolicy(
        readableTextMaxBytes = 2_000_000,
        enhancedEditMaxBytes = 256_000,
    )
    val label = "${'$'}{policy.enhancedEditMaxBytes / 1024} KB"
    ```

    <details open>
      <summary>HTML block</summary>
      <p data-state="draft">Inline <strong>markup</strong> is tokenized too.</p>
    </details>

    ---

    [policy]: https://example.test/docs/policy "Large document policy"
""".trimIndent()
