package com.gallatinapps.syntaxmp.languages.fixtures

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine

internal fun defaultTestEngine(): SyntaxTokenizerEngine =
    SyntaxTokenizerEngine(extensions = emptyList())

internal fun List<SyntaxTokenSpan>.has(
    code: String,
    role: SyntaxRole,
    lexeme: String,
): Boolean =
    any { span ->
        span.role.matchesRoot(role) &&
            code.substring(span.start, span.endExclusive) == lexeme
    }

private fun SyntaxRole.matchesRoot(root: SyntaxRole): Boolean =
    this == root || value.startsWith("${root.value}.")
