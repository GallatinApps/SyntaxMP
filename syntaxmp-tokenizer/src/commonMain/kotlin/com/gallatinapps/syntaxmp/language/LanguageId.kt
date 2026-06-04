package com.gallatinapps.syntaxmp.language

import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import kotlin.jvm.JvmInline

/**
 * Canonical public identity for a syntax language.
 *
 * Use built-in constants such as [Kotlin], [SyntaxTokenizer.resolveLanguageId] for raw
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
public value class LanguageId private constructor(
    public val value: String,
) {
    public companion object {
        /** JSON documents. */
        public val Json: LanguageId = LanguageId("json")
        /** YAML documents. */
        public val Yaml: LanguageId = LanguageId("yaml")
        /** TOML documents. */
        public val Toml: LanguageId = LanguageId("toml")
        /** CSV documents. */
        public val Csv: LanguageId = LanguageId("csv")
        /** Markdown documents. */
        public val Markdown: LanguageId = LanguageId("markdown")
        /** ANSI SQL documents. */
        public val Sql: LanguageId = LanguageId("sql")
        /** SQLite-flavored SQL documents. */
        public val Sqlite: LanguageId = LanguageId("sqlite")
        /** Unified diff and patch documents. */
        public val Diff: LanguageId = LanguageId("diff")
        /** CSS stylesheets. */
        public val Css: LanguageId = LanguageId("css")
        /** HTML documents. */
        public val Html: LanguageId = LanguageId("html")
        /** XML documents. */
        public val Xml: LanguageId = LanguageId("xml")
        /** INI documents. */
        public val Ini: LanguageId = LanguageId("ini")
        /** Java properties documents. */
        public val Properties: LanguageId = LanguageId("properties")
        /** Dotenv documents. */
        public val Dotenv: LanguageId = LanguageId("dotenv")
        /** Dockerfile and Containerfile documents. */
        public val Dockerfile: LanguageId = LanguageId("dockerfile")
        /** Makefile documents. */
        public val Makefile: LanguageId = LanguageId("makefile")
        /** GraphQL schema and query documents. */
        public val GraphQl: LanguageId = LanguageId("graphql")
        /** Protocol Buffer documents. */
        public val Protobuf: LanguageId = LanguageId("protobuf")
        /** PostgreSQL-flavored SQL documents. */
        public val Postgresql: LanguageId = LanguageId("postgresql")
        /** Python source documents. */
        public val Python: LanguageId = LanguageId("python")
        /** Ruby source documents. */
        public val Ruby: LanguageId = LanguageId("ruby")
        /** PHP source documents. */
        public val Php: LanguageId = LanguageId("php")
        /** Go source documents. */
        public val Go: LanguageId = LanguageId("go")
        /** Rust source documents. */
        public val Rust: LanguageId = LanguageId("rust")
        /** Dart source documents. */
        public val Dart: LanguageId = LanguageId("dart")
        /** C source documents. */
        public val C: LanguageId = LanguageId("c")
        /** C++ source documents. */
        public val Cpp: LanguageId = LanguageId("cpp")
        /** C# source documents. */
        public val CSharp: LanguageId = LanguageId("csharp")
        /** Kotlin and Kotlin Gradle script documents. */
        public val Kotlin: LanguageId = LanguageId("kotlin")
        /** Swift source documents. */
        public val Swift: LanguageId = LanguageId("swift")
        /** Java source documents. */
        public val Java: LanguageId = LanguageId("java")
        /** JavaScript source documents. */
        public val JavaScript: LanguageId = LanguageId("javascript")
        /** TypeScript source documents. */
        public val TypeScript: LanguageId = LanguageId("typescript")
        /** JSX JavaScript documents with component markup. */
        public val Jsx: LanguageId = LanguageId("jsx")
        /** TSX TypeScript documents with component markup. */
        public val Tsx: LanguageId = LanguageId("tsx")
        /** POSIX shell source documents. */
        public val Shell: LanguageId = LanguageId("shell")
        /** Bash shell source documents. */
        public val Bash: LanguageId = LanguageId("bash")
        /** Zsh shell source documents. */
        public val Zsh: LanguageId = LanguageId("zsh")
        /** PowerShell source documents. */
        public val PowerShell: LanguageId = LanguageId("powershell")

        /** All built-in SyntaxMP tokenizer languages. */
        public val BuiltIns: Set<LanguageId> = setOf(
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

        /**
         * Creates an exact custom language id without alias resolution.
         *
         * @throws IllegalArgumentException if [value] is blank after trimming.
         */
        public fun fromString(value: String): LanguageId =
            LanguageId(
                value.trimAndLowercaseOrNull()
                    ?: throw IllegalArgumentException("Syntax language id must not be blank."),
            )
    }
}

internal fun String?.trimAndLowercaseOrNull(): String? =
    this
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
