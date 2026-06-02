package com.gallatinapps.syntaxmp.primitives

internal fun Char.isIdentifierStart(): Boolean =
    this == '_' || isLetter()

internal fun Char.isIdentifierPart(): Boolean =
    this == '_' || isLetterOrDigit()
