package com.gallatinapps.syntaxmp.engine.scanners.script

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.numbers.NumericLiteralScanner
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ScriptLikeNumberScanner
import com.gallatinapps.syntaxmp.engine.primitives.BlockCommentOptions
import com.gallatinapps.syntaxmp.engine.primitives.LineCommentOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions

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
