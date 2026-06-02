package com.gallatinapps.syntaxmp.languages.properties

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.languages.ini.IniTokenizer
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object PropertiesTokenizer : LanguageTokenizer {
    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        IniTokenizer.tokenize(request)
}
