package com.gallatinapps.syntaxmp.languages.diff

import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class DiffScanner(
    private val code: String,
    private val language: LanguageId,
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var lineStart = 0
        while (lineStart <= code.length) {
            val lineEnd = code.indexOf('\n', lineStart).let { if (it == -1) code.length else it }
            scanLine(lineStart, lineEnd)
            if (lineEnd == code.length) break
            lineStart = lineEnd + 1
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int) {
        val line = code.substring(start, end)
        val role = when {
            line.startsWith("diff --git") ||
                line.startsWith("index ") ||
                line.startsWith("---") ||
                line.startsWith("+++") ->
                DiffRole.Header
            line.startsWith("+") -> DiffRole.Addition
            line.startsWith("-") -> DiffRole.Deletion
            line.isMetadataHeaderLine() -> DiffRole.Header
            line.startsWith("@@") -> DiffRole.Hunk
            line.startsWith("\\ No newline") -> DiffRole.NoNewline
            line.startsWith(" ") -> DiffRole.Context
            else -> null
        }
        if (role != null) {
            add(start, end, role)
        }
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}

private object DiffRole {
    private val Root = SyntaxRole.Markup.append("diff")

    val Header = Root.append("header")
    val Hunk = Root.append("hunk")
    val Context = Root.append("context")
    val Deletion = Root.append("deletion")
    val Addition = Root.append("addition")
    val NoNewline = Root.append("no-newline")
}

private fun String.isMetadataHeaderLine(): Boolean =
    startsWith("rename from ") ||
        startsWith("rename to ") ||
        startsWith("copy from ") ||
        startsWith("copy to ") ||
        startsWith("similarity index ") ||
        startsWith("dissimilarity index ") ||
        startsWith("deleted file mode ") ||
        startsWith("new file mode ") ||
        startsWith("old mode ") ||
        startsWith("new mode ") ||
        startsWith("Binary files ") ||
        this == "GIT binary patch"
