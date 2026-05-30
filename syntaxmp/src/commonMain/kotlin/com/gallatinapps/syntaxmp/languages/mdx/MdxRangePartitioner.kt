package com.gallatinapps.syntaxmp.languages.mdx

import com.gallatinapps.syntaxmp.engine.primitives.findBraceBalancedEnd

internal data class MdxRangePartition(
    val esmRanges: List<MdxSourceRange>,
    val componentRanges: List<MdxSourceRange>,
    val expressionRanges: List<MdxSourceRange>,
    val markdownRanges: List<MdxSourceRange>,
)

internal data class MdxSourceRange(
    val start: Int,
    val endExclusive: Int,
)

internal fun partitionMdxRanges(code: String): MdxRangePartition {
    val fenceRanges = findFenceRanges(code)
    val esmRanges = findMdxEsmRanges(code = code, fenceRanges = fenceRanges)
    val componentRanges = findComponentRanges(
        code = code,
        skippedRanges = (fenceRanges + esmRanges).sortedBy { it.start },
    )
    val expressionRanges = findExpressionRanges(
        code = code,
        skippedRanges = (fenceRanges + esmRanges + componentRanges).sortedBy { it.start },
    )
    val nonMarkdownRanges = (esmRanges + componentRanges + expressionRanges)
        .sortedWith(compareBy<MdxSourceRange> { it.start }.thenBy { it.endExclusive })
    return MdxRangePartition(
        esmRanges = esmRanges,
        componentRanges = componentRanges,
        expressionRanges = expressionRanges,
        markdownRanges = complementRanges(code.length, nonMarkdownRanges),
    )
}

private fun findFenceRanges(code: String): List<MdxSourceRange> =
    buildList {
        var lineStart = 0
        var fence: MdxFence? = null
        var fenceStart = -1
        while (lineStart <= code.length) {
            val lineEnd = lineEnd(code, lineStart)
            val trimmedStart = firstNonWhitespace(code, lineStart, lineEnd)
            val lineFence = mdxFenceAt(code, trimmedStart, lineEnd)
            if (lineFence != null) {
                if (fence == null) {
                    fence = lineFence
                    fenceStart = lineStart
                } else if (lineFence.closes(fence)) {
                    add(MdxSourceRange(start = fenceStart, endExclusive = lineEnd))
                    fence = null
                    fenceStart = -1
                }
            }
            if (lineEnd == code.length) {
                if (fence != null && fenceStart >= 0) {
                    add(MdxSourceRange(start = fenceStart, endExclusive = code.length))
                }
                break
            }
            lineStart = lineEnd + 1
        }
    }

private fun findMdxEsmRanges(
    code: String,
    fenceRanges: List<MdxSourceRange>,
): List<MdxSourceRange> =
    buildList {
        var lineStart = 0
        while (lineStart <= code.length) {
            val lineEnd = lineEnd(code, lineStart)
            val trimmedStart = firstNonWhitespace(code, lineStart, lineEnd)
            if (
                !fenceRanges.containsIndex(trimmedStart) &&
                (
                    code.startsWith(MdxImportKeyword, trimmedStart) ||
                        code.startsWith(MdxExportKeyword, trimmedStart)
                )
            ) {
                add(MdxSourceRange(start = trimmedStart, endExclusive = lineEnd))
            }
            if (lineEnd == code.length) break
            lineStart = lineEnd + 1
        }
    }

private fun findComponentRanges(
    code: String,
    skippedRanges: List<MdxSourceRange>,
): List<MdxSourceRange> =
    buildList {
        var index = 0
        while (index < code.length) {
            val skipped = skippedRanges.rangeContaining(index)
            if (skipped != null) {
                index = skipped.endExclusive.coerceAtLeast(index + 1)
                continue
            }
            if (code[index] == '`') {
                index = skipInlineCode(code = code, start = index, end = lineEnd(code, index))
                continue
            }
            val range = parseComponentRange(code = code, start = index)
            if (range != null) {
                add(range)
                index = range.endExclusive
            } else {
                index++
            }
        }
    }

private fun findExpressionRanges(
    code: String,
    skippedRanges: List<MdxSourceRange>,
): List<MdxSourceRange> =
    buildList {
        var index = 0
        while (index < code.length) {
            val skipped = skippedRanges.rangeContaining(index)
            if (skipped != null) {
                index = skipped.endExclusive.coerceAtLeast(index + 1)
                continue
            }
            if (code[index] == '`') {
                index = skipInlineCode(code = code, start = index, end = lineEnd(code, index))
                continue
            }
            if (code[index] == '{') {
                val close = findBraceBalancedEnd(code = code, openIndex = index)
                val endExclusive = if (close < code.length) close + 1 else code.length
                add(MdxSourceRange(start = index, endExclusive = endExclusive))
                index = endExclusive
            } else {
                index++
            }
        }
    }

private fun parseComponentRange(
    code: String,
    start: Int,
): MdxSourceRange? {
    val opening = parseComponentOpeningTag(code = code, start = start) ?: return null
    if (opening.selfClosing) {
        return MdxSourceRange(start = start, endExclusive = opening.endExclusive)
    }
    val closingEnd = findClosingComponentTagEnd(
        code = code,
        opening = opening,
        start = opening.endExclusive,
    ) ?: opening.endExclusive
    return MdxSourceRange(start = start, endExclusive = closingEnd)
}

private fun parseComponentOpeningTag(
    code: String,
    start: Int,
): MdxComponentOpeningTag? {
    if (code.getOrNull(start) != '<') return null
    val next = code.getOrNull(start + 1) ?: return null
    if (next == '>') {
        return MdxComponentOpeningTag(
            tagName = "",
            endExclusive = start + 2,
            selfClosing = false,
        )
    }
    if (!next.isUpperCase()) return null
    var index = start + 1
    val nameStart = index
    while (index < code.length && code[index].isJsxNamePart()) index++
    if (index == nameStart) return null
    val endExclusive = findTagEnd(code = code, start = index) ?: return null
    return MdxComponentOpeningTag(
        tagName = code.substring(nameStart, index),
        endExclusive = endExclusive,
        selfClosing = endExclusive >= 2 && code[endExclusive - 2] == '/',
    )
}

private fun findClosingComponentTagEnd(
    code: String,
    opening: MdxComponentOpeningTag,
    start: Int,
): Int? {
    if (opening.tagName.isEmpty()) {
        val closingStart = code.indexOf("</>", start)
        return if (closingStart == -1) null else closingStart + 3
    }

    var depth = 1
    var cursor = start
    while (cursor < code.length) {
        val tagStart = code.indexOf('<', cursor)
        if (tagStart == -1) return null
        when {
            code.startsWith("<!--", tagStart) -> {
                cursor = code.indexOf("-->", tagStart + 4).let { if (it == -1) code.length else it + 3 }
            }
            code.startsWith("</", tagStart) -> {
                val nameStart = tagStart + 2
                val nameEnd = readJsxNameEnd(code = code, start = nameStart)
                val tagEnd = findTagEnd(code = code, start = nameEnd) ?: return null
                if (code.substring(nameStart, nameEnd) == opening.tagName) {
                    depth--
                    if (depth == 0) return tagEnd
                }
                cursor = tagEnd
            }
            else -> {
                val nested = parseComponentOpeningTag(code = code, start = tagStart)
                if (nested != null) {
                    if (!nested.selfClosing && nested.tagName == opening.tagName) {
                        depth++
                    }
                    cursor = nested.endExclusive
                } else {
                    cursor = tagStart + 1
                }
            }
        }
    }
    return null
}

private fun findTagEnd(
    code: String,
    start: Int,
): Int? {
    var index = start
    var quote: Char? = null
    var braceDepth = 0
    while (index < code.length) {
        val char = code[index]
        if (quote != null) {
            if (char == '\\') {
                index = (index + 2).coerceAtMost(code.length)
                continue
            }
            if (char == quote) quote = null
        } else {
            when (char) {
                '"', '\'', '`' -> quote = char
                '{' -> braceDepth++
                '}' -> if (braceDepth > 0) braceDepth--
                '>' -> if (braceDepth == 0) return index + 1
            }
        }
        index++
    }
    return null
}

private fun readJsxNameEnd(
    code: String,
    start: Int,
): Int {
    var index = start
    while (index < code.length && code[index].isJsxNamePart()) index++
    return index
}

private fun complementRanges(
    length: Int,
    occupiedRanges: List<MdxSourceRange>,
): List<MdxSourceRange> =
    buildList {
        var cursor = 0
        occupiedRanges.forEach { range ->
            if (range.start > cursor) {
                add(MdxSourceRange(start = cursor, endExclusive = range.start))
            }
            cursor = cursor.coerceAtLeast(range.endExclusive)
        }
        if (cursor < length) {
            add(MdxSourceRange(start = cursor, endExclusive = length))
        }
    }

private fun skipInlineCode(
    code: String,
    start: Int,
    end: Int,
): Int {
    var markerEnd = start
    while (markerEnd < end && code[markerEnd] == '`') markerEnd++
    val marker = code.substring(start, markerEnd)
    val closing = code.indexOf(marker, markerEnd).takeIf { it != -1 && it < end }
    return closing?.plus(marker.length) ?: markerEnd
}

private fun List<MdxSourceRange>.containsIndex(index: Int): Boolean =
    any { index >= it.start && index < it.endExclusive }

private fun List<MdxSourceRange>.rangeContaining(index: Int): MdxSourceRange? =
    firstOrNull { index >= it.start && index < it.endExclusive }

private fun lineEnd(code: String, start: Int): Int =
    code.indexOf('\n', start).let { if (it == -1) code.length else it }

private fun firstNonWhitespace(
    code: String,
    start: Int,
    endExclusive: Int,
): Int {
    var index = start
    while (index < endExclusive && code[index].isWhitespace()) index++
    return index
}

private data class MdxComponentOpeningTag(
    val tagName: String,
    val endExclusive: Int,
    val selfClosing: Boolean,
)

private data class MdxFence(
    val marker: Char,
    val length: Int,
) {
    fun closes(opening: MdxFence): Boolean =
        marker == opening.marker && length >= opening.length
}

private fun mdxFenceAt(
    code: String,
    start: Int,
    endExclusive: Int,
): MdxFence? {
    val marker = code.getOrNull(start)
    if (marker != '`' && marker != '~') return null
    var index = start
    while (index < endExclusive && code[index] == marker) index++
    val length = index - start
    return if (length >= 3) MdxFence(marker = marker, length = length) else null
}

private fun Char.isJsxNamePart(): Boolean =
    isLetterOrDigit() || this == '_' || this == '-' || this == ':' || this == '.'
