package com.gallatinapps.syntaxmp.languages.fixtures.perl

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class PerlFixtureTest {
    private val code = """
        # perl scoring
        package Demo;
        sub score {
          my (${ '$' }job) = @_;
          print ${ '$' }job->{name};
          return undef if ${ '$' }job =~ /skip/;
          return 1;
        }
    """.trimIndent()

    @Test
    fun `perl comments declarations sigil variables builtins regex constants and numbers`() {
        assertTokenAt("perl", code, "# perl scoring", "Comment")
        assertTokenAt("perl", code, "package", "Keyword")
        assertTokenAt("perl", code, "Demo", "Variable", "variable.namespace")
        assertTokenAt("perl", code, "sub", "Keyword")
        assertTokenAt("perl", code, "score", "Variable")
        assertTokenAt("perl", code, "my", "Keyword")
        assertTokenAt("perl", code, "${'$'}job", "Variable")
        assertTokenAt("perl", code, "print", "Function")
        assertTokenAt("perl", code, "name", "Variable")
        assertTokenAt("perl", code, "return", "Keyword")
        assertTokenAt("perl", code, "undef", "Constant")
        assertTokenAt("perl", code, "/", "Operator")
        assertTokenAt("perl", code, "skip", "Variable")
        assertTokenAt("perl", code, "1", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "perl",
        code = "${'$'}label = \"return\";",
        substring = "return",
        category = "Keyword",
    )

    @Test
    fun `double quoted strings tokenize variables and format verbs`() {
        val code = "${'$'}label = \"job ${'$'}name %02d\";"

        assertTokenAt("perl", code, "\"job ", "String")
        assertTokenAt("perl", code, "${'$'}name", "Variable")
        assertTokenAt("perl", code, "%02d", "Escape")
    }

    @Test
    fun `heredocs and quote like operators stay coherent`() {
        val code = """
            my ${'$'}doc = <<TEXT;
            hello ${'$'}name
            TEXT
            my ${'$'}literal = <<'TEXT';
            return ${'$'}name
            TEXT
            my ${'$'}quoted = qq/job ${'$'}name/;
            my @words = qw(class return);
            ${'$'}line =~ s/foo/bar/g;
            my ${'$'}re = qr/skip/;
        """.trimIndent()

        assertTokenAt("perl", code, "<<TEXT;\nhello ", "String")
        assertTokenAt("perl", code, "${'$'}name", "Variable")
        assertTokenAt("perl", code, "qq/job ", "String")
        assertTokenAt("perl", code, "${'$'}name", "Variable", occurrence = 2)
        assertTokenAt("perl", code, "s/foo/bar/g", "String")
        assertTokenAt("perl", code, "qr/skip/", "String")
        assertNoTokenAt("perl", code, "return", "Keyword", occurrence = 0)
        assertNoTokenAt("perl", code, "class", "Keyword")
    }

    @Test
    fun `quote like operators support non slash delimiters`() {
        val code = """
            my ${'$'}plain = q{class return};
            my ${'$'}quoted = qq{job ${'$'}name};
            ${'$'}line =~ s{foo}{bar}g;
            my ${'$'}re = qr{skip};
        """.trimIndent()

        assertTokenAt("perl", code, "q{class return}", "String")
        assertNoTokenAt("perl", code, "class", "Keyword")
        assertNoTokenAt("perl", code, "return", "Keyword")
        assertTokenAt("perl", code, "qq{job ", "String")
        assertTokenAt("perl", code, "${'$'}name", "Variable")
        assertTokenAt("perl", code, "s{foo}{bar}g", "String")
        assertTokenAt("perl", code, "qr{skip}", "String")
    }

    @Test
    fun `operator looking literals do not shadow modulo shift or identifier suffixes`() {
        val code = """
            my ${'$'}value = 10 % 3;
            my ${'$'}shifted = ${'$'}left << 1;
            my ${'$'}word = prefixqq{bar};
        """.trimIndent()

        assertTokenAt("perl", code, "%", "Operator")
        assertTokenAt("perl", code, "<<", "Operator")
        assertNoTokenAt("perl", code, "qq{bar}", "String")
    }
}
