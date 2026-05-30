package com.gallatinapps.syntaxmp.languages.dnszone

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult

internal object DnsZoneTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            DnsZoneScanner(
                code = request.code,
                language = request.languageId,
                classes = DnsZoneClasses,
                recordTypes = DnsZoneRecordTypes,
            ).scan(),
        )
}
