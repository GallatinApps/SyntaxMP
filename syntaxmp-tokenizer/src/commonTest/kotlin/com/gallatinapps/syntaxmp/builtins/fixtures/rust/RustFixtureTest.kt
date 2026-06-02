package com.gallatinapps.syntaxmp.builtins.fixtures.rust

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class RustFixtureTest {
    private val code = """
        // rust scoring
        pub struct Job { count: i32 }
        pub fn score(job: Job) -> i32 {
            println!("{}", job.count);
            if job.count > 0 { return 1; }
            return 0;
        }
    """.trimIndent()

    @Test
    fun `rust comments declarations types macros properties and numbers`() {
        assertTokenAt("rust", code, "// rust scoring", "Comment")
        assertTokenAt("rust", code, "pub", "Keyword")
        assertTokenAt("rust", code, "struct", "Keyword")
        assertTokenAt("rust", code, "Job", "Type")
        assertTokenAt("rust", code, "i32", "Type")
        assertTokenAt("rust", code, "fn", "Keyword")
        assertTokenAt("rust", code, "score", "Function")
        assertTokenAt("rust", code, "println", "Function")
        assertTokenAt("rust", code, "{}", "Escape")
        assertTokenAt("rust", code, "count", "Property", occurrence = 1)
        assertTokenAt("rust", code, "return", "Keyword")
        assertTokenAt("rust", code, "1", "Number")
    }

    @Test
    fun `comment contents are not declarations`() = assertNoTokenAt(
        language = "rust",
        code = code,
        substring = "rust",
        category = "Keyword",
    )

    @Test
    fun `raw strings stay raw`() {
        val code = "let raw = r#\"struct {value}\"#; let bytes = br#\"bytes\"#;"

        assertTokenAt("rust", code, "r#\"struct {value}\"#", "String")
        assertTokenAt("rust", code, "br#\"bytes\"#", "String")
        assertNoTokenAt("rust", code, "struct", "Keyword")
        assertNoTokenAt("rust", code, "value", "Variable")
    }

    @Test
    fun `lifetimes labels and attributes are not string literals`() {
        val code = """
            #[derive(Debug, Clone)]
            struct Note<'a> { title: &'a str, value: &'a mut str }
            impl<'a> Note<'a> {}
            fn run() {
                'outer: loop { break 'outer; }
                let letter = 'x';
                let byte = b'x';
            }
        """.trimIndent()

        assertTokenAt("rust", code, "#[derive(Debug, Clone)]", "Annotation")
        assertTokenAt("rust", code, "'a", "Variable", occurrence = 0)
        assertTokenAt("rust", code, "'a", "Variable", occurrence = 1)
        assertTokenAt("rust", code, "'a", "Variable", occurrence = 2)
        assertTokenAt("rust", code, "mut", "Keyword")
        assertTokenAt("rust", code, "'outer", "Variable", occurrence = 0)
        assertTokenAt("rust", code, "'outer", "Variable", occurrence = 1)
        assertTokenAt("rust", code, "'x'", "String")
        assertTokenAt("rust", code, "b'x'", "String")
        assertNoTokenAt("rust", code, "'a", "String")
        assertNoTokenAt("rust", code, "'outer", "String")
    }

    @Test
    fun `rust macro rules question operator and block comments stay scoped`() {
        val code = """
            macro_rules! note { () => {}; }
            fn run() -> Result<(), Error> {
                let value = parse()?;
                note!();
                /* fn ignored */
                Ok(value)
            }
        """.trimIndent()

        assertTokenAt("rust", code, "macro_rules", "Function", "function.macro")
        assertTokenAt("rust", code, "fn", "Keyword")
        assertTokenAt("rust", code, "Result", "Type")
        assertTokenAt("rust", code, "Error", "Type")
        assertTokenAt("rust", code, "parse", "Function")
        assertTokenAt("rust", code, "?", "Operator")
        assertTokenAt("rust", code, "note", "Function", "function.macro", occurrence = 1)
        assertTokenAt("rust", code, "/* fn ignored */", "Comment")
        assertTokenAt("rust", code, "Ok", "Constant")
        assertNoTokenAt("rust", code, "fn", "Keyword", occurrence = 1)
    }

    @Test
    fun `rust enum variants including payload variants are constants`() {
        val code = """
            struct Error;

            enum Status {
                Draft,
                Review,
                Published,
            }

            enum ParseResult<T> {
                Ok(T),
                Err { source: Error },
                Pending = 2,
            }
        """.trimIndent()

        assertTokenAt("rust", code, "Error", "Type")
        assertTokenAt("rust", code, "Status", "Type")
        assertTokenAt("rust", code, "Draft", "Constant")
        assertTokenAt("rust", code, "Review", "Constant")
        assertTokenAt("rust", code, "Published", "Constant")
        assertTokenAt("rust", code, "ParseResult", "Type")
        assertTokenAt("rust", code, "Ok", "Constant")
        assertTokenAt("rust", code, "Err", "Constant", occurrence = 1)
        assertTokenAt("rust", code, "source", "Variable")
        assertTokenAt("rust", code, "Error", "Type", occurrence = 1)
        assertTokenAt("rust", code, "Pending", "Constant")
        assertTokenAt("rust", code, "2", "Number")
    }
}
