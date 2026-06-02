package com.gallatinapps.syntaxmp.languages.sqlite

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.languages.sql.SqlScanner
import com.gallatinapps.syntaxmp.languages.sql.SqlScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

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
