package com.gallatinapps.syntaxmp.languages.postgresql

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.languages.sql.SqlScanner
import com.gallatinapps.syntaxmp.languages.sql.SqlScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

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
