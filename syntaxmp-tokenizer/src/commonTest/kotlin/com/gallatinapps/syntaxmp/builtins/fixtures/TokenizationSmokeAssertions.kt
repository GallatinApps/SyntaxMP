package com.gallatinapps.syntaxmp.builtins.fixtures

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal fun defaultTestEngine(): SyntaxTokenizer =
    SyntaxTokenizer(extensions = emptyList())

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
