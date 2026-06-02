package com.gallatinapps.syntaxmp.builtins.markdown

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object MarkdownTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        MarkdownScanner(
            request = request,
            htmlLanguage = com.gallatinapps.syntaxmp.language.LanguageId.Html.value,
        ).scan()
}
