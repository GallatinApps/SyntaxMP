package com.gallatinapps.syntaxmp.builtins.fixtures.dart

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class DartFixtureTest {
    private val code = """
        // dart model
        @immutable
        class Job {
          final String name;
          final bool enabled = true;
          Job(this.name);
          int score() => print(name) + 1;
        }
    """.trimIndent()

    @Test
    fun `dart comments annotations keywords types functions constants and numbers`() {
        assertTokenAt("dart", code, "// dart model", "Comment")
        assertTokenAt("dart", code, "@immutable", "Annotation")
        assertTokenAt("dart", code, "class", "Keyword")
        assertTokenAt("dart", code, "Job", "Type")
        assertTokenAt("dart", code, "final", "Keyword")
        assertTokenAt("dart", code, "String", "Type")
        assertTokenAt("dart", code, "bool", "Type")
        assertTokenAt("dart", code, "true", "Constant")
        assertTokenAt("dart", code, "score", "Function")
        assertTokenAt("dart", code, "print", "Function")
        assertTokenAt("dart", code, "1", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "dart",
        code = "final label = \"class\";",
        substring = "class",
        category = "Keyword",
    )

    @Test
    fun `string interpolation tokenizes bare and braced expressions`() {
        val code = "final label = 'Job ${'$'}name ${'$'}{score(count)}'; final raw = r'${'$'}name';"

        assertTokenAt("dart", code, "${'$'}", "Escape")
        assertTokenAt("dart", code, "name", "Variable")
        assertTokenAt("dart", code, "${'$'}{", "Escape")
        assertTokenAt("dart", code, "score", "Function")
        assertTokenAt("dart", code, "count", "Variable")
        assertTokenAt("dart", code, "}", "Escape")
        assertNoTokenAt(
            language = "dart",
            code = code,
            substring = "${'$'}",
            category = "Escape",
            occurrence = 2,
        )
    }

    @Test
    fun `triple quoted strings support interpolation and raw triples do not`() {
        val code = "final text = \"\"\"Hello ${'$'}name ${'$'}{score(count)}\"\"\"; " +
            "final single = '''Hi ${'$'}name'''; final raw = r\"\"\"Hello ${'$'}name\"\"\";"

        assertTokenAt("dart", code, "\"\"\"Hello ", "String")
        assertTokenAt("dart", code, "${'$'}", "Escape")
        assertTokenAt("dart", code, "name", "Variable")
        assertTokenAt("dart", code, "${'$'}{", "Escape")
        assertTokenAt("dart", code, "score", "Function")
        assertTokenAt("dart", code, "'''Hi ", "String")
        assertTokenAt("dart", code, "${'$'}", "Escape", occurrence = 2)
        assertNoTokenAt("dart", code, "${'$'}", "Escape", occurrence = 3)
    }

    @Test
    fun `dart import export property access and annotation strings stay scoped`() {
        val code = """
            import 'dart:math' as math;
            export 'src/job.dart' show Job;
            class Job {
              final String title;
              int get count => details.count;
            }
            final text = "@immutable";
        """.trimIndent()

        assertTokenAt("dart", code, "import", "Keyword")
        assertTokenAt("dart", code, "'dart:math'", "String")
        assertTokenAt("dart", code, "as", "Keyword")
        assertTokenAt("dart", code, "export", "Keyword")
        assertTokenAt("dart", code, "'src/job.dart'", "String")
        assertTokenAt("dart", code, "show", "Keyword")
        assertTokenAt("dart", code, "Job", "Type")
        assertTokenAt("dart", code, "String", "Type")
        assertTokenAt("dart", code, "title", "Variable")
        assertTokenAt("dart", code, "count", "Variable")
        assertTokenAt("dart", code, "count", "Property", occurrence = 1)
        assertTokenAt("dart", code, "\"@immutable\"", "String")
        assertNoTokenAt("dart", code, "@immutable", "Annotation")
    }

    @Test
    fun `dart enum members and const declarations are constants without constructor false positives`() {
        val code = """
            enum Status { draft, review, published }

            class Constants {
              static const label = 'draft';
            }

            class Widget {
              const Widget();
            }

            class Router {
              const Router.fromString();
            }

            const answer = 42;
            final mutableName = 'not const';

            final widget = const Widget();
            final route = const Router.fromString();
        """.trimIndent()

        assertTokenAt("dart", code, "Status", "Type")
        assertTokenAt("dart", code, "draft", "Constant")
        assertTokenAt("dart", code, "review", "Constant")
        assertTokenAt("dart", code, "published", "Constant")
        assertTokenAt("dart", code, "label", "Constant")
        assertTokenAt("dart", code, "answer", "Constant")
        assertNoTokenAt("dart", code, "mutableName", "Constant")
        assertTokenAt("dart", code, "Widget", "Type", occurrence = 2)
        assertNoTokenAt("dart", code, "Widget", "Constant", occurrence = 2)
        assertTokenAt("dart", code, "Router", "Type", occurrence = 2)
        assertNoTokenAt("dart", code, "Router", "Constant", occurrence = 2)
    }
}
