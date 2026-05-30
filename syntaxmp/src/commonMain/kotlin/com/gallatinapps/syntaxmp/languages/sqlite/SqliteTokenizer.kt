package com.gallatinapps.syntaxmp.languages.sqlite

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.sql.SqlScanner
import com.gallatinapps.syntaxmp.languages.sql.SqlScannerOptions

internal object SqliteTokenizer {
    private val scannerOptions = SqlScannerOptions(
        backtickQuotedIdentifiers = true,
        bracketQuotedIdentifiers = true,
        questionMarkParameters = true,
        namedParameterPrefixes = setOf(':', '@', '$'),
        dollarParameterQualifiedSuffixes = true,
        blobLiteralPrefixes = setOf('x', 'X'),
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            SqlScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = SqliteKeywordRoles,
                constants = SqliteConstants,
                typeKeywords = SqliteTypeKeywords,
                builtinRoles = SqliteBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
