package com.gallatinapps.syntaxmp.builtins.zsh

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.builtins.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object ZshTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ShellTokenizer.tokenize(request)
}
