package com.gallatinapps.syntaxmp.languages.fixtures

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageHighlightSmokeTest {
    @Test
    fun kotlinHighlightsKeywordsTypesFunctionsVariablesAndNumbers() {
        val code = """
                data class Job(val name: String, val count: Int)
                fun score(job: Job): Int = max(0, job.count)
            """.trimIndent()
        val result = defaultTestEngine().tokenize(
            code = code,
            languageLabel = "kotlin",
        )

        assertTrue(result.has(code, SyntaxRole.Keyword, "data"))
        assertTrue(result.has(code, SyntaxRole.Type, "Job"))
        assertTrue(result.has(code, SyntaxRole.Function, "score"))
        assertTrue(result.has(code, SyntaxRole.Variable, "job"))
        assertTrue(result.has(code, SyntaxRole.Number, "0"))
    }

    @Test
    fun kotlinHighlightsConstructorNamesAndStringInterpolationContents() {
        val code = """
            data class Note(val title: String, val pinned: Boolean = false)
            sealed interface SyncState {
                data object Idle : SyncState
                data class Running(val percent: Int) : SyncState
            }
            fun summarize(note: Note) {
                if (note.pinned) println("Pinned: ${'$'}{note.title}")
            }
            val sample = listOf(Note("Plan", pinned = true), Running(10))
            println("Sample ${'$'}sample")
        """.trimIndent()
        val result = defaultTestEngine().tokenize(code, "kotlin")

        assertTrue(result.has(code, SyntaxRole.Type, "Note"))
        assertTrue(result.has(code, SyntaxRole.Type, "Running"))
        assertTrue(result.has(code, SyntaxRole.Variable, "note"))
        assertTrue(result.has(code, SyntaxRole.Property, "title"))
        assertTrue(result.has(code, SyntaxRole.Variable, "sample"))
    }

    @Test
    fun kotlinImportPathsStayBaseColored() {
        val code = """
            import android.os.Build
            import java.util.Locale
        """.trimIndent()
        val result = defaultTestEngine().tokenize(code, "kotlin")

        assertTrue(result.has(code, SyntaxRole.Keyword, "import"))
        assertTrue(result.has(code, SyntaxRole.Variable, "android.os.Build"))
        assertTrue(result.has(code, SyntaxRole.Variable, "java.util.Locale"))
        assertFalse(result.has(code, SyntaxRole.Type, "Build"))
        assertFalse(result.has(code, SyntaxRole.Type, "Locale"))
    }

    @Test
    fun kotlinHighlightsWholeMultilineBlockComment() {
        val code = """
            // androidMain
            /*
             * Imports should stay visually calmer than declarations.
             */
            import android.os.Build
        """.trimIndent()
        val result = defaultTestEngine().tokenize(code, "kotlin")
        val commentStart = code.indexOf("/*")
        val commentEnd = code.indexOf("*/") + 2

        assertTrue(
            result.any { span ->
                span.role == SyntaxRole.Comment &&
                    span.start == commentStart &&
                    span.endExclusive == commentEnd
            },
        )
        assertFalse(result.has(code, SyntaxRole.Variable, "Imports"))
    }

    @Test
    fun swiftHighlightsFoundationLikeFixture() {
        val code = """
            import Foundation
            struct Job: Identifiable {
                let id = UUID()
                var enabled: Bool
            }
            func score(_ job: Job) -> Int {
                return max(0, job.enabled ? 10 : -4)
            }
        """.trimIndent()
        val result = defaultTestEngine().tokenize(code, "swift")

        assertTrue(result.has(code, SyntaxRole.Keyword, "import"))
        assertTrue(result.has(code, SyntaxRole.Variable, "Foundation"))
        assertTrue(result.has(code, SyntaxRole.Function, "score"))
        assertTrue(result.has(code, SyntaxRole.Keyword, "_"))
        assertTrue(result.has(code, SyntaxRole.Type, "UUID"))
        assertTrue(result.has(code, SyntaxRole.Property, "enabled"))
        assertTrue(result.has(code, SyntaxRole.Number, "10"))
    }

    @Test
    fun swiftHighlightsStringInterpolationContents() {
        val code = """print("\(index + 1). \(job.name): \(score(job))")"""
        val result = defaultTestEngine().tokenize(code, "swift")

        assertTrue(result.has(code, SyntaxRole.Function, "print"))
        assertTrue(result.has(code, SyntaxRole.Variable, "index"))
        assertTrue(result.has(code, SyntaxRole.Number, "1"))
        assertTrue(result.has(code, SyntaxRole.Variable, "job"))
        assertTrue(result.has(code, SyntaxRole.Property, "name"))
        assertTrue(result.has(code, SyntaxRole.Function, "score"))
    }

    @Test
    fun jsonHighlightsPropertiesStringsNumbersAndConstants() {
        val code = """{"name": "scan", "count": 4, "enabled": true, "note": "hi 🚀"}"""
        val result = defaultTestEngine().tokenize(code, "json")

        assertTrue(result.has(code, SyntaxRole.Property, "\"name\""))
        assertTrue(result.has(code, SyntaxRole.String, "\"scan\""))
        assertTrue(result.has(code, SyntaxRole.Number, "4"))
        assertTrue(result.has(code, SyntaxRole.Constant, "true"))
        assertTrue(result.has(code, SyntaxRole.String, "\"hi 🚀\""))
    }

}
