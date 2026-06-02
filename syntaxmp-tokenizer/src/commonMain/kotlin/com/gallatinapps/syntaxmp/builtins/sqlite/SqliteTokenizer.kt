package com.gallatinapps.syntaxmp.builtins.sqlite

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.builtins.sql.SqlScanner
import com.gallatinapps.syntaxmp.builtins.sql.SqlScannerOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object SqliteTokenizer : LanguageTokenizer {
    private val scannerOptions = SqlScannerOptions(
        backtickQuotedIdentifiers = true,
        bracketQuotedIdentifiers = true,
        questionMarkParameters = true,
        namedParameterPrefixes = setOf(':', '@', '$'),
        dollarParameterQualifiedSuffixes = true,
        blobLiteralPrefixes = setOf('x', 'X'),
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        SqlScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = SqliteKeywordRoles,
            constants = SqliteConstants,
            typeKeywords = SqliteTypeKeywords,
            builtinRoles = SqliteBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
