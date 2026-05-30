package com.gallatinapps.syntaxmp.languages.zsh

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.shell.ShellTokenizer

internal object ZshTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        ShellTokenizer.tokenize(request)
}
