package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.demo.model.samples.*
import com.gallatinapps.syntaxmp.language.LanguageId

internal object DemoLanguageCatalog {
    val DefaultDemoLanguageId: LanguageId = LanguageId.Kotlin

    val Languages: List<DemoLanguage> = listOf(
        DemoLanguage(
            id = LanguageId.Json,
            displayName = "JSON",
            sample = JsonSample,
        ),
        DemoLanguage(
            id = LanguageId.Yaml,
            displayName = "YAML",
            aliases = listOf("yml"),
            sample = YamlSample,
        ),
        DemoLanguage(
            id = LanguageId.Toml,
            displayName = "TOML",
            sample = TomlSample,
        ),
        DemoLanguage(
            id = LanguageId.Csv,
            displayName = "CSV",
            sample = CsvSample,
        ),
        DemoLanguage(
            id = LanguageId.Markdown,
            displayName = "Markdown",
            aliases = listOf("md"),
            sample = MarkdownSample,
        ),
        DemoLanguage(
            id = LanguageId.Sql,
            displayName = "SQL",
            sample = SqlSample,
        ),
        DemoLanguage(
            id = LanguageId.Sqlite,
            displayName = "SQLite",
            aliases = listOf("sqlite3"),
            sample = SqliteSample,
        ),
        DemoLanguage(
            id = LanguageId.Diff,
            displayName = "Diff",
            aliases = listOf("patch"),
            sample = DiffSample,
        ),
        DemoLanguage(
            id = LanguageId.Css,
            displayName = "CSS",
            sample = CssSample,
        ),
        DemoLanguage(
            id = LanguageId.Html,
            displayName = "HTML",
            aliases = listOf("htm"),
            sample = HtmlSample,
        ),
        DemoLanguage(
            id = LanguageId.Xml,
            displayName = "XML",
            sample = XmlSample,
        ),
        DemoLanguage(
            id = LanguageId.Ini,
            displayName = "INI",
            sample = IniSample,
        ),
        DemoLanguage(
            id = LanguageId.Properties,
            displayName = "Properties",
            sample = PropertiesSample,
        ),
        DemoLanguage(
            id = LanguageId.Dotenv,
            displayName = "Dotenv",
            aliases = listOf("env"),
            sample = DotenvSample,
        ),
        DemoLanguage(
            id = LanguageId.Dockerfile,
            displayName = "Dockerfile",
            aliases = listOf("containerfile"),
            sample = DockerfileSample,
        ),
        DemoLanguage(
            id = LanguageId.Makefile,
            displayName = "Makefile",
            aliases = listOf("make", "mk"),
            sample = MakefileSample,
        ),
        DemoLanguage(
            id = LanguageId.GraphQl,
            displayName = "GraphQL",
            aliases = listOf("gql"),
            sample = GraphQlSample,
        ),
        DemoLanguage(
            id = LanguageId.Protobuf,
            displayName = "Protocol Buffers",
            aliases = listOf("proto"),
            sample = ProtobufSample,
        ),
        DemoLanguage(
            id = LanguageId.Postgresql,
            displayName = "PostgreSQL",
            aliases = listOf("pgsql", "postgres"),
            sample = PostgresqlSample,
        ),
        DemoLanguage(
            id = LanguageId.Python,
            displayName = "Python",
            aliases = listOf("py"),
            sample = PythonSample,
        ),
        DemoLanguage(
            id = LanguageId.Ruby,
            displayName = "Ruby",
            aliases = listOf("rb"),
            sample = RubySample,
        ),
        DemoLanguage(
            id = LanguageId.Php,
            displayName = "PHP",
            sample = PhpSample,
        ),
        DemoLanguage(
            id = LanguageId.Go,
            displayName = "Go",
            aliases = listOf("golang"),
            sample = GoSample,
        ),
        DemoLanguage(
            id = LanguageId.Rust,
            displayName = "Rust",
            aliases = listOf("rs"),
            sample = RustSample,
        ),
        DemoLanguage(
            id = LanguageId.Dart,
            displayName = "Dart",
            sample = DartSample,
        ),
        DemoLanguage(
            id = LanguageId.C,
            displayName = "C",
            aliases = listOf("h"),
            sample = CSample,
        ),
        DemoLanguage(
            id = LanguageId.Cpp,
            displayName = "C++",
            aliases = listOf("cpp", "hpp"),
            sample = CppSample,
        ),
        DemoLanguage(
            id = LanguageId.CSharp,
            displayName = "C#",
            aliases = listOf("cs", "csharp"),
            sample = CSharpSample,
        ),
        DemoLanguage(
            id = LanguageId.Kotlin,
            displayName = "Kotlin",
            aliases = listOf("kt", "kts"),
            sample = KotlinSample,
        ),
        DemoLanguage(
            id = LanguageId.Swift,
            displayName = "Swift",
            sample = SwiftSample,
        ),
        DemoLanguage(
            id = LanguageId.Java,
            displayName = "Java",
            sample = JavaSample,
        ),
        DemoLanguage(
            id = LanguageId.JavaScript,
            displayName = "JavaScript",
            aliases = listOf("js"),
            sample = JavaScriptSample,
        ),
        DemoLanguage(
            id = LanguageId.TypeScript,
            displayName = "TypeScript",
            aliases = listOf("ts"),
            sample = TypeScriptSample,
        ),
        DemoLanguage(
            id = LanguageId.Jsx,
            displayName = "JSX",
            sample = JsxSample,
        ),
        DemoLanguage(
            id = LanguageId.Tsx,
            displayName = "TSX",
            sample = TsxSample,
        ),
        DemoLanguage(
            id = LanguageId.Shell,
            displayName = "Shell",
            aliases = listOf("sh"),
            sample = ShellSample,
        ),
        DemoLanguage(
            id = LanguageId.Bash,
            displayName = "Bash",
            sample = BashSample,
        ),
        DemoLanguage(
            id = LanguageId.Zsh,
            displayName = "Zsh",
            sample = ZshSample,
        ),
        DemoLanguage(
            id = LanguageId.PowerShell,
            displayName = "PowerShell",
            aliases = listOf("ps1"),
            sample = PowerShellSample,
        ),
    )

    fun defaultLanguage(languages: List<DemoLanguage> = Languages): DemoLanguage? =
        languages.firstOrNull { it.id == DefaultDemoLanguageId } ?: languages.firstOrNull()

    fun languageById(languageId: String): DemoLanguage? {
        val normalizedId = languageId.trim().lowercase()
        return Languages.firstOrNull { it.id.value == normalizedId }
    }

    fun languageByRouteSegment(segment: String): DemoLanguage? {
        val normalizedSegment = segment.trim().lowercase()
        return Languages.firstOrNull { language ->
            language.routeSegment == normalizedSegment || normalizedSegment in language.aliases
        }
    }
}
