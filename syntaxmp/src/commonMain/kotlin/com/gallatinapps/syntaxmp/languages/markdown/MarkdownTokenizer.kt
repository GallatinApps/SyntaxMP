package com.gallatinapps.syntaxmp.languages.markdown

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object MarkdownTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            MarkdownScanner(
                request = request,
                htmlLanguage = com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId.Html.value,
            ).scan(),
        )
}
