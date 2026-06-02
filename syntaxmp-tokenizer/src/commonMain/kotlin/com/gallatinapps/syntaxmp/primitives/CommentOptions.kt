package com.gallatinapps.syntaxmp.primitives

internal data class LineCommentOptions(
    val prefixes: Set<String>,
) {
    val prefixesByDescendingLength: List<String> =
        prefixes
            .filter { it.isNotEmpty() }
            .sortedByDescending { it.length }

    companion object {
        val None = LineCommentOptions(prefixes = emptySet())
        val Hash = LineCommentOptions(prefixes = setOf("#"))
        val DoubleSlash = LineCommentOptions(prefixes = setOf("//"))
    }
}

internal data class BlockCommentOptions(
    val opener: String,
    val closer: String,
) {
    companion object {
        val None: BlockCommentOptions? = null
        val CLike = BlockCommentOptions(opener = "/*", closer = "*/")
    }
}

internal data class CommentOptions(
    val line: LineCommentOptions,
    val block: BlockCommentOptions?,
) {
    companion object {
        val CLike = CommentOptions(
            line = LineCommentOptions.DoubleSlash,
            block = BlockCommentOptions.CLike,
        )
    }
}
