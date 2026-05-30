package com.gallatinapps.syntaxmp.engine.routing

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.languages.apacheconf.ApacheConfTokenizer
import com.gallatinapps.syntaxmp.languages.astro.AstroTokenizer
import com.gallatinapps.syntaxmp.languages.csharp.CSharpTokenizer
import com.gallatinapps.syntaxmp.languages.c.CTokenizer
import com.gallatinapps.syntaxmp.languages.cpp.CppTokenizer
import com.gallatinapps.syntaxmp.languages.css.CssTokenizer
import com.gallatinapps.syntaxmp.languages.csv.CsvTokenizer
import com.gallatinapps.syntaxmp.languages.dart.DartTokenizer
import com.gallatinapps.syntaxmp.languages.diff.DiffTokenizer
import com.gallatinapps.syntaxmp.languages.dnszone.DnsZoneTokenizer
import com.gallatinapps.syntaxmp.languages.dockerfile.DockerfileTokenizer
import com.gallatinapps.syntaxmp.languages.elixir.ElixirTokenizer
import com.gallatinapps.syntaxmp.languages.glsl.GlslTokenizer
import com.gallatinapps.syntaxmp.languages.go.GoTokenizer
import com.gallatinapps.syntaxmp.languages.graphql.GraphQlTokenizer
import com.gallatinapps.syntaxmp.languages.groovy.GroovyTokenizer
import com.gallatinapps.syntaxmp.languages.hcl.HclTokenizer
import com.gallatinapps.syntaxmp.languages.html.HtmlTokenizer
import com.gallatinapps.syntaxmp.languages.ini.IniTokenizer
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptTokenizer
import com.gallatinapps.syntaxmp.languages.java.JavaTokenizer
import com.gallatinapps.syntaxmp.languages.json5.Json5Tokenizer
import com.gallatinapps.syntaxmp.languages.json.JsonTokenizer
import com.gallatinapps.syntaxmp.languages.jsx.JsxTokenizer
import com.gallatinapps.syntaxmp.languages.kotlin.KotlinTokenizer
import com.gallatinapps.syntaxmp.languages.less.LessTokenizer
import com.gallatinapps.syntaxmp.languages.lua.LuaTokenizer
import com.gallatinapps.syntaxmp.languages.makefile.MakefileTokenizer
import com.gallatinapps.syntaxmp.languages.markdown.MarkdownTokenizer
import com.gallatinapps.syntaxmp.languages.mdx.MdxTokenizer
import com.gallatinapps.syntaxmp.languages.objectivec.ObjectiveCTokenizer
import com.gallatinapps.syntaxmp.languages.perl.PerlTokenizer
import com.gallatinapps.syntaxmp.languages.php.PhpTokenizer
import com.gallatinapps.syntaxmp.languages.postgresql.PostgresqlTokenizer
import com.gallatinapps.syntaxmp.languages.powershell.PowerShellTokenizer
import com.gallatinapps.syntaxmp.languages.protobuf.ProtobufTokenizer
import com.gallatinapps.syntaxmp.languages.python.PythonTokenizer
import com.gallatinapps.syntaxmp.languages.r.RTokenizer
import com.gallatinapps.syntaxmp.languages.ruby.RubyTokenizer
import com.gallatinapps.syntaxmp.languages.rust.RustTokenizer
import com.gallatinapps.syntaxmp.languages.scala.ScalaTokenizer
import com.gallatinapps.syntaxmp.languages.scss.ScssTokenizer
import com.gallatinapps.syntaxmp.languages.shell.ShellTokenizer
import com.gallatinapps.syntaxmp.languages.sqlite.SqliteTokenizer
import com.gallatinapps.syntaxmp.languages.sql.SqlTokenizer
import com.gallatinapps.syntaxmp.languages.svelte.SvelteTokenizer
import com.gallatinapps.syntaxmp.languages.swift.SwiftTokenizer
import com.gallatinapps.syntaxmp.languages.terraform.TerraformTokenizer
import com.gallatinapps.syntaxmp.languages.toml.TomlTokenizer
import com.gallatinapps.syntaxmp.languages.tsx.TsxTokenizer
import com.gallatinapps.syntaxmp.languages.typescript.TypeScriptTokenizer
import com.gallatinapps.syntaxmp.languages.vue.VueTokenizer
import com.gallatinapps.syntaxmp.languages.xml.XmlTokenizer
import com.gallatinapps.syntaxmp.languages.yaml.YamlTokenizer

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
        SyntaxLanguageId.Json5 to SyntaxTokenizer(Json5Tokenizer::tokenize),
        SyntaxLanguageId.Dockerfile to SyntaxTokenizer(DockerfileTokenizer::tokenize),
        SyntaxLanguageId.Makefile to SyntaxTokenizer(MakefileTokenizer::tokenize),
        SyntaxLanguageId.Terraform to SyntaxTokenizer(TerraformTokenizer::tokenize),
        SyntaxLanguageId.Hcl to SyntaxTokenizer(HclTokenizer::tokenize),
        SyntaxLanguageId.GraphQl to SyntaxTokenizer(GraphQlTokenizer::tokenize),
        SyntaxLanguageId.Protobuf to SyntaxTokenizer(ProtobufTokenizer::tokenize),
        SyntaxLanguageId.Postgresql to SyntaxTokenizer(PostgresqlTokenizer::tokenize),
        SyntaxLanguageId.Glsl to SyntaxTokenizer(GlslTokenizer::tokenize),
        SyntaxLanguageId.ApacheConf to SyntaxTokenizer(ApacheConfTokenizer::tokenize),
        SyntaxLanguageId.DnsZone to SyntaxTokenizer(DnsZoneTokenizer::tokenize),
        SyntaxLanguageId.ObjectiveC to SyntaxTokenizer(ObjectiveCTokenizer::tokenize),
        SyntaxLanguageId.Lua to SyntaxTokenizer(LuaTokenizer::tokenize),
        SyntaxLanguageId.R to SyntaxTokenizer(RTokenizer::tokenize),
        SyntaxLanguageId.Scala to SyntaxTokenizer(ScalaTokenizer::tokenize),
        SyntaxLanguageId.Elixir to SyntaxTokenizer(ElixirTokenizer::tokenize),
        SyntaxLanguageId.Python to SyntaxTokenizer(PythonTokenizer::tokenize),
        SyntaxLanguageId.Ruby to SyntaxTokenizer(RubyTokenizer::tokenize),
        SyntaxLanguageId.Php to SyntaxTokenizer(PhpTokenizer::tokenize),
        SyntaxLanguageId.Go to SyntaxTokenizer(GoTokenizer::tokenize),
        SyntaxLanguageId.Rust to SyntaxTokenizer(RustTokenizer::tokenize),
        SyntaxLanguageId.Dart to SyntaxTokenizer(DartTokenizer::tokenize),
        SyntaxLanguageId.Groovy to SyntaxTokenizer(GroovyTokenizer::tokenize),
        SyntaxLanguageId.C to SyntaxTokenizer(CTokenizer::tokenize),
        SyntaxLanguageId.Cpp to SyntaxTokenizer(CppTokenizer::tokenize),
        SyntaxLanguageId.CSharp to SyntaxTokenizer(CSharpTokenizer::tokenize),
        SyntaxLanguageId.Perl to SyntaxTokenizer(PerlTokenizer::tokenize),
        SyntaxLanguageId.Kotlin to SyntaxTokenizer(KotlinTokenizer::tokenize),
        SyntaxLanguageId.Swift to SyntaxTokenizer(SwiftTokenizer::tokenize),
        SyntaxLanguageId.Java to SyntaxTokenizer(JavaTokenizer::tokenize),
        SyntaxLanguageId.JavaScript to SyntaxTokenizer(JavaScriptTokenizer::tokenize),
        SyntaxLanguageId.TypeScript to SyntaxTokenizer(TypeScriptTokenizer::tokenize),
        SyntaxLanguageId.Jsx to SyntaxTokenizer(JsxTokenizer::tokenize),
        SyntaxLanguageId.Tsx to SyntaxTokenizer(TsxTokenizer::tokenize),
        SyntaxLanguageId.Mdx to SyntaxTokenizer(MdxTokenizer::tokenize),
        SyntaxLanguageId.Vue to SyntaxTokenizer(VueTokenizer::tokenize),
        SyntaxLanguageId.Svelte to SyntaxTokenizer(SvelteTokenizer::tokenize),
        SyntaxLanguageId.Astro to SyntaxTokenizer(AstroTokenizer::tokenize),
        SyntaxLanguageId.Shell to SyntaxTokenizer(ShellTokenizer::tokenize),
        SyntaxLanguageId.Scss to SyntaxTokenizer(ScssTokenizer::tokenize),
        SyntaxLanguageId.Less to SyntaxTokenizer(LessTokenizer::tokenize),
        SyntaxLanguageId.PowerShell to SyntaxTokenizer(PowerShellTokenizer::tokenize),
    )
