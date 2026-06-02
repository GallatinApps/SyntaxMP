package com.gallatinapps.syntaxmp.scanners.script

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.numbers.NumericLiteralScanner
import com.gallatinapps.syntaxmp.primitives.numbers.ScriptLikeNumberScanner
import com.gallatinapps.syntaxmp.primitives.BlockCommentOptions
import com.gallatinapps.syntaxmp.primitives.LineCommentOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions

internal data class ScriptLikeScannerOptions(
    val lineComments: LineCommentOptions = LineCommentOptions.Hash,
    val blockComments: BlockCommentOptions? = BlockCommentOptions.None,
    val identifiers: ScriptIdentifierOptions = ScriptIdentifierOptions.Default,
    val qualifiedNames: QualifiedNameOptions = QualifiedNameOptions.None,
    val literals: StringLiteralOptions = StringLiteralOptions.scriptLikeDefaults(),
    val numbers: NumericLiteralScanner = ScriptLikeNumberScanner,
    val hashBracketAnnotations: Boolean = false,
    val lineStartDecoratorRole: SyntaxRole? = null,
) {
    companion object {
        val Standard = ScriptLikeScannerOptions()
    }
}

internal data class ScriptIdentifierOptions(
    val sigilVariablePrefixes: Set<Char> = emptySet(),
    val suffixes: Set<Char> = emptySet(),
) {
    companion object {
        val Default = ScriptIdentifierOptions()
    }
}
