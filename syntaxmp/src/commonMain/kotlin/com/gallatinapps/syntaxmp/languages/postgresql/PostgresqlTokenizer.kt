package com.gallatinapps.syntaxmp.languages.postgresql

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.sql.SqlScanner
import com.gallatinapps.syntaxmp.languages.sql.SqlScannerOptions

internal object PostgresqlTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            SqlScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = PostgresqlKeywordRoles,
                constants = PostgresqlConstants,
                typeKeywords = PostgresqlTypeKeywords,
                builtinRoles = PostgresqlBuiltinRoles,
                options = SqlScannerOptions(dollarQuotedStrings = true),
            ).scan(),
        )
}
