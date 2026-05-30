package com.gallatinapps.syntaxmp.engine.routing

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer
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

internal fun builtInTokenizers(): Map<SyntaxLanguageId, SyntaxTokenizer> =
    mapOf(
        SyntaxLanguageId.Json to SyntaxTokenizer(JsonTokenizer::tokenize),
        SyntaxLanguageId.Yaml to SyntaxTokenizer(YamlTokenizer::tokenize),
        SyntaxLanguageId.Toml to SyntaxTokenizer(TomlTokenizer::tokenize),
        SyntaxLanguageId.Csv to SyntaxTokenizer(CsvTokenizer::tokenize),
        SyntaxLanguageId.Markdown to SyntaxTokenizer(MarkdownTokenizer::tokenize),
        SyntaxLanguageId.Sql to SyntaxTokenizer(SqlTokenizer::tokenize),
        SyntaxLanguageId.Sqlite to SyntaxTokenizer(SqliteTokenizer::tokenize),
        SyntaxLanguageId.Diff to SyntaxTokenizer(DiffTokenizer::tokenize),
        SyntaxLanguageId.Css to SyntaxTokenizer(CssTokenizer::tokenize),
        SyntaxLanguageId.Html to SyntaxTokenizer(HtmlTokenizer::tokenize),
        SyntaxLanguageId.Xml to SyntaxTokenizer(XmlTokenizer::tokenize),
        SyntaxLanguageId.Ini to SyntaxTokenizer(IniTokenizer::tokenize),
        SyntaxLanguageId.Properties to SyntaxTokenizer(PropertiesTokenizer::tokenize),
        SyntaxLanguageId.Dotenv to SyntaxTokenizer(DotenvTokenizer::tokenize),
        SyntaxLanguageId.Dockerfile to SyntaxTokenizer(DockerfileTokenizer::tokenize),
        SyntaxLanguageId.Makefile to SyntaxTokenizer(MakefileTokenizer::tokenize),
        SyntaxLanguageId.GraphQl to SyntaxTokenizer(GraphQlTokenizer::tokenize),
        SyntaxLanguageId.Protobuf to SyntaxTokenizer(ProtobufTokenizer::tokenize),
        SyntaxLanguageId.Postgresql to SyntaxTokenizer(PostgresqlTokenizer::tokenize),
        SyntaxLanguageId.Python to SyntaxTokenizer(PythonTokenizer::tokenize),
        SyntaxLanguageId.Ruby to SyntaxTokenizer(RubyTokenizer::tokenize),
        SyntaxLanguageId.Php to SyntaxTokenizer(PhpTokenizer::tokenize),
        SyntaxLanguageId.Go to SyntaxTokenizer(GoTokenizer::tokenize),
        SyntaxLanguageId.Rust to SyntaxTokenizer(RustTokenizer::tokenize),
        SyntaxLanguageId.Dart to SyntaxTokenizer(DartTokenizer::tokenize),
        SyntaxLanguageId.C to SyntaxTokenizer(CTokenizer::tokenize),
        SyntaxLanguageId.Cpp to SyntaxTokenizer(CppTokenizer::tokenize),
        SyntaxLanguageId.CSharp to SyntaxTokenizer(CSharpTokenizer::tokenize),
        SyntaxLanguageId.Kotlin to SyntaxTokenizer(KotlinTokenizer::tokenize),
        SyntaxLanguageId.Swift to SyntaxTokenizer(SwiftTokenizer::tokenize),
        SyntaxLanguageId.Java to SyntaxTokenizer(JavaTokenizer::tokenize),
        SyntaxLanguageId.JavaScript to SyntaxTokenizer(JavaScriptTokenizer::tokenize),
        SyntaxLanguageId.TypeScript to SyntaxTokenizer(TypeScriptTokenizer::tokenize),
        SyntaxLanguageId.Jsx to SyntaxTokenizer(JsxTokenizer::tokenize),
        SyntaxLanguageId.Tsx to SyntaxTokenizer(TsxTokenizer::tokenize),
        SyntaxLanguageId.Shell to SyntaxTokenizer(ShellTokenizer::tokenize),
        SyntaxLanguageId.Bash to SyntaxTokenizer(BashTokenizer::tokenize),
        SyntaxLanguageId.Zsh to SyntaxTokenizer(ZshTokenizer::tokenize),
        SyntaxLanguageId.PowerShell to SyntaxTokenizer(PowerShellTokenizer::tokenize),
    )
