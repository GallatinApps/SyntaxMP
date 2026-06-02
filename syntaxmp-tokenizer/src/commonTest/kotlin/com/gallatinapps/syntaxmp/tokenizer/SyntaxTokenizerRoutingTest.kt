package com.gallatinapps.syntaxmp.tokenizer

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.builtins.fixtures.defaultTestEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntaxTokenizerRoutingTest {
    @Test
    fun defaultLanguageSetHasTokenizersForEveryLanguage() {
        val samples = mapOf(
            LanguageId.Json to """{"name": "scan"}""",
            LanguageId.Yaml to "name: scan\n",
            LanguageId.Toml to "name = \"scan\"\n",
            LanguageId.Csv to "name,count\n\"Hash,Jot\",2",
            LanguageId.Markdown to "# Title\n\n- item with `code`\n",
            LanguageId.Sql to "select count(*) from notes where pinned = true",
            LanguageId.Sqlite to "select json_extract(data, '${'$'}.title') from [notes] where id = ?1",
            LanguageId.Diff to "diff --git a/a.md b/a.md\n@@ -1 +1 @@\n-old\n+new",
            LanguageId.Css to ".note { color: #38a3ff; }",
            LanguageId.Html to """<section class="note">Hi</section>""",
            LanguageId.Xml to """<?xml version="1.0"?><vector android:width="24dp"/>""",
            LanguageId.Ini to "name = scan\n",
            LanguageId.Properties to "name = scan\n",
            LanguageId.Dotenv to "export NAME=scan\n",
            LanguageId.Dockerfile to "FROM alpine:3.20\nRUN echo hi",
            LanguageId.Makefile to "build:\n\tgradle build",
            LanguageId.GraphQl to "query Note { note { title } }",
            LanguageId.Protobuf to "message Note { string title = 1; }",
            LanguageId.Postgresql to "select data->>'title' from notes returning id",
            LanguageId.Python to "def score(job):\n    return 1",
            LanguageId.Ruby to "def score(job)\n  return 1\nend",
            LanguageId.Php to "<?php function score(): int { return 1; }",
            LanguageId.Go to "func score() int { return 1 }",
            LanguageId.Rust to "fn score() -> i32 { return 1 }",
            LanguageId.Dart to "class Job { final String name; }",
            LanguageId.C to "int score(void) { return 1; }",
            LanguageId.Cpp to "class Job { int score() { return 1; } };",
            LanguageId.CSharp to "class Job { int Score() => 1; }",
            LanguageId.Kotlin to "fun score(): Int = 1",
            LanguageId.Swift to "func score() -> Int { return 1 }",
            LanguageId.Java to "class Job { int score() { return 1; } }",
            LanguageId.JavaScript to "function score() { return 1 }",
            LanguageId.TypeScript to "function score(): number { return 1 }",
            LanguageId.Jsx to """export function Card() { return <section className="card">Hi</section>; }""",
            LanguageId.Tsx to "type Props = { title: string }; export function Card(props: Props) { return <h1>{props.title}</h1>; }",
            LanguageId.Shell to "echo 1",
            LanguageId.Bash to "if [[ -n ${'$'}HOME ]]; then echo ${'$'}HOME; fi",
            LanguageId.Zsh to "autoload -Uz compinit && compinit",
            LanguageId.PowerShell to "Write-Host ${'$'}name",
        )

        LanguageId.BuiltIns.forEach { language ->
            val code = samples.getValue(language)

            assertTrue(
                defaultTestEngine().tokenize(code = code, languageLabel = language.value).isNotEmpty(),
                "Expected ${language.value} to resolve to a built-in tokenizer.",
            )
        }
    }

    @Test
    fun unknownAndBlankLanguagesReturnNoSpans() {
        assertTrue(defaultTestEngine().tokenize("val x = 1", "").isEmpty())
        assertTrue(defaultTestEngine().tokenize("val x = 1", "brainfuck").isEmpty())
    }

    @Test
    fun coffeeScriptAliasesNoLongerResolve() {
        assertTrue(defaultTestEngine().tokenize("score = -> 1", "coffee").isEmpty())
        assertTrue(defaultTestEngine().tokenize("score = -> 1", "coffeescript").isEmpty())
    }

    @Test
    fun resolveCoversBuiltInLanguageExtensions() {
        val engine = defaultTestEngine()

        assertEquals(LanguageId.Toml, engine.resolveLanguageId("toml"))
        assertEquals(LanguageId.Csv, engine.resolveLanguageId("csv"))
        assertEquals(LanguageId.Markdown, engine.resolveLanguageId("md"))
        assertEquals(LanguageId.Sqlite, engine.resolveLanguageId("sqlite3"))
        assertEquals(LanguageId.Diff, engine.resolveLanguageId("patch"))
        assertEquals(LanguageId.Css, engine.resolveLanguageId("css"))
        assertEquals(LanguageId.Html, engine.resolveLanguageId("htm"))
        assertEquals(LanguageId.Xml, engine.resolveLanguageId("xml"))
        assertEquals(LanguageId.Properties, engine.resolveLanguageId("properties"))
        assertEquals(LanguageId.Dotenv, engine.resolveLanguageId("dotenv"))
        assertEquals(LanguageId.Dotenv, engine.resolveLanguageId("env"))
        assertEquals(LanguageId.Dockerfile, engine.resolveLanguageId("containerfile"))
        assertEquals(LanguageId.Makefile, engine.resolveLanguageId("mk"))
        assertEquals(LanguageId.GraphQl, engine.resolveLanguageId("gql"))
        assertEquals(LanguageId.Protobuf, engine.resolveLanguageId("proto"))
        assertEquals(LanguageId.Postgresql, engine.resolveLanguageId("pgsql"))
        assertEquals(LanguageId.Kotlin, engine.resolveLanguageId("gradle.kts"))
        assertEquals(LanguageId.Jsx, engine.resolveLanguageId("jsx"))
        assertEquals(LanguageId.Tsx, engine.resolveLanguageId("tsx"))
        assertEquals(LanguageId.Shell, engine.resolveLanguageId("sh"))
        assertEquals(LanguageId.Bash, engine.resolveLanguageId("bash"))
        assertEquals(LanguageId.Zsh, engine.resolveLanguageId("zsh"))
        assertEquals(LanguageId.PowerShell, engine.resolveLanguageId("ps1"))
    }
}
