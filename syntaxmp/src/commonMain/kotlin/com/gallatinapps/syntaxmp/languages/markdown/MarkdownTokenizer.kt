package com.gallatinapps.syntaxmp.languages.markdown

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object MarkdownTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkdownScanner(
            request = request,
            htmlLanguage = com.gallatinapps.syntaxmp.engine.language.LanguageId.Html.value,
        ).scan()
}
