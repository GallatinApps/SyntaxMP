package com.gallatinapps.syntaxmp.engine.routing

import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.languages.bash.BashTokenizer
import com.gallatinapps.syntaxmp.languages.csharp.CSharpTokenizer
import com.gallatinapps.syntaxmp.languages.c.CTokenizer
import com.gallatinapps.syntaxmp.languages.cpp.CppTokenizer
import com.gallatinapps.syntaxmp.languages.css.CssTokenizer
import com.gallatinapps.syntaxmp.languages.csv.CsvTokenizer
import com.gallatinapps.syntaxmp.languages.dart.DartTokenizer
import com.gallatinapps.syntaxmp.languages.diff.DiffTokenizer
import com.gallatinapps.syntaxmp.languages.dockerfile.DockerfileTokenizer
import com.gallatinapps.syntaxmp.languages.dotenv.DotenvTokenizer
import com.gallatinapps.syntaxmp.languages.go.GoTokenizer
import com.gallatinapps.syntaxmp.languages.graphql.GraphQlTokenizer
import com.gallatinapps.syntaxmp.languages.html.HtmlTokenizer
import com.gallatinapps.syntaxmp.languages.ini.IniTokenizer
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptTokenizer
import com.gallatinapps.syntaxmp.languages.java.JavaTokenizer
import com.gallatinapps.syntaxmp.languages.json.JsonTokenizer
import com.gallatinapps.syntaxmp.languages.jsx.JsxTokenizer
import com.gallatinapps.syntaxmp.languages.kotlin.KotlinTokenizer
import com.gallatinapps.syntaxmp.languages.makefile.MakefileTokenizer
import com.gallatinapps.syntaxmp.languages.markdown.MarkdownTokenizer
import com.gallatinapps.syntaxmp.languages.php.PhpTokenizer
import com.gallatinapps.syntaxmp.languages.postgresql.PostgresqlTokenizer
import com.gallatinapps.syntaxmp.languages.powershell.PowerShellTokenizer
import com.gallatinapps.syntaxmp.languages.properties.PropertiesTokenizer
import com.gallatinapps.syntaxmp.languages.protobuf.ProtobufTokenizer
import com.gallatinapps.syntaxmp.languages.python.PythonTokenizer
import com.gallatinapps.syntaxmp.languages.ruby.RubyTokenizer
import com.gallatinapps.syntaxmp.languages.rust.RustTokenizer
import com.gallatinapps.syntaxmp.languages.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.languages.sqlite.SqliteTokenizer
import com.gallatinapps.syntaxmp.languages.sql.SqlTokenizer
import com.gallatinapps.syntaxmp.languages.swift.SwiftTokenizer
import com.gallatinapps.syntaxmp.languages.toml.TomlTokenizer
import com.gallatinapps.syntaxmp.languages.tsx.TsxTokenizer
import com.gallatinapps.syntaxmp.languages.typescript.TypeScriptTokenizer
import com.gallatinapps.syntaxmp.languages.xml.XmlTokenizer
import com.gallatinapps.syntaxmp.languages.yaml.YamlTokenizer
import com.gallatinapps.syntaxmp.languages.zsh.ZshTokenizer

internal fun builtInTokenizers(): Map<LanguageId, LanguageTokenizer> =
    mapOf(
        LanguageId.Json to JsonTokenizer,
        LanguageId.Yaml to YamlTokenizer,
        LanguageId.Toml to TomlTokenizer,
        LanguageId.Csv to CsvTokenizer,
        LanguageId.Markdown to MarkdownTokenizer,
        LanguageId.Sql to SqlTokenizer,
        LanguageId.Sqlite to SqliteTokenizer,
        LanguageId.Diff to DiffTokenizer,
        LanguageId.Css to CssTokenizer,
        LanguageId.Html to HtmlTokenizer,
        LanguageId.Xml to XmlTokenizer,
        LanguageId.Ini to IniTokenizer,
        LanguageId.Properties to PropertiesTokenizer,
        LanguageId.Dotenv to DotenvTokenizer,
        LanguageId.Dockerfile to DockerfileTokenizer,
        LanguageId.Makefile to MakefileTokenizer,
        LanguageId.GraphQl to GraphQlTokenizer,
        LanguageId.Protobuf to ProtobufTokenizer,
        LanguageId.Postgresql to PostgresqlTokenizer,
        LanguageId.Python to PythonTokenizer,
        LanguageId.Ruby to RubyTokenizer,
        LanguageId.Php to PhpTokenizer,
        LanguageId.Go to GoTokenizer,
        LanguageId.Rust to RustTokenizer,
        LanguageId.Dart to DartTokenizer,
        LanguageId.C to CTokenizer,
        LanguageId.Cpp to CppTokenizer,
        LanguageId.CSharp to CSharpTokenizer,
        LanguageId.Kotlin to KotlinTokenizer,
        LanguageId.Swift to SwiftTokenizer,
        LanguageId.Java to JavaTokenizer,
        LanguageId.JavaScript to JavaScriptTokenizer,
        LanguageId.TypeScript to TypeScriptTokenizer,
        LanguageId.Jsx to JsxTokenizer,
        LanguageId.Tsx to TsxTokenizer,
        LanguageId.Shell to ShellTokenizer,
        LanguageId.Bash to BashTokenizer,
        LanguageId.Zsh to ZshTokenizer,
        LanguageId.PowerShell to PowerShellTokenizer,
    )
