package com.gallatinapps.syntaxmp.engine.primitives

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal data class QualifiedNameOptions(
    val introducers: Map<String, Set<String>>,
    val leadingModifiers: Set<String> = emptySet(),
    val separators: Set<String> = setOf("."),
    val wildcardMarkers: Set<String> = setOf("*"),
    val terminatorKeywords: Set<String> = emptySet(),
    val role: SyntaxRole = SyntaxRole.Variable.Namespace,
) {
    val markersByDescendingLength: List<String> =
        (separators + wildcardMarkers)
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedByDescending { it.length }
    val possibleContextStartChars: Set<Char> =
        (introducers.keys + leadingModifiers)
            .mapNotNull { it.firstOrNull() }
            .plus('@')
            .toSet()

    companion object {
        val None = QualifiedNameOptions(introducers = emptyMap())

        fun after(
            vararg keywords: String,
            leadingModifiers: Set<String> = emptySet(),
            separators: Set<String> = setOf("."),
            wildcardMarkers: Set<String> = setOf("*"),
            terminatorKeywords: Set<String> = emptySet(),
            role: SyntaxRole = SyntaxRole.Variable.Namespace,
        ): QualifiedNameOptions =
            QualifiedNameOptions(
                introducers = keywords.associateWith { emptySet() },
                leadingModifiers = leadingModifiers,
                separators = separators,
                wildcardMarkers = wildcardMarkers,
                terminatorKeywords = terminatorKeywords,
                role = role,
            )
    }
}
