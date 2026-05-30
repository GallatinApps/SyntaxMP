package com.gallatinapps.syntaxmp.languages.bash

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.shell.ShellTokenizer

internal object BashTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        ShellTokenizer.tokenize(request)
}
