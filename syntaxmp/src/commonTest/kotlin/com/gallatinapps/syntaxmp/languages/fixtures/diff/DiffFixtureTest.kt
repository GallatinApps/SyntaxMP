package com.gallatinapps.syntaxmp.languages.fixtures.diff

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class DiffFixtureTest {
    private val code = """
        diff --git a/README.md b/README.md
        index 1111111..2222222 100644
        --- a/README.md
        +++ b/README.md
        @@ -1,3 +1,4 @@
         # Title
        -old line
        +new line
    """.trimIndent()

    @Test
    fun `diff headers hunks deletions and additions`() {
        assertTokenAt("diff",
            code,
            "diff --git a/README.md b/README.md",
            "Markup",
            "markup.diff.header",
        )
        assertTokenAt("diff", code, "index 1111111..2222222 100644", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "--- a/README.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "+++ b/README.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "@@ -1,3 +1,4 @@", "Markup", "markup.diff.hunk")
        assertTokenAt("diff", code, " # Title", "Markup", "markup.diff.context")
        assertTokenAt("diff", code, "-old line", "Markup", "markup.diff.deletion")
        assertTokenAt("diff", code, "+new line", "Markup", "markup.diff.addition")
        assertNoTokenAt("diff", code, "new", "String")
        assertNoTokenAt("diff", code, "old", "Comment")
        assertNoTokenAt("diff", code, "Title", "Keyword")
        assertNoTokenAt("diff", code, "1111111", "Number")
    }

    @Test
    fun `diff no newline marker has a dedicated scope`() {
        val code = """
            @@ -1 +1 @@
            -old
            \ No newline at end of file
        """.trimIndent()

        assertTokenAt("diff", code, "\\ No newline at end of file", "Markup", "markup.diff.no-newline")
    }

    @Test
    fun `diff rename copy mode similarity and binary metadata are headers`() {
        val code = """
            diff --git a/old-name.md b/new-name.md
            similarity index 92%
            dissimilarity index 8%
            rename from old-name.md
            rename to new-name.md
            copy from template.md
            copy to copied.md
            deleted file mode 100644
            new file mode 100755
            old mode 100644
            new mode 100755
            Binary files a/foo.bin and b/foo.bin differ
            GIT binary patch
        """.trimIndent()

        assertTokenAt("diff", code, "similarity index 92%", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "dissimilarity index 8%", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "rename from old-name.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "rename to new-name.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "copy from template.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "copy to copied.md", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "deleted file mode 100644", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "new file mode 100755", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "old mode 100644", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "new mode 100755", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "Binary files a/foo.bin and b/foo.bin differ", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "GIT binary patch", "Markup", "markup.diff.header")
    }

    @Test
    fun `diff metadata phrases inside changed or context lines keep line kind priority`() {
        val code = """
            @@ -1,3 +1,3 @@
            -rename from old-name.md
            +Binary files a/foo.bin and b/foo.bin differ
             rename from remains context
             Binary files remains context
        """.trimIndent()

        assertTokenAt("diff", code, "-rename from old-name.md", "Markup", "markup.diff.deletion")
        assertTokenAt("diff", code, "+Binary files a/foo.bin and b/foo.bin differ", "Markup", "markup.diff.addition")
        assertTokenAt("diff", code, " rename from remains context", "Markup", "markup.diff.context")
        assertTokenAt("diff", code, " Binary files remains context", "Markup", "markup.diff.context")
        assertNoTokenAt("diff", code, "rename from remains context", "Markup", "markup.diff.header")
        assertNoTokenAt("diff", code, "Binary files remains context", "Markup", "markup.diff.header")
    }

    @Test
    fun `multi file diffs keep line scoped boundaries`() {
        val code = """
            diff --git a/first.txt b/first.txt
            --- a/first.txt
            +++ b/first.txt
            @@ -0,0 +1,0 @@
            +created
            \ No newline at end of file
            diff --git a/second.txt b/second.txt
            --- a/second.txt
            +++ b/second.txt
            @@ -2 +2 @@
            -old
            +new
        """.trimIndent()

        assertTokenAt("diff", code, "diff --git a/first.txt b/first.txt", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "@@ -0,0 +1,0 @@", "Markup", "markup.diff.hunk")
        assertTokenAt("diff", code, "+created", "Markup", "markup.diff.addition")
        assertTokenAt("diff", code, "\\ No newline at end of file", "Markup", "markup.diff.no-newline")
        assertTokenAt("diff", code, "diff --git a/second.txt b/second.txt", "Markup", "markup.diff.header")
        assertTokenAt("diff", code, "-old", "Markup", "markup.diff.deletion")
        assertTokenAt("diff", code, "+new", "Markup", "markup.diff.addition")
    }

    @Test
    fun `diff markers are line position sensitive`() {
        val code = """
            context +plus is text
            context -minus is text
        """.trimIndent()

        assertNoTokenAt("diff", code, "+plus is text", "Markup", "markup.diff.addition")
        assertNoTokenAt("diff", code, "-minus is text", "Markup", "markup.diff.deletion")
    }
}
