package com.gallatinapps.syntaxmp.routing

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.builtins.bash.BashTokenizer
import com.gallatinapps.syntaxmp.builtins.csharp.CSharpTokenizer
import com.gallatinapps.syntaxmp.builtins.c.CTokenizer
import com.gallatinapps.syntaxmp.builtins.cpp.CppTokenizer
import com.gallatinapps.syntaxmp.builtins.css.CssTokenizer
import com.gallatinapps.syntaxmp.builtins.csv.CsvTokenizer
import com.gallatinapps.syntaxmp.builtins.dart.DartTokenizer
import com.gallatinapps.syntaxmp.builtins.diff.DiffTokenizer
import com.gallatinapps.syntaxmp.builtins.dockerfile.DockerfileTokenizer
import com.gallatinapps.syntaxmp.builtins.dotenv.DotenvTokenizer
import com.gallatinapps.syntaxmp.builtins.go.GoTokenizer
import com.gallatinapps.syntaxmp.builtins.graphql.GraphQlTokenizer
import com.gallatinapps.syntaxmp.builtins.html.HtmlTokenizer
import com.gallatinapps.syntaxmp.builtins.ini.IniTokenizer
import com.gallatinapps.syntaxmp.builtins.javascript.JavaScriptTokenizer
import com.gallatinapps.syntaxmp.builtins.java.JavaTokenizer
import com.gallatinapps.syntaxmp.builtins.json.JsonTokenizer
import com.gallatinapps.syntaxmp.builtins.jsx.JsxTokenizer
import com.gallatinapps.syntaxmp.builtins.kotlin.KotlinTokenizer
import com.gallatinapps.syntaxmp.builtins.makefile.MakefileTokenizer
import com.gallatinapps.syntaxmp.builtins.markdown.MarkdownTokenizer
import com.gallatinapps.syntaxmp.builtins.php.PhpTokenizer
import com.gallatinapps.syntaxmp.builtins.postgresql.PostgresqlTokenizer
import com.gallatinapps.syntaxmp.builtins.powershell.PowerShellTokenizer
import com.gallatinapps.syntaxmp.builtins.properties.PropertiesTokenizer
import com.gallatinapps.syntaxmp.builtins.protobuf.ProtobufTokenizer
import com.gallatinapps.syntaxmp.builtins.python.PythonTokenizer
import com.gallatinapps.syntaxmp.builtins.ruby.RubyTokenizer
import com.gallatinapps.syntaxmp.builtins.rust.RustTokenizer
import com.gallatinapps.syntaxmp.builtins.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.builtins.sqlite.SqliteTokenizer
import com.gallatinapps.syntaxmp.builtins.sql.SqlTokenizer
import com.gallatinapps.syntaxmp.builtins.swift.SwiftTokenizer
import com.gallatinapps.syntaxmp.builtins.toml.TomlTokenizer
import com.gallatinapps.syntaxmp.builtins.tsx.TsxTokenizer
import com.gallatinapps.syntaxmp.builtins.typescript.TypeScriptTokenizer
import com.gallatinapps.syntaxmp.builtins.xml.XmlTokenizer
import com.gallatinapps.syntaxmp.builtins.yaml.YamlTokenizer
import com.gallatinapps.syntaxmp.builtins.zsh.ZshTokenizer

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
