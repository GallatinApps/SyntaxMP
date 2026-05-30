package com.gallatinapps.syntaxmp.engine.language

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine
import kotlin.jvm.JvmInline

/**
 * Canonical public identity for a syntax language.
 *
 * Use built-in constants such as [Kotlin], [SyntaxTokenizerEngine.resolveLanguageId] for raw
 * host labels, and [fromString] for exact custom language ids.
 *
 * [value] is the resolved identity emitted on token spans and used for language-specific theme
 * overrides. It is not the persistence contract for user-selected or file-derived language
 * choices: store the original label (for example, `bash`, `dotenv`, or `xml`) and resolve it again
 * at render time.
 *
 * @property value Normalized language id string (trimmed, lowercased).
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
        /** INI documents. */
        public val Ini: SyntaxLanguageId = SyntaxLanguageId("ini")
        /** Java properties documents. */
        public val Properties: SyntaxLanguageId = SyntaxLanguageId("properties")
        /** Dotenv documents. */
        public val Dotenv: SyntaxLanguageId = SyntaxLanguageId("dotenv")
        /** Dockerfile and Containerfile documents. */
        public val Dockerfile: SyntaxLanguageId = SyntaxLanguageId("dockerfile")
        /** Makefile documents. */
        public val Makefile: SyntaxLanguageId = SyntaxLanguageId("makefile")
        /** GraphQL schema and query documents. */
        public val GraphQl: SyntaxLanguageId = SyntaxLanguageId("graphql")
        /** Protocol Buffer documents. */
        public val Protobuf: SyntaxLanguageId = SyntaxLanguageId("protobuf")
        /** PostgreSQL-flavored SQL documents. */
        public val Postgresql: SyntaxLanguageId = SyntaxLanguageId("postgresql")
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
        /** C source documents. */
        public val C: SyntaxLanguageId = SyntaxLanguageId("c")
        /** C++ source documents. */
        public val Cpp: SyntaxLanguageId = SyntaxLanguageId("cpp")
        /** C# source documents. */
        public val CSharp: SyntaxLanguageId = SyntaxLanguageId("csharp")
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
        /** POSIX shell source documents. */
        public val Shell: SyntaxLanguageId = SyntaxLanguageId("shell")
        /** Bash shell source documents. */
        public val Bash: SyntaxLanguageId = SyntaxLanguageId("bash")
        /** Zsh shell source documents. */
        public val Zsh: SyntaxLanguageId = SyntaxLanguageId("zsh")
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
            Properties,
            Dotenv,
            Dockerfile,
            Makefile,
            GraphQl,
            Protobuf,
            Postgresql,
            Python,
            Ruby,
            Php,
            Go,
            Rust,
            Dart,
            C,
            Cpp,
            CSharp,
            Kotlin,
            Swift,
            Java,
            JavaScript,
            TypeScript,
            Jsx,
            Tsx,
            Shell,
            Bash,
            Zsh,
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
    "ini" to SyntaxLanguageId.Ini,
    "properties" to SyntaxLanguageId.Properties,
    "env" to SyntaxLanguageId.Dotenv,
    "dotenv" to SyntaxLanguageId.Dotenv,
    "docker" to SyntaxLanguageId.Dockerfile,
    "dockerfile" to SyntaxLanguageId.Dockerfile,
    "containerfile" to SyntaxLanguageId.Dockerfile,
    "make" to SyntaxLanguageId.Makefile,
    "makefile" to SyntaxLanguageId.Makefile,
    "mk" to SyntaxLanguageId.Makefile,
    "graphql" to SyntaxLanguageId.GraphQl,
    "gql" to SyntaxLanguageId.GraphQl,
    "proto" to SyntaxLanguageId.Protobuf,
    "protobuf" to SyntaxLanguageId.Protobuf,
    "pgsql" to SyntaxLanguageId.Postgresql,
    "postgres" to SyntaxLanguageId.Postgresql,
    "postgresql" to SyntaxLanguageId.Postgresql,
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
    "sh" to SyntaxLanguageId.Shell,
    "shell" to SyntaxLanguageId.Shell,
    "bash" to SyntaxLanguageId.Bash,
    "zsh" to SyntaxLanguageId.Zsh,
    "ps" to SyntaxLanguageId.PowerShell,
    "ps1" to SyntaxLanguageId.PowerShell,
    "powershell" to SyntaxLanguageId.PowerShell,
)
