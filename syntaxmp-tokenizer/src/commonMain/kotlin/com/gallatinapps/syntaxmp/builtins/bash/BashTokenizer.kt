package com.gallatinapps.syntaxmp.builtins.bash

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.builtins.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object BashTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ShellTokenizer.tokenize(request)
}
