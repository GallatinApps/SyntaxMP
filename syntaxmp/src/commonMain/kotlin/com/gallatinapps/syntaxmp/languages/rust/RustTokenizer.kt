package com.gallatinapps.syntaxmp.languages.rust

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.OctalMode
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BraceFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.HashCountPrefixedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralScope
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralStartRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object RustTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        allowBinary = true,
        allowOctal = OctalMode.ZeroOhPrefix,
        digitSeparators = setOf('_'),
        typeSuffixes = setOf(
            "u8", "u16", "u32", "u64", "u128", "usize",
            "i8", "i16", "i32", "i64", "i128", "isize",
            "f32", "f64",
        ),
    )
    private val braceFormats = listOf(BraceFormatSpecifierRule)
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.HashBracket,
        identifiers = IdentifierOptions(
            macroSuffix = true,
            enumMemberRole = SyntaxRole.Constant,
            enumMemberAllowsPayload = true,
        ),
        qualifiedNames = QualifiedNameOptions.after(
            "use",
            leadingModifiers = setOf("pub"),
            separators = setOf("::"),
            terminatorKeywords = setOf("as"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                HashCountPrefixedStringRule(prefixes = setOf("br", "r"), quote = '"'),
                RustByteCharacterRule,
                RustLifetimeRule,
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    formats = mapOf('"' to braceFormats, '\'' to braceFormats),
                ),
            ),
        ),
        numbers = numberScanner,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = RustKeywordRoles,
            constants = RustConstants,
            typeKeywords = RustTypeKeywords,
            builtinRoles = RustBuiltinRoles,
            options = scannerOptions,
        ).scan()
}

private object RustLifetimeRule : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('\'')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != '\'') return null
        var end = start + 1
        if (!context.code.getOrNull(end).isRustLifetimeStartOrNull()) return null
        end++
        while (end < context.code.length && context.code[end].isRustLifetimePart()) end++
        if (context.code.getOrNull(end) == '\'') return null
        context.emit(start, end, SyntaxRole.Variable)
        return end
    }
}

private object RustByteCharacterRule : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('b')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (!context.startsWith(start, "b'")) return null
        if (start > 0 && context.code[start - 1].isRustLifetimePart()) return null
        var end = start + 2
        while (end < context.code.length) {
            end = if (context.code[end] == '\\') {
                (end + 2).coerceAtMost(context.code.length)
            } else if (context.code[end] == '\'') {
                context.emit(start, end + 1, SyntaxRole.String)
                return end + 1
            } else {
                end + 1
            }
        }
        return null
    }
}

private fun Char?.isRustLifetimeStartOrNull(): Boolean =
    this != null && (this == '_' || isLetter())

private fun Char.isRustLifetimePart(): Boolean =
    this == '_' || isLetterOrDigit()
