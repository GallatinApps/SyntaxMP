package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId

import kotlin.test.Test
import kotlin.test.assertTrue

class SyntaxTokenizerErrorHandlingTest {
    @Test
    fun extensionThrowableReturnsEmptySpans() {
        val engine = testEngine(
            extensions = listOf(
                LanguageExtension(
                    languageId = LanguageId.Kotlin,
                    tokenizer = LanguageTokenizer { throw Throwable("boom") },
                ),
            ),
        )

        val result = engine.tokenize("val answer = 42", "kotlin")

        assertTrue(result.isEmpty())
    }
}
