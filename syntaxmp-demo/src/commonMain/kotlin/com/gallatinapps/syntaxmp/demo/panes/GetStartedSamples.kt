package com.gallatinapps.syntaxmp.demo.panes

internal val InstallationCatalogSample = """
[versions]
syntaxmp = "0.2.0"

[libraries]
syntaxmp = { module = "com.gallatinapps.syntaxmp:syntaxmp", version.ref = "syntaxmp" }
""".trimIndent()

internal val InstallationBuildGradleSample = """
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.syntaxmp)
        }
    }
}
""".trimIndent()


internal val BasicTextSample = """
@Composable
fun CodeBlock(code: String, languageLabel: String) {
    val engine = remember { SyntaxTokenizer() }
    BasicText(
        text = rememberSyntaxAnnotatedString(
            code = code,
            languageLabel = languageLabel,
            engine = engine,
            theme = SyntaxTheme.DefaultDark,
        ),
        style = TextStyle(fontFamily = FontFamily.Monospace),
    )
}
""".trimIndent()

internal val EditableCodeSample = """
@Composable
fun CodeField(state: TextFieldState, languageLabel: String) {
    val engine = remember { SyntaxTokenizer() }
    val theme = SyntaxTheme.DefaultDark

    BasicTextField(
        state = state,
        outputTransformation = {
            val code = asCharSequence().toString()
            val spans = engine.tokenize(code = code, languageLabel = languageLabel)
            val styledSpans = buildSyntaxStyledSpans(
                code = code,
                spans = spans,
                theme = theme,
            )
            applySyntaxStyledSpans(styledSpans)
        },
        textStyle = TextStyle(fontFamily = FontFamily.Monospace),
    )
}
""".trimIndent()

internal val ThemeSample = """
val roleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF3B73D9)),
    SyntaxRole.Operator to SyntaxStyle(color = Color(0xFF4B5563)),
    SyntaxRole.Punctuation to SyntaxStyle(color = Color(0xFF6B7280)),
    SyntaxRole.Function to SyntaxStyle(color = Color(0xFF6F42C1)),
    SyntaxRole.Type to SyntaxStyle(color = Color(0xFFB45309)),
    SyntaxRole.Property to SyntaxStyle(color = Color(0xFF0F766E)),
    SyntaxRole.String to SyntaxStyle(color = Color(0xFF2E7D5B)),
    SyntaxRole.Number to SyntaxStyle(color = Color(0xFFAD3DA4)),
    SyntaxRole.Tag to SyntaxStyle(color = Color(0xFF22863A)),
    SyntaxRole.Attribute to SyntaxStyle(color = Color(0xFF6F42C1)),
    SyntaxRole.Comment to SyntaxStyle(
        color = Color(0xFF7A7F87),
        fontStyle = FontStyle.Italic,
    ),
)

val theme = SyntaxTheme(roleStyles = roleStyles)
BasicText(
    text = rememberSyntaxAnnotatedString(
        code = code,
        languageLabel = "kotlin",
        engine = engine,
        theme = theme,
    ),
    style = TextStyle(fontFamily = FontFamily.Monospace),
)
""".trimIndent()

internal val PerLanguageThemeSample = """
val myTheme = SyntaxTheme(
    roleStyles = SyntaxTheme.DefaultDark.roleStyles,
    languageOverrides = mapOf(
        LanguageId.Kotlin to SyntaxRoleStyles(
            SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF7F52FF)),
        ),
        LanguageId.Python to SyntaxRoleStyles(
            SyntaxRole.Keyword to SyntaxStyle(color = Color(0xFF7DCFFF)),
        ),
    ),
)
""".trimIndent()

internal val AppThemeLocalSample = """
val LocalAppSyntaxTheme = compositionLocalOf<SyntaxTheme> {
    SyntaxTheme.DefaultDark
}

@Composable
fun AppSyntaxTheme(
    theme: SyntaxTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppSyntaxTheme provides theme) {
        content()
    }
}
""".trimIndent()

internal val LanguageSetSample = """
val enabledLanguages = setOf(
    LanguageId.Kotlin,
    LanguageId.Json,
    LanguageId.Markdown,
    LanguageId.Shell,
)
val engine = SyntaxTokenizer(builtInLanguages = enabledLanguages)
val activeLanguageIds = engine.languageIds
val activeLanguageLabels = engine.languageLabels
""".trimIndent()


internal val CustomLanguageSample = """
val myql = LanguageId.fromString("myql")

val engine = SyntaxTokenizer(
    extensions = listOf(
        LanguageExtension(
            languageId = myql,
            aliases = setOf("mql"), // Optional; "myql" is recognized from the id.
            tokenizer = myqlTokenizer, // Create a custom LanguageTokenizer
        ),
    ),
)
""".trimIndent()
