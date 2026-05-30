package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId

import kotlin.test.Test
import kotlin.test.assertTrue

class SyntaxTokenizerEngineErrorHandlingTest {
    @Test
    fun extensionThrowableReturnsEmptySpans() {
        val engine = testEngine(
            extensions = listOf(
                SyntaxLanguageExtension(
                    languageId = SyntaxLanguageId.Kotlin,
                    tokenizer = SyntaxTokenizer { throw Throwable("boom") },
                ),
            ),
        )

        val result = engine.tokenize("val answer = 42", "kotlin")

        assertTrue(result.isEmpty())
    }
}
