package com.gallatinapps.syntaxmp.languages.sql

internal data class SqlScannerOptions(
    val dollarQuotedStrings: Boolean = false,
    val backtickQuotedIdentifiers: Boolean = false,
    val bracketQuotedIdentifiers: Boolean = false,
    val questionMarkParameters: Boolean = false,
    val namedParameterPrefixes: Set<Char> = emptySet(),
    val dollarParameterQualifiedSuffixes: Boolean = false,
    val blobLiteralPrefixes: Set<Char> = emptySet(),
) {
    companion object {
        val Standard = SqlScannerOptions()
    }
}
