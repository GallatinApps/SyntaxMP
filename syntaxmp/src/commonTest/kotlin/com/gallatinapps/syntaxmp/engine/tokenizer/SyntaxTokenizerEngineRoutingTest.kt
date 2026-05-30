package com.gallatinapps.syntaxmp.engine.tokenizer

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageExtension
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.languages.fixtures.defaultTestEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntaxTokenizerEngineRoutingTest {
    @Test
    fun defaultLanguageSetHasTokenizersForEveryLanguage() {
        val samples = mapOf(
            SyntaxLanguageId.Json to """{"name": "scan"}""",
            SyntaxLanguageId.Yaml to "name: scan\n",
            SyntaxLanguageId.Toml to "name = \"scan\"\n",
            SyntaxLanguageId.Csv to "name,count\n\"Hash,Jot\",2",
            SyntaxLanguageId.Markdown to "# Title\n\n- item with `code`\n",
            SyntaxLanguageId.Sql to "select count(*) from notes where pinned = true",
            SyntaxLanguageId.Sqlite to "select json_extract(data, '${'$'}.title') from [notes] where id = ?1",
            SyntaxLanguageId.Diff to "diff --git a/a.md b/a.md\n@@ -1 +1 @@\n-old\n+new",
            SyntaxLanguageId.Css to ".note { color: #38a3ff; }",
            SyntaxLanguageId.Html to """<section class="note">Hi</section>""",
            SyntaxLanguageId.Xml to """<?xml version="1.0"?><vector android:width="24dp"/>""",
            SyntaxLanguageId.Ini to "name = scan\n",
            SyntaxLanguageId.Json5 to "{ name: 'scan', enabled: true }",
            SyntaxLanguageId.Dockerfile to "FROM alpine:3.20\nRUN echo hi",
            SyntaxLanguageId.Makefile to "build:\n\tgradle build",
            SyntaxLanguageId.Terraform to "resource \"local_file\" \"note\" { filename = \"note.md\" }",
            SyntaxLanguageId.Hcl to "server \"web\" { host = \"localhost\" }",
            SyntaxLanguageId.GraphQl to "query Note { note { title } }",
            SyntaxLanguageId.Protobuf to "message Note { string title = 1; }",
            SyntaxLanguageId.Postgresql to "select data->>'title' from notes returning id",
            SyntaxLanguageId.Glsl to "void main() { vec3 color = texture(tex, uv).rgb; }",
            SyntaxLanguageId.ApacheConf to "<VirtualHost *:80>\nServerName example.test\n</VirtualHost>",
            SyntaxLanguageId.DnsZone to "${'$'}ORIGIN example.test.\n@ IN A 192.0.2.1",
            SyntaxLanguageId.ObjectiveC to "@interface Job : NSObject\n@end",
            SyntaxLanguageId.Lua to "function score(job) return job.count end",
            SyntaxLanguageId.R to "score <- function(job) { job${'$'}count }",
            SyntaxLanguageId.Scala to "case class Job(name: String)",
            SyntaxLanguageId.Elixir to "defmodule Job do\n  def score(job), do: job.count\nend",
            SyntaxLanguageId.Python to "def score(job):\n    return 1",
            SyntaxLanguageId.Ruby to "def score(job)\n  return 1\nend",
            SyntaxLanguageId.Php to "<?php function score(): int { return 1; }",
            SyntaxLanguageId.Go to "func score() int { return 1 }",
            SyntaxLanguageId.Rust to "fn score() -> i32 { return 1 }",
            SyntaxLanguageId.Dart to "class Job { final String name; }",
            SyntaxLanguageId.Groovy to "class Job { def score() { return 1 } }",
            SyntaxLanguageId.C to "int score(void) { return 1; }",
            SyntaxLanguageId.Cpp to "class Job { int score() { return 1; } };",
            SyntaxLanguageId.CSharp to "class Job { int Score() => 1; }",
            SyntaxLanguageId.Perl to "sub score { return 1; }",
            SyntaxLanguageId.Kotlin to "fun score(): Int = 1",
            SyntaxLanguageId.Swift to "func score() -> Int { return 1 }",
            SyntaxLanguageId.Java to "class Job { int score() { return 1; } }",
            SyntaxLanguageId.JavaScript to "function score() { return 1 }",
            SyntaxLanguageId.TypeScript to "function score(): number { return 1 }",
            SyntaxLanguageId.Jsx to """export function Card() { return <section className="card">Hi</section>; }""",
            SyntaxLanguageId.Tsx to "type Props = { title: string }; export function Card(props: Props) { return <h1>{props.title}</h1>; }",
            SyntaxLanguageId.Mdx to "import { Chart } from \"./Chart\";\n# Metrics\n<Chart value={count} />",
            SyntaxLanguageId.Vue to """<template><button @click="count++">{{ count }}</button></template><script setup lang="ts">const count: number = 0;</script>""",
            SyntaxLanguageId.Svelte to """<script lang="ts">let count: number = 0;</script>{#if count}<button on:click={() => count += 1}>{count}</button>{/if}""",
            SyntaxLanguageId.Astro to "---\nconst title: string = \"Hi\";\n---\n<h1>{title}</h1>",
            SyntaxLanguageId.Shell to "echo 1",
            SyntaxLanguageId.Scss to "${'$'}color: #fff; .note { color: ${'$'}color; }",
            SyntaxLanguageId.Less to "@color: #fff; .note { color: @color; }",
            SyntaxLanguageId.PowerShell to "Write-Host ${'$'}name",
        )

        SyntaxLanguageId.BuiltIns.forEach { language ->
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

        assertEquals(SyntaxLanguageId.Toml, engine.resolveLanguageId("toml"))
        assertEquals(SyntaxLanguageId.Csv, engine.resolveLanguageId("csv"))
        assertEquals(SyntaxLanguageId.Markdown, engine.resolveLanguageId("md"))
        assertEquals(SyntaxLanguageId.Sqlite, engine.resolveLanguageId("sqlite3"))
        assertEquals(SyntaxLanguageId.Diff, engine.resolveLanguageId("patch"))
        assertEquals(SyntaxLanguageId.Css, engine.resolveLanguageId("css"))
        assertEquals(SyntaxLanguageId.Html, engine.resolveLanguageId("htm"))
        assertEquals(SyntaxLanguageId.Xml, engine.resolveLanguageId("svg"))
        assertEquals(SyntaxLanguageId.Ini, engine.resolveLanguageId("properties"))
        assertEquals(SyntaxLanguageId.Json5, engine.resolveLanguageId("json5"))
        assertEquals(SyntaxLanguageId.Dockerfile, engine.resolveLanguageId("containerfile"))
        assertEquals(SyntaxLanguageId.Makefile, engine.resolveLanguageId("mk"))
        assertEquals(SyntaxLanguageId.Hcl, engine.resolveLanguageId("hcl"))
        assertEquals(SyntaxLanguageId.GraphQl, engine.resolveLanguageId("gql"))
        assertEquals(SyntaxLanguageId.Protobuf, engine.resolveLanguageId("proto"))
        assertEquals(SyntaxLanguageId.Postgresql, engine.resolveLanguageId("pgsql"))
        assertEquals(SyntaxLanguageId.Glsl, engine.resolveLanguageId("glsl"))
        assertEquals(SyntaxLanguageId.ApacheConf, engine.resolveLanguageId("htaccess"))
        assertEquals(SyntaxLanguageId.DnsZone, engine.resolveLanguageId("zone"))
        assertEquals(SyntaxLanguageId.ObjectiveC, engine.resolveLanguageId("objc"))
        assertEquals(SyntaxLanguageId.Lua, engine.resolveLanguageId("lua"))
        assertEquals(SyntaxLanguageId.R, engine.resolveLanguageId("r"))
        assertEquals(SyntaxLanguageId.Scala, engine.resolveLanguageId("sc"))
        assertEquals(SyntaxLanguageId.Elixir, engine.resolveLanguageId("ex"))
        assertEquals(SyntaxLanguageId.Groovy, engine.resolveLanguageId("gradle"))
        assertEquals(SyntaxLanguageId.Kotlin, engine.resolveLanguageId("gradle.kts"))
        assertEquals(SyntaxLanguageId.Jsx, engine.resolveLanguageId("jsx"))
        assertEquals(SyntaxLanguageId.Tsx, engine.resolveLanguageId("tsx"))
        assertEquals(SyntaxLanguageId.Mdx, engine.resolveLanguageId("mdx"))
        assertEquals(SyntaxLanguageId.Vue, engine.resolveLanguageId("vue"))
        assertEquals(SyntaxLanguageId.Svelte, engine.resolveLanguageId("svelte"))
        assertEquals(SyntaxLanguageId.Astro, engine.resolveLanguageId("astro"))
        assertEquals(SyntaxLanguageId.Scss, engine.resolveLanguageId("scss"))
        assertEquals(SyntaxLanguageId.Scss, engine.resolveLanguageId("sass"))
        assertEquals(SyntaxLanguageId.Less, engine.resolveLanguageId("less"))
        assertEquals(SyntaxLanguageId.PowerShell, engine.resolveLanguageId("ps1"))
    }

}
