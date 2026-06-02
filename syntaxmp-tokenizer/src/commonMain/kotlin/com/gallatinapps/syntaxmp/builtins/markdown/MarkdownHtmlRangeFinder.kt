package com.gallatinapps.syntaxmp.builtins.markdown

internal class MarkdownHtmlRangeFinder(
    private val code: String,
) {
    fun findInlineRange(start: Int, endExclusive: Int): MarkdownHtmlRange? {
        val opening = parseOpeningTag(start, endExclusive) ?: return null
        val htmlEnd = if (opening.selfClosing || opening.tagName.isEmpty()) {
            opening.endExclusive
        } else {
            findMatchingClosingTagEnd(
                tagName = opening.tagName,
                start = opening.endExclusive,
                endExclusive = endExclusive,
            ) ?: opening.endExclusive
        }
        return MarkdownHtmlRange(
            start = start,
            endExclusive = htmlEnd,
            resumeAt = htmlEnd,
        )
    }

    fun findBlockRange(
        start: Int,
        lineStart: Int,
        lineEnd: Int,
        endExclusive: Int,
        canInterruptParagraph: Boolean,
    ): MarkdownHtmlRange? {
        if (start >= lineEnd || code[start] != '<') return null
        if (start - lineStart > 3) return null
        return findSpecialTagBlock(start, lineEnd, endExclusive)
            ?: findDelimitedBlock(start, lineEnd, endExclusive, opener = "<!--", closer = "-->")
            ?: findDelimitedBlock(start, lineEnd, endExclusive, opener = "<?", closer = "?>")
            ?: findDeclarationBlock(start, lineEnd, endExclusive)
            ?: findDelimitedBlock(start, lineEnd, endExclusive, opener = "<![CDATA[", closer = "]]>")
            ?: findBlockTagRange(start, lineEnd, endExclusive)
            ?: if (canInterruptParagraph) {
                findCompleteTagRange(start, lineEnd, endExclusive)
            } else {
                null
            }
    }

    private fun findSpecialTagBlock(start: Int, lineEnd: Int, endExclusive: Int): MarkdownHtmlRange? {
        specialBlockTagAt(start, lineEnd) ?: return null
        val endLineEnd = findLineContainingHtmlEndTag(
            start = start,
            endExclusive = endExclusive,
            tagNames = MarkdownSpecialHtmlBlockTags,
        ) ?: lineEndAt(endExclusive, endExclusive)
        return lineRange(start = start, endLineEnd = endLineEnd, endExclusive = endExclusive)
    }

    private fun findDelimitedBlock(
        start: Int,
        lineEnd: Int,
        endExclusive: Int,
        opener: String,
        closer: String,
    ): MarkdownHtmlRange? {
        if (!code.startsWith(opener, start)) return null
        val endLineEnd = findLineContaining(
            start = start,
            endExclusive = endExclusive,
            value = closer,
        ) ?: lineEndAt(endExclusive, endExclusive)
        return lineRange(start = start, endLineEnd = endLineEnd, endExclusive = endExclusive)
    }

    private fun findDeclarationBlock(start: Int, lineEnd: Int, endExclusive: Int): MarkdownHtmlRange? {
        if (!code.startsWith("<!", start)) return null
        val declarationStart = start + 2
        if (declarationStart >= lineEnd || code[declarationStart] !in 'A'..'Z') return null
        val endLineEnd = findLineContaining(
            start = start,
            endExclusive = endExclusive,
            value = ">",
        ) ?: lineEndAt(endExclusive, endExclusive)
        return lineRange(start = start, endLineEnd = endLineEnd, endExclusive = endExclusive)
    }

    private fun findBlockTagRange(start: Int, lineEnd: Int, endExclusive: Int): MarkdownHtmlRange? {
        val tagName = blockTagAt(start, lineEnd) ?: return null
        if (tagName !in MarkdownHtmlBlockTags) return null
        return blankTerminatedRange(start = start, firstLineEnd = lineEnd, endExclusive = endExclusive)
    }

    private fun findCompleteTagRange(start: Int, lineEnd: Int, endExclusive: Int): MarkdownHtmlRange? {
        if (!isCompleteTagLine(start, lineEnd)) return null
        return blankTerminatedRange(start = start, firstLineEnd = lineEnd, endExclusive = endExclusive)
    }

    private fun specialBlockTagAt(start: Int, lineEnd: Int): String? {
        if (start + 1 >= lineEnd || code[start] != '<') return null
        val tagStart = start + 1
        val tagName = MarkdownSpecialHtmlBlockTags.firstOrNull { tag ->
            code.regionMatches(tagStart, tag, 0, tag.length, ignoreCase = true)
        } ?: return null
        val afterName = tagStart + tagName.length
        if (afterName > lineEnd) return null
        if (afterName == lineEnd || code[afterName].isWhitespace() || code[afterName] == '>') {
            return tagName
        }
        return null
    }

    private fun blockTagAt(start: Int, lineEnd: Int): String? {
        if (start + 1 >= lineEnd || code[start] != '<') return null
        var index = start + 1
        if (index < lineEnd && code[index] == '/') index++
        val tagStart = index
        while (index < lineEnd && code[index].isHtmlNamePart()) index++
        if (index == tagStart) return null
        val tagName = code.substring(tagStart, index).lowercase()
        return if (hasBlockTagBoundary(index, lineEnd)) tagName else null
    }

    private fun hasBlockTagBoundary(index: Int, lineEnd: Int): Boolean =
        index == lineEnd ||
            code[index].isWhitespace() ||
            code[index] == '>' ||
            (code[index] == '/' && index + 1 < lineEnd && code[index + 1] == '>')

    private fun isCompleteTagLine(start: Int, lineEnd: Int): Boolean {
        if (start + 1 >= lineEnd || code[start] != '<') return false
        val isClosing = code[start + 1] == '/'
        val nameStart = if (isClosing) start + 2 else start + 1
        if (nameStart >= lineEnd || !code[nameStart].isLetter()) return false
        var nameEnd = nameStart
        while (nameEnd < lineEnd && code[nameEnd].isHtmlNamePart()) nameEnd++
        val tagName = code.substring(nameStart, nameEnd).lowercase()
        if (tagName in MarkdownSpecialHtmlBlockTags) return false
        val tagEnd = findTagEnd(nameEnd, lineEnd) ?: return false
        return code.substring(tagEnd, lineEnd).isBlank()
    }

    private fun parseOpeningTag(start: Int, endExclusive: Int): HtmlOpeningTag? {
        if (start + 1 >= endExclusive || code[start] != '<' || !code[start + 1].isLetter()) return null
        var index = start + 1
        val tagStart = index
        while (index < endExclusive && code[index].isHtmlNamePart()) index++
        if (index == tagStart) return null
        val tagName = code.substring(tagStart, index).lowercase()
        val tagEnd = findTagEnd(index, endExclusive) ?: return null
        val selfClosing = tagEnd >= 2 && code[tagEnd - 2] == '/'
        return HtmlOpeningTag(
            tagName = tagName,
            endExclusive = tagEnd,
            selfClosing = selfClosing,
        )
    }

    private fun findMatchingClosingTagEnd(
        tagName: String,
        start: Int,
        endExclusive: Int,
    ): Int? {
        var cursor = start
        while (cursor < endExclusive) {
            val closingStart = code.indexOf("</", cursor)
            if (closingStart == -1 || closingStart >= endExclusive) return null
            var nameStart = closingStart + 2
            while (nameStart < endExclusive && code[nameStart].isWhitespace()) nameStart++
            var nameEnd = nameStart
            while (nameEnd < endExclusive && code[nameEnd].isHtmlNamePart()) nameEnd++
            if (code.substring(nameStart, nameEnd).lowercase() == tagName) {
                return findTagEnd(nameEnd, endExclusive)
            }
            cursor = closingStart + 2
        }
        return null
    }

    private fun findLineContainingHtmlEndTag(
        start: Int,
        endExclusive: Int,
        tagNames: Set<String>,
    ): Int? {
        var lineStart = start
        while (lineStart <= endExclusive) {
            val lineEnd = lineEndAt(lineStart, endExclusive)
            if (lineContainsHtmlEndTag(lineStart, lineEnd, tagNames)) {
                return lineEnd
            }
            if (lineEnd >= endExclusive) return null
            lineStart = lineEnd + 1
        }
        return null
    }

    private fun lineContainsHtmlEndTag(start: Int, end: Int, tagNames: Set<String>): Boolean {
        var cursor = start
        while (cursor < end) {
            val closingStart = code.indexOf("</", cursor)
            if (closingStart == -1 || closingStart >= end) return false
            var nameStart = closingStart + 2
            while (nameStart < end && code[nameStart].isWhitespace()) nameStart++
            var nameEnd = nameStart
            while (nameEnd < end && code[nameEnd].isHtmlNamePart()) nameEnd++
            val name = code.substring(nameStart, nameEnd).lowercase()
            if (name in tagNames && findTagEnd(nameEnd, end) != null) {
                return true
            }
            cursor = closingStart + 2
        }
        return false
    }

    private fun findLineContaining(start: Int, endExclusive: Int, value: String): Int? {
        var lineStart = start
        while (lineStart <= endExclusive) {
            val lineEnd = lineEndAt(lineStart, endExclusive)
            if (code.indexOf(value, startIndex = lineStart).let { it != -1 && it < lineEnd }) {
                return lineEnd
            }
            if (lineEnd >= endExclusive) return null
            lineStart = lineEnd + 1
        }
        return null
    }

    private fun blankTerminatedRange(
        start: Int,
        firstLineEnd: Int,
        endExclusive: Int,
    ): MarkdownHtmlRange {
        var currentLineEnd = firstLineEnd
        var nextLineStart = nextLineStart(currentLineEnd, endExclusive) ?: endExclusive
        while (nextLineStart < endExclusive) {
            val nextLineEnd = lineEndAt(nextLineStart, endExclusive)
            if (code.substring(nextLineStart, nextLineEnd).isBlank()) {
                return MarkdownHtmlRange(
                    start = start,
                    endExclusive = currentLineEnd,
                    resumeAt = nextLineStart,
                )
            }
            currentLineEnd = nextLineEnd
            nextLineStart = nextLineStart(currentLineEnd, endExclusive) ?: endExclusive
        }
        return MarkdownHtmlRange(
            start = start,
            endExclusive = endExclusive,
            resumeAt = endExclusive,
        )
    }

    private fun lineRange(start: Int, endLineEnd: Int, endExclusive: Int): MarkdownHtmlRange =
        MarkdownHtmlRange(
            start = start,
            endExclusive = endLineEnd,
            resumeAt = nextLineStart(endLineEnd, endExclusive) ?: endLineEnd,
        )

    private fun nextLineStart(lineEnd: Int, endExclusive: Int): Int? =
        if (lineEnd < endExclusive && code[lineEnd] == '\n') lineEnd + 1 else null

    private fun lineEndAt(start: Int, endExclusive: Int): Int {
        val next = code.indexOf('\n', startIndex = start)
        return if (next == -1 || next > endExclusive) endExclusive else next
    }

    private fun findTagEnd(start: Int, endExclusive: Int): Int? {
        var index = start
        var quote: Char? = null
        while (index < endExclusive) {
            val char = code[index]
            if (quote != null) {
                if (char == quote) quote = null
            } else if (char == '"' || char == '\'') {
                quote = char
            } else if (char == '>') {
                return index + 1
            }
            index++
        }
        return null
    }
}

internal data class MarkdownHtmlRange(
    val start: Int,
    val endExclusive: Int,
    val resumeAt: Int,
)

private data class HtmlOpeningTag(
    val tagName: String,
    val endExclusive: Int,
    val selfClosing: Boolean,
)

private fun Char.isHtmlNamePart(): Boolean =
    isLetterOrDigit() || this == '_' || this == '-' || this == ':' || this == '.'
