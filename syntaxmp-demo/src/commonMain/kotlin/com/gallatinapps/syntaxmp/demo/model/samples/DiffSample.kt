package com.gallatinapps.syntaxmp.demo.model.samples

internal val DiffSample = """
    diff --git a/docs/browser.md b/docs/browser.md
    index 41ab2ac..9fc612a 100644
    --- a/docs/browser.md
    +++ b/docs/browser.md
    @@ -4,7 +4,10 @@ The browser pane owns library navigation.
    -Single-pane routes open hidden desktop tabs.
    +Single-pane routes replace the active touch document.
    +
    +Desktop tabs remain workspace state, not Navigation destinations.
    +Hash routes are shareable in the demo build.
     Search results are scoped by the current browser content scope.
    \ No newline at end of file
""".trimIndent()
