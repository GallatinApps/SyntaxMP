package com.gallatinapps.syntaxmp.languages.kotlin

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule

internal object KotlinTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        digitSeparators = setOf('_'),
        typeSuffixes = setOf("L", "F", "u", "uL", "uLL"),
    )
    private val dollarInterpolation = listOf(
        DollarIdentifierInterpolationRule,
        BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign.copy(ignorePrefixAfterIdentifier = true),
        identifiers = IdentifierOptions(
            classifyUppercaseCallsAsTypes = false,
            typeAfterColon = true,
            labelDeclarationRole = SyntaxRole.Variable,
            enumMemberRole = SyntaxRole.Property,
            enumMemberAllowsPayload = true,
            namedArgumentRole = SyntaxRole.Variable.Parameter,
            functionDeclarationKeywords = setOf("fun"),
            typeDeclarationKeywords = setOf("class", "interface", "object", "typealias"),
            memberFunctionRole = SyntaxRole.Function.Member,
            backtickIdentifierRole = SyntaxRole.Variable,
        ),
        qualifiedNames = QualifiedNameOptions.after(
            "import",
            "package",
            terminatorKeywords = setOf("as"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
                    interpolation = dollarInterpolation,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to dollarInterpolation),
                ),
            ),
        ),
        numbers = numberScanner,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = KotlinKeywordRoles,
                constants = KotlinConstants,
                builtinRoles = KotlinBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
