package com.gallatinapps.syntaxmp.languages.fixtures.r

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class RFixtureTest {
    private val code = """
        # summarize rows
        score <- function(job) {
          values <- c(1, 2, NA)
          print(job${'$'}name)
          if (TRUE) return(sum(values))
          return(NULL)
        }
    """.trimIndent()

    @Test
    fun `r comments keywords builtins constants properties and numbers`() {
        assertTokenAt("r", code, "# summarize rows", "Comment")
        assertTokenAt("r", code, "score", "Variable")
        assertTokenAt("r", code, "<-", "Operator")
        assertTokenAt("r", code, "function", "Keyword")
        assertTokenAt("r", code, "values", "Variable")
        assertTokenAt("r", code, "c", "Function", occurrence = 2)
        assertTokenAt("r", code, "1", "Number")
        assertTokenAt("r", code, "NA", "Constant")
        assertTokenAt("r", code, "print", "Function")
        assertTokenAt("r", code, "name", "Property")
        assertTokenAt("r", code, "TRUE", "Constant")
        assertTokenAt("r", code, "return", "Keyword")
        assertTokenAt("r", code, "sum", "Function", occurrence = 1)
        assertTokenAt("r", code, "NULL", "Constant")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "r",
        code = "label <- \"function\"",
        substring = "function",
        category = "Keyword",
    )

    @Test
    fun `r formulas backtick names namespace operators and comment delimiters are covered`() {
        val code = """
            model <- y ~ stats::filter(`weird name`, x) + pkg:::hidden
            text <- "# not comment"
            # "not string"
        """.trimIndent()

        assertTokenAt("r", code, "~", "Operator")
        assertTokenAt("r", code, "::", "Operator")
        assertTokenAt("r", code, "filter", "Function")
        assertTokenAt("r", code, "`weird name`", "Property")
        assertTokenAt("r", code, ":::", "Operator")
        assertTokenAt("r", code, "hidden", "Variable")
        assertTokenAt("r", code, "\"# not comment\"", "String")
        assertTokenAt("r", code, "# \"not string\"", "Comment")
        assertNoTokenAt("r", code, "# not comment", "Comment")
        assertNoTokenAt("r", code, "\"not string\"", "String")
    }
}
