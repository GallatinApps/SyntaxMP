package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.demo.model.samples.*
import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId

internal object DemoLanguageCatalog {
    val DefaultDemoLanguageId: SyntaxLanguageId = SyntaxLanguageId.Kotlin

    val Languages: List<DemoLanguage> = listOf(
        DemoLanguage(
            id = SyntaxLanguageId.Json,
            displayName = "JSON",
            sample = JsonSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Yaml,
            displayName = "YAML",
            aliases = listOf("yml"),
            sample = YamlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Toml,
            displayName = "TOML",
            sample = TomlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Csv,
            displayName = "CSV",
            sample = CsvSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Markdown,
            displayName = "Markdown",
            aliases = listOf("md"),
            sample = MarkdownSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Sql,
            displayName = "SQL",
            sample = SqlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Sqlite,
            displayName = "SQLite",
            aliases = listOf("sqlite3"),
            sample = SqliteSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Diff,
            displayName = "Diff",
            aliases = listOf("patch"),
            sample = DiffSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Css,
            displayName = "CSS",
            sample = CssSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Html,
            displayName = "HTML",
            aliases = listOf("htm"),
            sample = HtmlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Xml,
            displayName = "XML",
            sample = XmlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Ini,
            displayName = "INI",
            sample = IniSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Properties,
            displayName = "Properties",
            sample = PropertiesSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Dotenv,
            displayName = "Dotenv",
            aliases = listOf("env"),
            sample = DotenvSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Dockerfile,
            displayName = "Dockerfile",
            aliases = listOf("containerfile"),
            sample = DockerfileSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Makefile,
            displayName = "Makefile",
            aliases = listOf("make", "mk"),
            sample = MakefileSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.GraphQl,
            displayName = "GraphQL",
            aliases = listOf("gql"),
            sample = GraphQlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Protobuf,
            displayName = "Protocol Buffers",
            aliases = listOf("proto"),
            sample = ProtobufSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Postgresql,
            displayName = "PostgreSQL",
            aliases = listOf("pgsql", "postgres"),
            sample = PostgresqlSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Python,
            displayName = "Python",
            aliases = listOf("py"),
            sample = PythonSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Ruby,
            displayName = "Ruby",
            aliases = listOf("rb"),
            sample = RubySample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Php,
            displayName = "PHP",
            sample = PhpSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Go,
            displayName = "Go",
            aliases = listOf("golang"),
            sample = GoSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Rust,
            displayName = "Rust",
            aliases = listOf("rs"),
            sample = RustSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Dart,
            displayName = "Dart",
            sample = DartSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.C,
            displayName = "C",
            aliases = listOf("h"),
            sample = CSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Cpp,
            displayName = "C++",
            aliases = listOf("cpp", "hpp"),
            sample = CppSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.CSharp,
            displayName = "C#",
            aliases = listOf("cs", "csharp"),
            sample = CSharpSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Kotlin,
            displayName = "Kotlin",
            aliases = listOf("kt", "kts"),
            sample = KotlinSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Swift,
            displayName = "Swift",
            sample = SwiftSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Java,
            displayName = "Java",
            sample = JavaSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.JavaScript,
            displayName = "JavaScript",
            aliases = listOf("js"),
            sample = JavaScriptSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.TypeScript,
            displayName = "TypeScript",
            aliases = listOf("ts"),
            sample = TypeScriptSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Jsx,
            displayName = "JSX",
            sample = JsxSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Tsx,
            displayName = "TSX",
            sample = TsxSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Shell,
            displayName = "Shell",
            aliases = listOf("sh"),
            sample = ShellSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Bash,
            displayName = "Bash",
            sample = BashSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.Zsh,
            displayName = "Zsh",
            sample = ZshSample,
        ),
        DemoLanguage(
            id = SyntaxLanguageId.PowerShell,
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
