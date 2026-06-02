package com.gallatinapps.syntaxmp.scanners.clike

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.primitives.numbers.NumericLiteralScanner
import com.gallatinapps.syntaxmp.primitives.CommentOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions

internal data class CLikeScannerOptions(
    val comments: CommentOptions = CommentOptions.CLike,
    val annotations: AnnotationOptions = AnnotationOptions.None,
    val identifiers: IdentifierOptions = IdentifierOptions.Default,
    val qualifiedNames: QualifiedNameOptions = QualifiedNameOptions.None,
    val literals: StringLiteralOptions = StringLiteralOptions.cLikeDefaults(),
    val preprocessor: PreprocessorOptions = PreprocessorOptions.None,
    val numbers: NumericLiteralScanner = ConfigurableNumberScanner.Default,
) {
    companion object {
        val Standard = CLikeScannerOptions()
    }
}

internal data class IdentifierOptions(
    val macroSuffix: Boolean = false,
    val classifyUppercaseCallsAsTypes: Boolean = false,
    val typeAfterColon: Boolean = false,
    val wildcardIdentifierRole: SyntaxRole? = null,
    val enumMemberRole: SyntaxRole? = null,
    val enumMemberAllowsPayload: Boolean = false,
    val structFieldRole: SyntaxRole? = null,
    val labelDeclarationRole: SyntaxRole? = null,
    val propertyBeforeAssignment: Boolean = false,
    val propertyBeforeColon: Boolean = false,
    val propertyBeforeColonMode: PropertyBeforeColonMode = PropertyBeforeColonMode.Disabled,
    val namedArgumentRole: SyntaxRole? = null,
    val constantDeclarationKeywords: Set<String> = emptySet(),
    val constantDeclarationRequiresAssignment: Boolean = false,
    val functionDeclarationKeywords: Set<String> = emptySet(),
    val typeDeclarationKeywords: Set<String> = emptySet(),
    val memberFunctionRole: SyntaxRole? = null,
    val backtickIdentifierRole: SyntaxRole? = null,
    val sigilVariablePrefixes: Set<Char> = emptySet(),
    val sigilVariableRole: SyntaxRole = SyntaxRole.Variable,
) {
    companion object {
        val Default = IdentifierOptions()
    }
}

internal enum class PropertyBeforeColonMode {
    Disabled,
    ObjectLikeOnly,
}

internal data class AnnotationOptions(
    val prefix: Char?,
    val additionalPrefixes: Set<Char> = emptySet(),
    val bracketPrefixes: Set<String> = emptySet(),
    val bracketRequiresLineStart: Boolean = false,
    val ignorePrefixAfterIdentifier: Boolean = false,
) {
    val bracketPrefixesByDescendingLength: List<String> =
        bracketPrefixes
            .filter { it.isNotEmpty() }
            .sortedByDescending { it.length }

    companion object {
        val None = AnnotationOptions(prefix = null)
        val AtSign = AnnotationOptions(prefix = '@')
        val AtSignAndHash = AnnotationOptions(prefix = '@', additionalPrefixes = setOf('#'))
        val Hash = AnnotationOptions(prefix = '#')
        val HashBracket = AnnotationOptions(prefix = null, bracketPrefixes = setOf("#["))
    }
}

internal data class PreprocessorOptions(
    val linePrefix: Char?,
) {
    companion object {
        val None = PreprocessorOptions(linePrefix = null)
        val Hash = PreprocessorOptions(linePrefix = '#')
    }
}
