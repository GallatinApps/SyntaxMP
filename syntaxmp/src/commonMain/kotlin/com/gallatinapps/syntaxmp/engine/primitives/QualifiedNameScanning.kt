package com.gallatinapps.syntaxmp.engine.primitives

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal fun QualifiedNameOptions.roleForIdentifier(
    code: String,
    start: Int,
    word: String,
): SyntaxRole? {
    if (introducers.isEmpty() || word in terminatorKeywords) return null
    return role.takeIf { contextAt(code, start).isInNameContext(start) }
}

internal fun QualifiedNameOptions.markerAt(
    code: String,
    start: Int,
): String? {
    if (introducers.isEmpty() || !contextAt(code, start).isInNameContext(start)) return null
    return markerAt(code, start, markersByDescendingLength)
}

internal class QualifiedNameContextCache(
    private val code: String,
    private val options: QualifiedNameOptions,
) {
    private var cachedContext: QualifiedNameContext? = null

    fun roleForIdentifier(
        start: Int,
        word: String,
    ): SyntaxRole? {
        if (options.introducers.isEmpty() || word in options.terminatorKeywords) return null
        return options.role.takeIf { contextAt(start).isInNameContext(start) }
    }

    fun markerAt(start: Int): String? {
        if (options.introducers.isEmpty() || !contextAt(start).isInNameContext(start)) return null
        return markerAt(code, start, options.markersByDescendingLength)
    }

    private fun contextAt(index: Int): QualifiedNameContext {
        val cached = cachedContext
        if (cached != null) {
            if (cached.contains(index)) return cached
            if (index > cached.segmentEnd) {
                return options.contextFromSegmentStart(
                    code = code,
                    segmentStart = segmentStartAfter(cached.segmentEnd, index),
                    index = index,
                ).also {
                    cachedContext = it
                }
            }
        }
        return options.contextAt(code, index).also {
            cachedContext = it
        }
    }

    private fun segmentStartAfter(
        previousSegmentEnd: Int,
        index: Int,
    ): Int {
        var segmentStart = (previousSegmentEnd + 1).coerceAtMost(code.length)
        var cursor = segmentStart
        while (cursor < index && cursor < code.length) {
            if (code[cursor] == '\n' || code[cursor] == ';') segmentStart = cursor + 1
            cursor++
        }
        return segmentStart
    }
}

private fun markerAt(
    code: String,
    start: Int,
    markers: List<String>,
): String? =
    markers.firstOrNull { marker ->
        start + marker.length <= code.length &&
            code.regionMatches(start, marker, 0, marker.length)
    }

private fun QualifiedNameOptions.contextAt(
    code: String,
    index: Int,
): QualifiedNameContext {
    val previousBoundary = maxOf(
        code.lastIndexOf('\n', startIndex = (index - 1).coerceAtLeast(0)),
        code.lastIndexOf(';', startIndex = (index - 1).coerceAtLeast(0)),
    )
    val segmentStart = previousBoundary.let {
        if (it == -1) 0 else it + 1
    }
    return contextFromSegmentStart(
        code = code,
        segmentStart = segmentStart,
        index = index,
    )
}

private fun QualifiedNameOptions.contextFromSegmentStart(
    code: String,
    segmentStart: Int,
    index: Int,
): QualifiedNameContext {
    val segmentEnd = nextBoundary(code, start = index)
    val nameStart = nameStartForSegment(code, segmentStart)
    val nameStop = nameStart?.let { firstNameStop(code, start = it, limit = segmentEnd) }
    return QualifiedNameContext(
        segmentStart = segmentStart,
        segmentEnd = segmentEnd,
        nameStart = nameStart,
        nameStop = nameStop,
    )
}

private fun QualifiedNameOptions.nameStartForSegment(
    code: String,
    segmentStart: Int,
): Int? {
    var cursor = segmentStart.skipHorizontalWhitespace(code)
    if (cursor >= code.length || code[cursor] !in possibleContextStartChars) return null
    cursor = skipLeadingAnnotations(code, cursor).skipHorizontalWhitespace(code)
    cursor = skipLeadingModifiers(code, cursor).skipHorizontalWhitespace(code)

    val introducer = readIdentifier(code, cursor) ?: return null
    val optionalModifiers = introducers[introducer.word] ?: return null
    cursor = introducer.end.skipHorizontalWhitespace(code)
    cursor = skipOptionalModifiers(code, cursor, optionalModifiers).skipHorizontalWhitespace(code)
    return cursor
}

private fun nextBoundary(
    code: String,
    start: Int,
): Int {
    val nextNewline = code.indexOf('\n', startIndex = start)
    val nextSemicolon = code.indexOf(';', startIndex = start)
    return when {
        nextNewline == -1 -> nextSemicolon
        nextSemicolon == -1 -> nextNewline
        else -> minOf(nextNewline, nextSemicolon)
    }.let { boundary ->
        if (boundary == -1) code.length else boundary
    }
}

private fun QualifiedNameOptions.skipLeadingModifiers(
    code: String,
    start: Int,
): Int {
    var cursor = start
    while (true) {
        val modifier = readIdentifier(code, cursor) ?: return cursor
        if (modifier.word !in leadingModifiers) return cursor
        cursor = modifier.end.skipHorizontalWhitespace(code)
    }
}

private fun skipOptionalModifiers(
    code: String,
    start: Int,
    optionalModifiers: Set<String>,
): Int {
    var cursor = start
    while (true) {
        val modifier = readIdentifier(code, cursor) ?: return cursor
        if (modifier.word !in optionalModifiers) return cursor
        cursor = modifier.end.skipHorizontalWhitespace(code)
    }
}

private fun skipLeadingAnnotations(
    code: String,
    start: Int,
): Int {
    var cursor = start
    while (cursor < code.length && code[cursor] == '@') {
        cursor++
        while (cursor < code.length && code[cursor].isIdentifierPart()) cursor++
        cursor = cursor.skipHorizontalWhitespace(code)
    }
    return cursor
}

private fun QualifiedNameOptions.firstNameStop(
    code: String,
    start: Int,
    limit: Int,
): Int? {
    var cursor = start
    while (cursor < limit && cursor < code.length) {
        when (code[cursor]) {
            '\n', ';', '=' -> return cursor
        }
        val identifier = readIdentifier(code, cursor)
        if (identifier != null) {
            if (identifier.word in terminatorKeywords) return cursor
            cursor = identifier.end
        } else {
            cursor++
        }
    }
    return null
}

private data class IdentifierReadResult(
    val word: String,
    val end: Int,
)

private data class QualifiedNameContext(
    val segmentStart: Int,
    val segmentEnd: Int,
    val nameStart: Int?,
    val nameStop: Int?,
) {
    fun contains(index: Int): Boolean =
        index >= segmentStart && index < segmentEnd

    fun isInNameContext(index: Int): Boolean {
        val start = nameStart ?: return false
        return index >= start && (nameStop == null || index <= nameStop)
    }
}

private fun readIdentifier(
    code: String,
    start: Int,
): IdentifierReadResult? {
    if (start >= code.length || !code[start].isIdentifierStart()) return null
    var end = start + 1
    while (end < code.length && code[end].isIdentifierPart()) end++
    return IdentifierReadResult(
        word = code.substring(start, end),
        end = end,
    )
}

private fun Int.skipHorizontalWhitespace(code: String): Int {
    var cursor = this
    while (cursor < code.length && code[cursor].isWhitespace() && code[cursor] != '\n') cursor++
    return cursor
}
