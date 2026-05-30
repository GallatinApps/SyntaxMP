package com.gallatinapps.syntaxmp.languages.protobuf

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions

internal object ProtobufTokenizer {
    private val numberScanner = ConfigurableNumberScanner(allowHex = true)
    private val scannerOptions = CLikeScannerOptions(
        identifiers = IdentifierOptions(
            enumMemberRole = SyntaxRole.Constant,
            propertyBeforeAssignment = true,
        ),
        qualifiedNames = QualifiedNameOptions.after(
            "package",
            wildcardMarkers = emptySet(),
        ),
        numbers = numberScanner,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = ProtobufKeywordRoles,
                constants = ProtobufConstants,
                typeKeywords = ProtobufTypeKeywords,
                options = scannerOptions,
            ).scan(),
        )
}
