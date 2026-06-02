package com.gallatinapps.syntaxmp.languages.bash

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.languages.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object BashTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ShellTokenizer.tokenize(request)
}
