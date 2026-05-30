package com.gallatinapps.syntaxmp.engine.language

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine
import kotlin.jvm.JvmInline

/**
 * Canonical identifier for a syntax language.
 *
 * Use built-in constants such as [Kotlin], [SyntaxTokenizerEngine.resolveLanguageId] for raw
 * host labels, and [fromString] for exact custom language ids.
 *
 * @property value Normalized language id string (trimmed, lowercased). Stable across SyntaxMP
 * versions for built-ins, so it's safe to use as a persistence or wire key.
 */
@JvmInline
public value class SyntaxLanguageId private constructor(
    public val value: String,
) {
    public companion object {
        /** JSON documents. */
        public val Json: SyntaxLanguageId = SyntaxLanguageId("json")
        /** YAML documents. */
        public val Yaml: SyntaxLanguageId = SyntaxLanguageId("yaml")
        /** TOML documents. */
        public val Toml: SyntaxLanguageId = SyntaxLanguageId("toml")
        /** CSV documents. */
        public val Csv: SyntaxLanguageId = SyntaxLanguageId("csv")
        /** Markdown documents. */
        public val Markdown: SyntaxLanguageId = SyntaxLanguageId("markdown")
        /** ANSI SQL documents. */
        public val Sql: SyntaxLanguageId = SyntaxLanguageId("sql")
        /** SQLite-flavored SQL documents. */
        public val Sqlite: SyntaxLanguageId = SyntaxLanguageId("sqlite")
        /** Unified diff and patch documents. */
        public val Diff: SyntaxLanguageId = SyntaxLanguageId("diff")
        /** CSS stylesheets. */
        public val Css: SyntaxLanguageId = SyntaxLanguageId("css")
        /** HTML documents. */
        public val Html: SyntaxLanguageId = SyntaxLanguageId("html")
        /** XML documents. */
        public val Xml: SyntaxLanguageId = SyntaxLanguageId("xml")
        /** INI, properties, and dotenv-style documents. */
        public val Ini: SyntaxLanguageId = SyntaxLanguageId("ini")
        /** JSON5 documents. */
        public val Json5: SyntaxLanguageId = SyntaxLanguageId("json5")
        /** Dockerfile and Containerfile documents. */
        public val Dockerfile: SyntaxLanguageId = SyntaxLanguageId("dockerfile")
        /** Makefile documents. */
        public val Makefile: SyntaxLanguageId = SyntaxLanguageId("makefile")
        /** Terraform configuration documents. */
        public val Terraform: SyntaxLanguageId = SyntaxLanguageId("terraform")
        /** Generic HCL configuration documents. */
        public val Hcl: SyntaxLanguageId = SyntaxLanguageId("hcl")
        /** GraphQL schema and query documents. */
        public val GraphQl: SyntaxLanguageId = SyntaxLanguageId("graphql")
        /** Protocol Buffer documents. */
        public val Protobuf: SyntaxLanguageId = SyntaxLanguageId("protobuf")
        /** PostgreSQL-flavored SQL documents. */
        public val Postgresql: SyntaxLanguageId = SyntaxLanguageId("postgresql")
        /** GLSL shader documents. */
        public val Glsl: SyntaxLanguageId = SyntaxLanguageId("glsl")
        /** Apache configuration documents. */
        public val ApacheConf: SyntaxLanguageId = SyntaxLanguageId("apacheconf")
        /** DNS zone file documents. */
        public val DnsZone: SyntaxLanguageId = SyntaxLanguageId("dns-zone")
        /** Objective-C source documents. */
        public val ObjectiveC: SyntaxLanguageId = SyntaxLanguageId("objective-c")
        /** Lua source documents. */
        public val Lua: SyntaxLanguageId = SyntaxLanguageId("lua")
        /** R source documents. */
        public val R: SyntaxLanguageId = SyntaxLanguageId("r")
        /** Scala source documents. */
        public val Scala: SyntaxLanguageId = SyntaxLanguageId("scala")
        /** Elixir source documents. */
        public val Elixir: SyntaxLanguageId = SyntaxLanguageId("elixir")
        /** Python source documents. */
        public val Python: SyntaxLanguageId = SyntaxLanguageId("python")
        /** Ruby source documents. */
        public val Ruby: SyntaxLanguageId = SyntaxLanguageId("ruby")
        /** PHP source documents. */
        public val Php: SyntaxLanguageId = SyntaxLanguageId("php")
        /** Go source documents. */
        public val Go: SyntaxLanguageId = SyntaxLanguageId("go")
        /** Rust source documents. */
        public val Rust: SyntaxLanguageId = SyntaxLanguageId("rust")
        /** Dart source documents. */
        public val Dart: SyntaxLanguageId = SyntaxLanguageId("dart")
        /** Groovy and Groovy Gradle source documents. */
        public val Groovy: SyntaxLanguageId = SyntaxLanguageId("groovy")
        /** C source documents. */
        public val C: SyntaxLanguageId = SyntaxLanguageId("c")
        /** C++ source documents. */
        public val Cpp: SyntaxLanguageId = SyntaxLanguageId("cpp")
        /** C# source documents. */
        public val CSharp: SyntaxLanguageId = SyntaxLanguageId("csharp")
        /** Perl source documents. */
        public val Perl: SyntaxLanguageId = SyntaxLanguageId("perl")
        /** Kotlin and Kotlin Gradle script documents. */
        public val Kotlin: SyntaxLanguageId = SyntaxLanguageId("kotlin")
        /** Swift source documents. */
        public val Swift: SyntaxLanguageId = SyntaxLanguageId("swift")
        /** Java source documents. */
        public val Java: SyntaxLanguageId = SyntaxLanguageId("java")
        /** JavaScript source documents. */
        public val JavaScript: SyntaxLanguageId = SyntaxLanguageId("javascript")
        /** TypeScript source documents. */
        public val TypeScript: SyntaxLanguageId = SyntaxLanguageId("typescript")
        /** JSX JavaScript documents with component markup. */
        public val Jsx: SyntaxLanguageId = SyntaxLanguageId("jsx")
        /** TSX TypeScript documents with component markup. */
        public val Tsx: SyntaxLanguageId = SyntaxLanguageId("tsx")
        /** MDX Markdown documents with JSX components. */
        public val Mdx: SyntaxLanguageId = SyntaxLanguageId("mdx")
        /** Vue single-file component documents. */
        public val Vue: SyntaxLanguageId = SyntaxLanguageId("vue")
        /** Svelte component documents. */
        public val Svelte: SyntaxLanguageId = SyntaxLanguageId("svelte")
        /** Astro component documents. */
        public val Astro: SyntaxLanguageId = SyntaxLanguageId("astro")
        /** POSIX shell source documents. */
        public val Shell: SyntaxLanguageId = SyntaxLanguageId("shell")
        /** SCSS stylesheet documents. */
        public val Scss: SyntaxLanguageId = SyntaxLanguageId("scss")
        /** Less stylesheet documents. */
        public val Less: SyntaxLanguageId = SyntaxLanguageId("less")
        /** PowerShell source documents. */
        public val PowerShell: SyntaxLanguageId = SyntaxLanguageId("powershell")

        /** All built-in SyntaxMP tokenizer languages. */
        public val BuiltIns: Set<SyntaxLanguageId> = setOf(
            Json,
            Yaml,
            Toml,
            Csv,
            Markdown,
            Sql,
            Sqlite,
            Diff,
            Css,
            Html,
            Xml,
            Ini,
            Json5,
            Dockerfile,
            Makefile,
            Terraform,
            Hcl,
            GraphQl,
            Protobuf,
            Postgresql,
            Glsl,
            ApacheConf,
            DnsZone,
            ObjectiveC,
            Lua,
            R,
            Scala,
            Elixir,
            Python,
            Ruby,
            Php,
            Go,
            Rust,
            Dart,
            Groovy,
            C,
            Cpp,
            CSharp,
            Perl,
            Kotlin,
            Swift,
            Java,
            JavaScript,
            TypeScript,
            Jsx,
            Tsx,
            Mdx,
            Vue,
            Svelte,
            Astro,
            Shell,
            Scss,
            Less,
            PowerShell,
        )

        internal fun resolve(label: String?): SyntaxLanguageId? {
            val normalized = label.normalizeLanguageValue() ?: return null
            return BuiltInAliases[normalized] ?: fromString(normalized)
        }

        /**
         * Creates an exact custom language id without alias resolution.
         *
         * @throws IllegalArgumentException if [value] is blank after trimming.
         */
        public fun fromString(value: String): SyntaxLanguageId =
            SyntaxLanguageId(
                value.normalizeLanguageValue()
                    ?: throw IllegalArgumentException("Syntax language id must not be blank."),
            )
    }
}

internal fun String?.normalizeLanguageValue(): String? =
    this
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }

private val BuiltInAliases: Map<String, SyntaxLanguageId> = mapOf(
    "js" to SyntaxLanguageId.JavaScript,
    "ts" to SyntaxLanguageId.TypeScript,
    "jsx" to SyntaxLanguageId.Jsx,
    "tsx" to SyntaxLanguageId.Tsx,
    "mdx" to SyntaxLanguageId.Mdx,
    "vue" to SyntaxLanguageId.Vue,
    "svelte" to SyntaxLanguageId.Svelte,
    "astro" to SyntaxLanguageId.Astro,
    "kt" to SyntaxLanguageId.Kotlin,
    "kts" to SyntaxLanguageId.Kotlin,
    "swift" to SyntaxLanguageId.Swift,
    "json" to SyntaxLanguageId.Json,
    "yml" to SyntaxLanguageId.Yaml,
    "yaml" to SyntaxLanguageId.Yaml,
    "toml" to SyntaxLanguageId.Toml,
    "csv" to SyntaxLanguageId.Csv,
    "md" to SyntaxLanguageId.Markdown,
    "markdown" to SyntaxLanguageId.Markdown,
    "sql" to SyntaxLanguageId.Sql,
    "sqlite" to SyntaxLanguageId.Sqlite,
    "sqlite3" to SyntaxLanguageId.Sqlite,
    "diff" to SyntaxLanguageId.Diff,
    "patch" to SyntaxLanguageId.Diff,
    "css" to SyntaxLanguageId.Css,
    "html" to SyntaxLanguageId.Html,
    "htm" to SyntaxLanguageId.Html,
    "xml" to SyntaxLanguageId.Xml,
    "svg" to SyntaxLanguageId.Xml,
    "ini" to SyntaxLanguageId.Ini,
    "properties" to SyntaxLanguageId.Ini,
    "env" to SyntaxLanguageId.Ini,
    "dotenv" to SyntaxLanguageId.Ini,
    "json5" to SyntaxLanguageId.Json5,
    "docker" to SyntaxLanguageId.Dockerfile,
    "dockerfile" to SyntaxLanguageId.Dockerfile,
    "containerfile" to SyntaxLanguageId.Dockerfile,
    "make" to SyntaxLanguageId.Makefile,
    "makefile" to SyntaxLanguageId.Makefile,
    "mk" to SyntaxLanguageId.Makefile,
    "terraform" to SyntaxLanguageId.Terraform,
    "tf" to SyntaxLanguageId.Terraform,
    "hcl" to SyntaxLanguageId.Hcl,
    "graphql" to SyntaxLanguageId.GraphQl,
    "gql" to SyntaxLanguageId.GraphQl,
    "proto" to SyntaxLanguageId.Protobuf,
    "protobuf" to SyntaxLanguageId.Protobuf,
    "pgsql" to SyntaxLanguageId.Postgresql,
    "postgres" to SyntaxLanguageId.Postgresql,
    "postgresql" to SyntaxLanguageId.Postgresql,
    "glsl" to SyntaxLanguageId.Glsl,
    "vert" to SyntaxLanguageId.Glsl,
    "frag" to SyntaxLanguageId.Glsl,
    "apache" to SyntaxLanguageId.ApacheConf,
    "apacheconf" to SyntaxLanguageId.ApacheConf,
    "apache-config" to SyntaxLanguageId.ApacheConf,
    "htaccess" to SyntaxLanguageId.ApacheConf,
    "dns" to SyntaxLanguageId.DnsZone,
    "zone" to SyntaxLanguageId.DnsZone,
    "bind" to SyntaxLanguageId.DnsZone,
    "objc" to SyntaxLanguageId.ObjectiveC,
    "objective-c" to SyntaxLanguageId.ObjectiveC,
    "objectivec" to SyntaxLanguageId.ObjectiveC,
    "lua" to SyntaxLanguageId.Lua,
    "r" to SyntaxLanguageId.R,
    "scala" to SyntaxLanguageId.Scala,
    "sc" to SyntaxLanguageId.Scala,
    "elixir" to SyntaxLanguageId.Elixir,
    "ex" to SyntaxLanguageId.Elixir,
    "py" to SyntaxLanguageId.Python,
    "python" to SyntaxLanguageId.Python,
    "rb" to SyntaxLanguageId.Ruby,
    "ruby" to SyntaxLanguageId.Ruby,
    "php" to SyntaxLanguageId.Php,
    "go" to SyntaxLanguageId.Go,
    "golang" to SyntaxLanguageId.Go,
    "rs" to SyntaxLanguageId.Rust,
    "rust" to SyntaxLanguageId.Rust,
    "dart" to SyntaxLanguageId.Dart,
    "groovy" to SyntaxLanguageId.Groovy,
    "gvy" to SyntaxLanguageId.Groovy,
    "gy" to SyntaxLanguageId.Groovy,
    "gradle" to SyntaxLanguageId.Groovy,
    "gradle.groovy" to SyntaxLanguageId.Groovy,
    "gradle.kts" to SyntaxLanguageId.Kotlin,
    "c" to SyntaxLanguageId.C,
    "h" to SyntaxLanguageId.C,
    "cc" to SyntaxLanguageId.Cpp,
    "c++" to SyntaxLanguageId.Cpp,
    "cpp" to SyntaxLanguageId.Cpp,
    "cxx" to SyntaxLanguageId.Cpp,
    "hpp" to SyntaxLanguageId.Cpp,
    "cs" to SyntaxLanguageId.CSharp,
    "c#" to SyntaxLanguageId.CSharp,
    "csharp" to SyntaxLanguageId.CSharp,
    "pl" to SyntaxLanguageId.Perl,
    "perl" to SyntaxLanguageId.Perl,
    "sh" to SyntaxLanguageId.Shell,
    "bash" to SyntaxLanguageId.Shell,
    "zsh" to SyntaxLanguageId.Shell,
    "shell" to SyntaxLanguageId.Shell,
    "scss" to SyntaxLanguageId.Scss,
    "sass" to SyntaxLanguageId.Scss,
    "less" to SyntaxLanguageId.Less,
    "ps" to SyntaxLanguageId.PowerShell,
    "ps1" to SyntaxLanguageId.PowerShell,
    "powershell" to SyntaxLanguageId.PowerShell,
)
