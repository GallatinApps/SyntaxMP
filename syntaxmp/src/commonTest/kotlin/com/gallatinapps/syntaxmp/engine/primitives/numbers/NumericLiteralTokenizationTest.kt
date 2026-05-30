package com.gallatinapps.syntaxmp.engine.primitives.numbers

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericLiteralTokenizationTest {
    @Test
    fun cLikeTokenizersRecognizeConfiguredNumberForms() {
        assertNumber(SyntaxLanguageId.Cpp, "auto value = 1'000'000;", "1'000'000")
        assertNumber(SyntaxLanguageId.C, "double value = 0x1.8p3;", "0x1.8p3")
        assertNumber(SyntaxLanguageId.JavaScript, "const value = 100n;", "100n")
        assertNumber(SyntaxLanguageId.Go, "var value complex64 = 1i", "1i")
        assertNumber(SyntaxLanguageId.Rust, "let value = 100u32;", "100u32")
        assertNumber(SyntaxLanguageId.JavaScript, "const value = .5;", ".5")
    }

    @Test
    fun cLikeTokenizersDoNotConsumeRangeOperatorsAsNumbers() {
        val kotlinCode = "val range = 5..10"
        val kotlin = engine.tokenize(kotlinCode, SyntaxLanguageId.Kotlin.value)

        assertTrue(kotlin.hasNumber(kotlinCode, "5"))
        assertTrue(kotlin.hasNumber(kotlinCode, "10"))
        assertFalse(kotlin.hasNumber(kotlinCode, "5..10"))
        assertFalse(kotlin.hasNumber(kotlinCode, ".10"))

        val rustCode = "let range = 1..2;"
        val rust = engine.tokenize(rustCode, SyntaxLanguageId.Rust.value)

        assertTrue(rust.hasNumber(rustCode, "1"))
        assertTrue(rust.hasNumber(rustCode, "2"))
        assertFalse(rust.hasNumber(rustCode, "1..2"))
        assertFalse(rust.hasNumber(rustCode, ".2"))
    }

    private fun assertNumber(
        language: SyntaxLanguageId,
        code: String,
        lexeme: String,
    ) {
        assertTrue(
            actual = engine.tokenize(code, language.value).hasNumber(code, lexeme),
            message = "Expected ${language.value} to emit Number token for \"$lexeme\" in:\n$code",
        )
    }

    private fun List<SyntaxTokenSpan>.hasNumber(
        code: String,
        lexeme: String,
    ): Boolean =
        any { span ->
            span.role == SyntaxRole.Number &&
                code.substring(span.start, span.endExclusive) == lexeme
        }

    private companion object {
        val engine = SyntaxTokenizerEngine()
    }
}
