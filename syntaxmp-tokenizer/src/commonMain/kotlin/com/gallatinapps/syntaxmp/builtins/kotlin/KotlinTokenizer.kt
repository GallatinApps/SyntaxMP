package com.gallatinapps.syntaxmp.builtins.kotlin

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object KotlinTokenizer : LanguageTokenizer {
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

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = KotlinKeywordRoles,
            constants = KotlinConstants,
            builtinRoles = KotlinBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
