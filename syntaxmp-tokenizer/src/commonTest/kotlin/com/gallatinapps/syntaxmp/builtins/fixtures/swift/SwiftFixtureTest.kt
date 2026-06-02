package com.gallatinapps.syntaxmp.builtins.fixtures.swift

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class SwiftFixtureTest {
    private val code = """
        // swift scoring
        import Foundation
        @MainActor
        struct Job {
            let name: String
            var enabled: Bool = true
        }
        func score(_ job: Job) -> Int {
            print("\(job.name)")
            return max(0, 1)
        }
    """.trimIndent()

    @Test
    fun `swift comments imports annotations declarations interpolation and numbers`() {
        assertTokenAt("swift", code, "// swift scoring", "Comment")
        assertTokenAt("swift", code, "import", "Keyword")
        assertTokenAt("swift", code, "Foundation", "Variable", "variable.namespace")
        assertTokenAt("swift", code, "@MainActor", "Annotation")
        assertTokenAt("swift", code, "struct", "Keyword")
        assertTokenAt("swift", code, "Job", "Type")
        assertTokenAt("swift", code, "let", "Keyword")
        assertTokenAt("swift", code, "String", "Type")
        assertTokenAt("swift", code, "true", "Constant")
        assertTokenAt("swift", code, "func", "Keyword")
        assertTokenAt("swift", code, "_", "Keyword")
        assertTokenAt("swift", code, "print", "Function")
        assertTokenAt("swift", code, "name", "Property", occurrence = 1)
        assertTokenAt("swift", code, "0", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "swift",
        code = "let label = \"struct\"",
        substring = "struct",
        category = "Keyword",
    )

    @Test
    fun `nested paren interpolation finds its closing parenthesis`() {
        val code = """print("\(foo(bar(baz)))")"""

        assertTokenAt("swift", code, """\(""", "Escape")
        assertTokenAt("swift", code, "foo", "Function")
        assertTokenAt("swift", code, "bar", "Function")
        assertTokenAt("swift", code, "baz", "Variable")
        assertTokenAt("swift", code, ")", "Escape", occurrence = 2)
    }

    @Test
    fun `multiline strings and raw strings tokenize matching interpolation markers`() {
        val multiline = "let text = \"\"\"value \\(score(job))\"\"\""
        val raw = "let text = #\"value \\#(name)\"#; let plain = #\"value \\(name)\"#"

        assertTokenAt("swift", multiline, "\"\"\"value ", "String")
        assertTokenAt("swift", multiline, "\\(", "Escape")
        assertTokenAt("swift", multiline, "score", "Function")
        assertTokenAt("swift", raw, "\\#(", "Escape")
        assertTokenAt("swift", raw, "name", "Variable")
        assertNoTokenAt("swift", raw, "name", "Variable", occurrence = 1)
    }

    @Test
    fun `repeated hash raw strings require matching interpolation marker arity`() {
        val code = "let raw = ##\"value \\##(name)\"##; let plain = ##\"value \\#(name)\"##"

        assertTokenAt("swift", code, "\\##(", "Escape")
        assertTokenAt("swift", code, "name", "Variable")
        assertNoTokenAt("swift", code, "name", "Variable", occurrence = 1)
    }

    @Test
    fun `hash directives are annotations and property wrappers still work`() {
        val code = """
            @MainActor
            struct NotesView {
                @State private var count = 0
            }

            #Preview {
                NotesView()
            }

            if #available(iOS 17, *) {
                print("ok")
            }
        """.trimIndent()

        assertTokenAt("swift", code, "@MainActor", "Annotation")
        assertTokenAt("swift", code, "@State", "Annotation")
        assertTokenAt("swift", code, "#Preview", "Annotation")
        assertTokenAt("swift", code, "#available", "Annotation")
        assertTokenAt("swift", code, "NotesView", "Type", occurrence = 1)
    }

    @Test
    fun `projected value variables include dollar prefix`() {
        val code = """
            @Binding var note: NoteSummary
            Toggle("Pinned", isOn: ${'$'}note.isPinned)
        """.trimIndent()

        assertTokenAt("swift", code, "${'$'}note", "Variable")
        assertTokenAt("swift", code, "isPinned", "Property")
    }
}
