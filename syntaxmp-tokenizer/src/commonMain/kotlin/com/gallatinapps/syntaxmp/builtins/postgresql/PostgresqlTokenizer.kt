package com.gallatinapps.syntaxmp.builtins.postgresql

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.builtins.sql.SqlScanner
import com.gallatinapps.syntaxmp.builtins.sql.SqlScannerOptions
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object PostgresqlTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        SqlScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = PostgresqlKeywordRoles,
            constants = PostgresqlConstants,
            typeKeywords = PostgresqlTypeKeywords,
            builtinRoles = PostgresqlBuiltinRoles,
            options = SqlScannerOptions(dollarQuotedStrings = true),
        ).scan()
}
