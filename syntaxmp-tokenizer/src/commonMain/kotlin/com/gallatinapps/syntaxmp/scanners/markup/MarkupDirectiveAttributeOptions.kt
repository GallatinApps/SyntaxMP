package com.gallatinapps.syntaxmp.scanners.markup

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal data class MarkupDirectiveAttributeOptions(
    val prefixes: Set<String>,
    val role: SyntaxRole,
) {
    fun roleFor(name: String): SyntaxRole? =
        if (prefixes.any(name::startsWith)) role else null

    companion object {
        val None = MarkupDirectiveAttributeOptions(prefixes = emptySet(), role = SyntaxRole.Attribute)
        val ComponentDirectives = MarkupDirectiveAttributeOptions(
            prefixes = setOf("@", ":", "#", "v-", "on:", "bind:", "class:", "use:", "transition:"),
            role = SyntaxRole.Attribute.Directive,
        )
    }
}
