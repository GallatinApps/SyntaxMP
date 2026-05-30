package com.gallatinapps.syntaxmp.compose.theme

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

/** Sparse foreground style map keyed by syntax role. */
public typealias SyntaxRoleStyles = Map<SyntaxRole, SyntaxStyle>

/** Returns an empty syntax role style map. */
public fun SyntaxRoleStyles(): SyntaxRoleStyles = emptyMap()

/** Returns a syntax role style map containing [styles]. */
public fun SyntaxRoleStyles(
    vararg styles: Pair<SyntaxRole, SyntaxStyle>,
): SyntaxRoleStyles = mapOf(*styles)

/** Returns a copy with [style] replacing the entry for [role]. */
public fun SyntaxRoleStyles.withRoleStyle(
    role: SyntaxRole,
    style: SyntaxStyle,
): SyntaxRoleStyles = this + (role to style)
