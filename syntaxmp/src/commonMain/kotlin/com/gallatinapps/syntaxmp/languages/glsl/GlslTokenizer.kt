package com.gallatinapps.syntaxmp.languages.glsl

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.PreprocessorOptions

internal object GlslTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowLeadingDot = true,
        typeSuffixes = setOf("f", "F", "u", "U"),
    )
    private val scannerOptions = CLikeScannerOptions(
        preprocessor = PreprocessorOptions.Hash,
        numbers = numberScanner,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = GlslKeywordRoles,
                constants = GlslConstants,
                typeKeywords = GlslTypeKeywords,
                builtinRoles = GlslBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
