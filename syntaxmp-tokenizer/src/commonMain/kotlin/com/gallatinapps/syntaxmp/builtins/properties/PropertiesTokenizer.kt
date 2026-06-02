package com.gallatinapps.syntaxmp.builtins.properties

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.builtins.ini.IniTokenizer
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object PropertiesTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        IniTokenizer.tokenize(request)
}
