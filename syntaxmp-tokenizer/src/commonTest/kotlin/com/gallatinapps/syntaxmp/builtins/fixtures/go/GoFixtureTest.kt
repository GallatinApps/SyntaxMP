package com.gallatinapps.syntaxmp.builtins.fixtures.go

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class GoFixtureTest {
    private val code = """
        // go scoring
        package main
        type Job struct { Name string }
        func score(job Job) int {
            println(`raw`)
            if job.Name != "" { return len(job.Name) + 1 }
            return 0
        }
    """.trimIndent()

    @Test
    fun `go comments keywords types functions properties strings and numbers`() {
        assertTokenAt("go", code, "// go scoring", "Comment")
        assertTokenAt("go", code, "package", "Keyword")
        assertTokenAt("go", code, "type", "Keyword")
        assertTokenAt("go", code, "Job", "Type")
        assertTokenAt("go", code, "struct", "Keyword")
        assertTokenAt("go", code, "string", "Type")
        assertTokenAt("go", code, "func", "Keyword")
        assertTokenAt("go", code, "score", "Function")
        assertTokenAt("go", code, "int", "Type")
        assertTokenAt("go", code, "println", "Function")
        assertTokenAt("go", code, "`raw`", "String")
        assertTokenAt("go", code, "Name", "Property", occurrence = 1)
        assertTokenAt("go", code, "1", "Number")
    }

    @Test
    fun `comment contents are not declarations`() = assertNoTokenAt(
        language = "go",
        code = code,
        substring = "go",
        category = "Keyword",
    )

    @Test
    fun `format verbs inside strings are highlighted as escapes`() {
        val code = """fmt.Printf("job %s=%03d %%", name, count)"""

        assertTokenAt("go", code, "%s", "Escape")
        assertTokenAt("go", code, "%03d", "Escape")
        assertTokenAt("go", code, "%%", "Escape")
    }

    @Test
    fun `exported struct fields constants qualified types and raw strings keep their roles`() {
        val code = """
            type Note struct {
                Title string
                UpdatedAt time.Time
            }
            const Draft Status = "draft"
            note.Title = `raw ${'$'}{notInterpolation}`
        """.trimIndent()

        assertTokenAt("go", code, "Title", "Property", occurrence = 0)
        assertTokenAt("go", code, "UpdatedAt", "Property")
        assertTokenAt("go", code, "string", "Type")
        assertTokenAt("go", code, "time", "Variable")
        assertTokenAt("go", code, ".", "Punctuation")
        assertTokenAt("go", code, "Time", "Type")
        assertTokenAt("go", code, "Draft", "Constant")
        assertTokenAt("go", code, "Status", "Type")
        assertTokenAt("go", code, "Title", "Property", occurrence = 1)
        assertTokenAt("go", code, "`raw ${'$'}{notInterpolation}`", "String")
    }

    @Test
    fun `go imports methods interfaces runes imaginary numbers and raw keywords are covered`() {
        val code = """
            import (
                "fmt"
                "io"
            )
            type Reader interface {
                Read([]byte) (int, error)
            }
            func (n Note) Score() int {
                fmt.Println(n.Title)
                letter := 'x'
                value := 1i
                raw := `struct keyword`
                return len(raw) + int(value)
            }
        """.trimIndent()

        assertTokenAt("go", code, "import", "Keyword")
        assertTokenAt("go", code, "\"fmt\"", "String")
        assertTokenAt("go", code, "interface", "Keyword")
        assertTokenAt("go", code, "Read", "Function", occurrence = 1)
        assertTokenAt("go", code, "Note", "Type")
        assertTokenAt("go", code, "Score", "Function")
        assertTokenAt("go", code, "Println", "Function")
        assertTokenAt("go", code, "Title", "Property")
        assertTokenAt("go", code, "'x'", "String")
        assertTokenAt("go", code, "1i", "Number")
        assertTokenAt("go", code, "`struct keyword`", "String")
        assertNoTokenAt("go", code, "struct", "Keyword")
    }
}
