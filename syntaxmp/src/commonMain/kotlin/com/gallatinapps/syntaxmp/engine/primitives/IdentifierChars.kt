package com.gallatinapps.syntaxmp.engine.primitives

internal fun Char.isIdentifierStart(): Boolean =
    this == '_' || isLetter()

internal fun Char.isIdentifierPart(): Boolean =
    this == '_' || isLetterOrDigit()
