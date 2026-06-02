package com.gallatinapps.syntaxmp.builtins.fixtures

import com.gallatinapps.syntaxmp.role.SyntaxRole
import kotlin.test.Test
import kotlin.test.assertTrue

class LanguageAliasSmokeTest {
    @Test
    fun javascriptAndTypescriptAliasesResolve() {
        val jsCode = "const value = items.map(item => item.id)"
        val tsCode = "type Job = { enabled: boolean }"
        val js = defaultTestEngine().tokenize(jsCode, "js")
        val ts = defaultTestEngine().tokenize(tsCode, "ts")

        assertTrue(js.has(jsCode, SyntaxRole.Keyword, "const"))
        assertTrue(js.has(jsCode, SyntaxRole.Function, "map"))
        assertTrue(ts.has(tsCode, SyntaxRole.Keyword, "type"))
        assertTrue(ts.has(tsCode, SyntaxRole.Type, "Job"))
    }

    @Test
    fun componentLanguageAliasesResolve() {
        val jsxCode = "const view = <Card title={title} />"
        val tsxCode = "type Props = { title: string }; const view = <Card title={props.title} />"

        assertTrue(defaultTestEngine().tokenize(jsxCode, "jsx").has(jsxCode, SyntaxRole.Tag, "Card"))
        assertTrue(defaultTestEngine().tokenize(tsxCode, "tsx").has(tsxCode, SyntaxRole.Keyword, "type"))
    }

    @Test
    fun yamlAndShellAliasesResolve() {
        val yamlCode = "name: scan\nenabled: true\n"
        val shellCode = "if grep -q needle file; then echo \"\$HOME\"; fi"
        val yaml = defaultTestEngine().tokenize(yamlCode, "yml")
        val shell = defaultTestEngine().tokenize(shellCode, "sh")
        val bash = defaultTestEngine().tokenize(shellCode, "bash")
        val zsh = defaultTestEngine().tokenize(shellCode, "zsh")

        assertTrue(yaml.has(yamlCode, SyntaxRole.Property, "name"))
        assertTrue(yaml.has(yamlCode, SyntaxRole.Constant, "true"))
        assertTrue(shell.has(shellCode, SyntaxRole.Keyword, "if"))
        assertTrue(shell.has(shellCode, SyntaxRole.Function, "grep"))
        assertTrue(bash.has(shellCode, SyntaxRole.Keyword, "if"))
        assertTrue(zsh.has(shellCode, SyntaxRole.Function, "grep"))
    }

    @Test
    fun tomlAliasResolvesAndHighlightsVersionCatalogShape() {
        val tomlCode = """
            # NoteKit library versions
            [versions]
            compose = "1.8.0"
            markdownmp = "0.1.0"

            [libraries]
            markdownmp-compose = { module = "com.gallatinapps.markdownmp:markdownmp-compose", version.ref = "markdownmp" }

            [plugins]
            cmp-library = { id = "org.jetbrains.compose", version.ref = "compose", apply = false }
            enabled = true
            targets = ["jvm", "iosArm64"]
        """.trimIndent()
        val toml = defaultTestEngine().tokenize(tomlCode, "toml")

        assertTrue(toml.has(tomlCode, SyntaxRole.Comment, "# NoteKit library versions"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Property, "versions"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Property, "compose"))
        assertTrue(toml.has(tomlCode, SyntaxRole.String, "\"1.8.0\""))
        assertTrue(toml.has(tomlCode, SyntaxRole.Property, "module"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Property, "version"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Property, "ref"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Constant, "false"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Constant, "true"))
        assertTrue(toml.has(tomlCode, SyntaxRole.Punctuation, "["))
    }

    @Test
    fun markdownAliasesResolveAndHighlightStructuralMarkers() {
        val code = """
            # Heading

            > Quote with [link](https://example.com)
            - item with **strong** text and `inlineCode`

            ```kotlin
            val nested = "base only"
            ```
        """.trimIndent()
        val markdown = defaultTestEngine().tokenize(code, "md")

        assertTrue(markdown.has(code, SyntaxRole.Markup, "#"))
        assertTrue(markdown.has(code, SyntaxRole.Markup, ">"))
        assertTrue(markdown.has(code, SyntaxRole.Markup, "-"))
        assertTrue(markdown.has(code, SyntaxRole.Markup, "["))
        assertTrue(markdown.has(code, SyntaxRole.Markup, "**"))
        assertTrue(markdown.has(code, SyntaxRole.Markup, "`"))
        assertTrue(markdown.has(code, SyntaxRole.Markup, "```"))
        assertTrue(markdown.has(code, SyntaxRole.String, "kotlin"))
        assertTrue(markdown.has(code, SyntaxRole.Keyword, "val"))
    }

    @Test
    fun sqliteAliasesResolveAndHighlightDialectForms() {
        val code = """
            CREATE TABLE [note items] (`select` INTEGER);
            INSERT INTO [note items] (`select`) VALUES (?1);
        """.trimIndent()
        val sqlite = defaultTestEngine().tokenize(code, "sqlite3")

        assertTrue(sqlite.has(code, SyntaxRole.Keyword, "CREATE"))
        assertTrue(sqlite.has(code, SyntaxRole.Property, "[note items]"))
        assertTrue(sqlite.has(code, SyntaxRole.Property, "`select`"))
        assertTrue(sqlite.has(code, SyntaxRole.Variable, "?1"))
    }

    @Test
    fun diffAliasesResolveAndHighlightHeadersHunksAndChanges() {
        val code = """
            diff --git a/README.md b/README.md
            index 1111111..2222222 100644
            --- a/README.md
            +++ b/README.md
            @@ -1,3 +1,4 @@
             # Title
            -old line
            +new line
        """.trimIndent()
        val diff = defaultTestEngine().tokenize(code, "patch")

        assertTrue(diff.has(code, SyntaxRole.Markup, "diff --git a/README.md b/README.md"))
        assertTrue(diff.has(code, SyntaxRole.Markup, "--- a/README.md"))
        assertTrue(diff.has(code, SyntaxRole.Markup, "@@ -1,3 +1,4 @@"))
        assertTrue(diff.has(code, SyntaxRole.Markup, "-old line"))
        assertTrue(diff.has(code, SyntaxRole.Markup, "+new line"))
    }

    @Test
    fun htmlAliasesResolveAndHighlightMarkupStructure() {
        val code = """
            <!-- NoteKit preview card -->
            <article class="note-card" data-count=4>
              <h1>MarkdownMP</h1>
              <a href="/docs" aria-label="Open docs">Docs</a>
              <script>
                const skipped = true;
              </script>
            </article>
        """.trimIndent()
        val html = defaultTestEngine().tokenize(code, "htm")

        assertTrue(html.has(code, SyntaxRole.Comment, "<!-- NoteKit preview card -->"))
        assertTrue(html.has(code, SyntaxRole.Tag, "article"))
        assertTrue(html.has(code, SyntaxRole.Attribute, "class"))
        assertTrue(html.has(code, SyntaxRole.Attribute, "data-count"))
        assertTrue(html.has(code, SyntaxRole.String, "\"note-card\""))
        assertTrue(html.has(code, SyntaxRole.String, "4"))
        assertTrue(html.has(code, SyntaxRole.Tag, "script"))
        assertTrue(html.has(code, SyntaxRole.Keyword, "const"))
    }

    @Test
    fun cssAliasesResolveAndHighlightSelectorsDeclarationsAndValues() {
        val code = """
            /* Card shell */
            :root {
              --accent: #38a3ff;
            }

            .note-card, article[data-kind="status"] > a:hover {
              display: grid;
              grid-template-columns: minmax(0, 1fr);
              color: var(--accent);
              background: linear-gradient(180deg, #111827, transparent);
              margin: 0.5rem auto !important;
            }

            @media (max-width: 700px) {
              .note-card { display: block; }
            }
        """.trimIndent()
        val css = defaultTestEngine().tokenize(code, "css")

        assertTrue(css.has(code, SyntaxRole.Comment, "/* Card shell */"))
        assertTrue(css.has(code, SyntaxRole.Attribute, ".note-card"))
        assertTrue(css.has(code, SyntaxRole.Attribute, "data-kind"))
        assertTrue(css.has(code, SyntaxRole.Attribute, "hover"))
        assertTrue(css.has(code, SyntaxRole.Property, "--accent"))
        assertTrue(css.has(code, SyntaxRole.Property, "grid-template-columns"))
        assertTrue(css.has(code, SyntaxRole.Function, "minmax"))
        assertTrue(css.has(code, SyntaxRole.Function, "var"))
        assertTrue(css.has(code, SyntaxRole.Constant, "#38a3ff"))
        assertTrue(css.has(code, SyntaxRole.Constant, "!important"))
        assertTrue(css.has(code, SyntaxRole.Number, "0.5rem"))
        assertTrue(css.has(code, SyntaxRole.Keyword, "@media"))
    }

    @Test
    fun xmlAliasesResolveAndHighlightDeclarationsCdataAndAttributes() {
        val code = """
            <?xml version="1.0" encoding="utf-8"?>
            <!-- Android vector-style sample -->
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="24dp">
              <![CDATA[if (count < 4) keep text as character data]]>
              <path android:fillColor="#38A3FF" android:pathData="M4,4 L20,20" />
            </vector>
        """.trimIndent()
        val xml = defaultTestEngine().tokenize(code, "xml")

        assertTrue(xml.has(code, SyntaxRole.Annotation, "<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
        assertTrue(xml.has(code, SyntaxRole.Comment, "<!-- Android vector-style sample -->"))
        assertTrue(xml.has(code, SyntaxRole.Tag, "vector"))
        assertTrue(xml.has(code, SyntaxRole.Attribute, "xmlns:android"))
        assertTrue(xml.has(code, SyntaxRole.Attribute, "android:width"))
        assertTrue(xml.has(code, SyntaxRole.String, "\"24dp\""))
        assertTrue(xml.has(code, SyntaxRole.Markup, "<![CDATA["))
        assertTrue(xml.has(code, SyntaxRole.String, "if (count < 4) keep text as character data"))
        assertTrue(xml.has(code, SyntaxRole.Markup, "]]>"))
    }

    @Test
    fun iniPropertiesAndDotenvIdentitiesShareConfigScanner() {
        val iniCode = """
            ; local editor settings
            [editor.preview]
            enabled = true
            title = "NoteKit"
            retries: 3
            export API_URL=https://example.com
        """.trimIndent()
        val dotenvCode = """
            # local env
            export API_URL=https://example.com
            ENABLED=true
            RETRIES=3
        """.trimIndent()
        val ini = defaultTestEngine().tokenize(iniCode, "ini")
        val properties = defaultTestEngine().tokenize(iniCode, "properties")
        val dotenv = defaultTestEngine().tokenize(dotenvCode, "dotenv")
        val env = defaultTestEngine().tokenize(dotenvCode, "env")

        assertTrue(ini.has(iniCode, SyntaxRole.Comment, "; local editor settings"))
        assertTrue(ini.has(iniCode, SyntaxRole.Property, "editor.preview"))
        assertTrue(ini.has(iniCode, SyntaxRole.Property, "enabled"))
        assertTrue(ini.has(iniCode, SyntaxRole.Constant, "true"))
        assertTrue(ini.has(iniCode, SyntaxRole.String, "\"NoteKit\""))
        assertTrue(ini.has(iniCode, SyntaxRole.Keyword, "export"))
        assertTrue(properties.has(iniCode, SyntaxRole.Property, "enabled"))
        assertTrue(dotenv.has(dotenvCode, SyntaxRole.Keyword, "export"))
        assertTrue(dotenv.has(dotenvCode, SyntaxRole.Property, "API_URL"))
        assertTrue(dotenv.has(dotenvCode, SyntaxRole.Constant, "true"))
        assertTrue(env.has(dotenvCode, SyntaxRole.Number, "3"))
    }

    @Test
    fun dockerfileAndMakefileAliasesResolveAndHighlightBuildShape() {
        val dockerCode = """
            # Runtime image
            FROM --platform=${'$'}TARGETPLATFORM alpine:3.20 AS runtime
            ARG APP_HOME=/app
            COPY --from=builder /out/app ${'$'}APP_HOME/app
            RUN echo "ready" && chmod +x ${'$'}APP_HOME/app
        """.trimIndent()
        val makeCode = listOf(
            "# Build shortcuts",
            "OUT := build/app",
            "",
            "build: ${'$'}(OUT)",
            "\tgradle :sharedApp:compileKotlinJvm",
            "",
            "${'$'}(OUT):",
            "\tmkdir -p ${'$'}@",
        ).joinToString("\n")
        val docker = defaultTestEngine().tokenize(dockerCode, "docker")
        val make = defaultTestEngine().tokenize(makeCode, "make")

        assertTrue(docker.has(dockerCode, SyntaxRole.Comment, "# Runtime image"))
        assertTrue(docker.has(dockerCode, SyntaxRole.Keyword, "FROM"))
        assertTrue(docker.has(dockerCode, SyntaxRole.Attribute, "--platform"))
        assertTrue(docker.has(dockerCode, SyntaxRole.Variable, "${'$'}TARGETPLATFORM"))
        assertTrue(docker.has(dockerCode, SyntaxRole.String, "\"ready\""))
        assertTrue(make.has(makeCode, SyntaxRole.Comment, "# Build shortcuts"))
        assertTrue(make.has(makeCode, SyntaxRole.Property, "OUT"))
        assertTrue(make.has(makeCode, SyntaxRole.Function, "build"))
        assertTrue(make.has(makeCode, SyntaxRole.Variable, "${'$'}(OUT)"))
        assertTrue(make.has(makeCode, SyntaxRole.Function, "gradle"))
    }

    @Test
    fun graphQlAliasesResolveAndHighlightDomainShape() {
        val graphQlCode = """
            # Fetch a pinned note
            query Note(${'$'}id: ID!, ${'$'}includeBody: Boolean = true) {
              note(id: ${'$'}id) @include(if: ${'$'}includeBody) {
                title
                body
              }
            }
        """.trimIndent()
        val graphQl = defaultTestEngine().tokenize(graphQlCode, "gql")

        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Comment, "# Fetch a pinned note"))
        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Keyword, "query"))
        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Variable, "${'$'}id"))
        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Type, "ID"))
        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Annotation, "@include"))
        assertTrue(graphQl.has(graphQlCode, SyntaxRole.Property, "title"))
    }

    @Test
    fun pythonRubyPhpAliasesResolve() {
        val pythonCode = "def score(job):\n    return len(job.name) if job.enabled else 0"
        val rubyCode = "class Job\n  def score(count)\n    puts count if count > 0\n  end\nend"
        val phpCode = "<?php function score(array \$job): int { return count(\$job); }"
        val python = defaultTestEngine().tokenize(pythonCode, "py")
        val ruby = defaultTestEngine().tokenize(rubyCode, "rb")
        val php = defaultTestEngine().tokenize(phpCode, "php")

        assertTrue(python.has(pythonCode, SyntaxRole.Keyword, "def"))
        assertTrue(python.has(pythonCode, SyntaxRole.Function, "score"))
        assertTrue(python.has(pythonCode, SyntaxRole.Function, "len"))
        assertTrue(ruby.has(rubyCode, SyntaxRole.Keyword, "class"))
        assertTrue(ruby.has(rubyCode, SyntaxRole.Function, "puts"))
        assertTrue(php.has(phpCode, SyntaxRole.Type, "array"))
        assertTrue(php.has(phpCode, SyntaxRole.Variable, "\$job"))
    }

    @Test
    fun systemsLanguagesAliasesResolve() {
        val goCode = "func score(job Job) int { return len(job.Name) }"
        val rustCode = "pub fn score(job: Job) -> i32 { println!(\"{}\", job.count); return 1 }"
        val dartCode = "class Job { final String name; Job(this.name); }"
        val go = defaultTestEngine().tokenize(goCode, "golang")
        val rust = defaultTestEngine().tokenize(rustCode, "rs")
        val dart = defaultTestEngine().tokenize(dartCode, "dart")

        assertTrue(go.has(goCode, SyntaxRole.Keyword, "func"))
        assertTrue(go.has(goCode, SyntaxRole.Type, "int"))
        assertTrue(rust.has(rustCode, SyntaxRole.Keyword, "fn"))
        assertTrue(rust.has(rustCode, SyntaxRole.Function, "println"))
        assertTrue(dart.has(dartCode, SyntaxRole.Keyword, "class"))
        assertTrue(dart.has(dartCode, SyntaxRole.Type, "String"))
    }

    @Test
    fun cFamilyAliasesResolve() {
        val cCode = "#include <stdio.h>\nint score(int count) { return count + 1; }"
        val cppCode = "class Job { public: int score() const { return 1; } };"
        val csharpCode = "public class Job { public int Score() => Math.Max(0, Count); }"
        val c = defaultTestEngine().tokenize(cCode, "c")
        val cpp = defaultTestEngine().tokenize(cppCode, "c++")
        val csharp = defaultTestEngine().tokenize(csharpCode, "c#")

        assertTrue(c.has(cCode, SyntaxRole.Annotation, "#include <stdio.h>"))
        assertTrue(c.has(cCode, SyntaxRole.Type, "int"))
        assertTrue(cpp.has(cppCode, SyntaxRole.Keyword, "class"))
        assertTrue(cpp.has(cppCode, SyntaxRole.Type, "int"))
        assertTrue(csharp.has(csharpCode, SyntaxRole.Keyword, "public"))
        assertTrue(csharp.has(csharpCode, SyntaxRole.Function, "Score"))
    }

    @Test
    fun deferredV1LanguagesNoLongerResolveToBuiltIns() {
        val labels = listOf(
            "apacheconf", "htaccess", "dns", "zone", "elixir", "ex", "glsl", "groovy",
            "gradle", "json5", "less", "lua", "mdx", "objective-c", "objc", "perl",
            "pl", "r", "scala", "sc", "scss", "sass", "svelte", "vue", "terraform",
            "tf", "tfvars", "hcl", "astro", "svg",
        )

        labels.forEach { label ->
            assertTrue(
                defaultTestEngine().tokenize("keyword value = 1", label).isEmpty(),
                "Expected $label to be outside the V1 built-in language surface.",
            )
        }
    }
}
