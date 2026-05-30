package com.gallatinapps.syntaxmp.demo.panes

internal val InstallationCatalogSample = """
[versions]
syntaxmp = "0.1.0-SNAPSHOT"

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

internal val QuickStartSample = """
@Composable
fun CodeBlock(code: String, language: String) {
    val engine = remember { SyntaxTokenizerEngine() }
    BasicText(
        text = rememberSyntaxAnnotatedString(
            code = code,
            languageLabel = language,
            engine = engine,
            theme = SyntaxTheme.DefaultDark,
        ),
        style = TextStyle(fontFamily = FontFamily.Monospace),
    )
}
""".trimIndent()

internal val BasicTextSample = """
@Composable
fun CustomCodeBlock(code: String, languageLabel: String) {
    val engine = remember { SyntaxTokenizerEngine() }
    val theme = SyntaxTheme.DefaultDark
    val spans = remember(engine, code, languageLabel) {
        engine.tokenize(code = code, languageLabel = languageLabel)
    }
    val text = remember(code, spans, theme) {
        buildSyntaxAnnotatedString(code = code, spans = spans, theme = theme)
    }

    BasicText(
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
        ),
    )
}
""".trimIndent()

internal val EditableCodeSample = """
@Composable
fun CodeField(state: TextFieldState, languageLabel: String) {
    val engine = remember { SyntaxTokenizerEngine() }
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
val myRoleStyles = SyntaxRoleStyles(
    SyntaxRole.Keyword to SyntaxStyle(
        color = Color(0xFF3B73D9),
        fontWeight = FontWeight.SemiBold,
    ),
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

val theme = SyntaxTheme(roleStyles = myRoleStyles)
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

internal val RoleOverrideSample = """
val theme = SyntaxTheme.DefaultLight
    .withRoleStyle(
        SyntaxRole.Keyword.Control,
        SyntaxStyle(
            color = Color(0xFF0969DA),
            fontWeight = FontWeight.Bold,
        ),
    )
    .withRoleStyle(
        SyntaxRole.Constant.Builtin.append("null"),
        SyntaxStyle(
            color = Color(0xFFB91C1C),
            fontStyle = FontStyle.Italic,
        ),
    )
""".trimIndent()

internal val PerLanguageThemeSample = """
val myTheme = SyntaxTheme(
    roleStyles = SyntaxTheme.DefaultDark.roleStyles,
    languageOverrides = mapOf(
        SyntaxLanguageId.Kotlin to SyntaxRoleStyles(
            SyntaxRole.Keyword to SyntaxStyle(
                color = Color(0xFF7F52FF),
                fontWeight = FontWeight.Bold,
            ),
        ),
        SyntaxLanguageId.Python to SyntaxRoleStyles(
            SyntaxRole.Keyword to SyntaxStyle(
                color = Color(0xFF7DCFFF),
                fontWeight = FontWeight.SemiBold,
            ),
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
    SyntaxLanguageId.Kotlin,
    SyntaxLanguageId.Json,
    SyntaxLanguageId.Markdown,
    SyntaxLanguageId.Shell,
)
val engine = SyntaxTokenizerEngine(builtInLanguages = enabledLanguages)
""".trimIndent()

internal val RawTokenSample = """
val engine = SyntaxTokenizerEngine()
val code = "let x = 1"
val spans = engine.tokenize(code = code, languageLabel = "js")

for (span in spans) {
    val text = code.substring(span.start, span.endExclusive)
    println("${'$'}{span.languageId.value}/${'$'}{span.role.value}  ${'$'}text")
}
""".trimIndent()

internal val CustomLanguageSample = """
val myql = SyntaxLanguageId.fromString("myql")

val myqlTokenizer = SyntaxTokenizer { request ->
    val spans = mutableListOf<SyntaxTokenSpan>()
    val code = request.code
    var i = 0

    while (i < code.length) {
        when {
            code.startsWith("--", i) -> {
                val end = code.indexOf('\n', i).let {
                    if (it == -1) code.length else it
                }
                spans += SyntaxTokenSpan(i, end, SyntaxRole.Comment, request.languageId)
                i = end
            }
            code.startsWith("select", i, ignoreCase = true) -> {
                spans += SyntaxTokenSpan(i, i + 6, SyntaxRole.Keyword, request.languageId)
                i += 6
            }
            else -> i++
        }
    }

    SyntaxTokenizeResult(spans)
}

val engine = SyntaxTokenizerEngine(
    extensions = listOf(
        SyntaxLanguageExtension(
            languageId = myql,
            aliases = setOf("mql"),
            tokenizer = myqlTokenizer,
        ),
    ),
)
""".trimIndent()

internal val CachingSample = """
data class TokenizationKey(
    val languageLabel: String?,
    val codeHash: Int,
    val codeLength: Int,
)

class TokenizationCache(private val maxEntries: Int) {
    private val entries =
        LinkedHashMap<TokenizationKey, List<SyntaxTokenSpan>>()

    fun getOrPut(
        key: TokenizationKey,
        produce: () -> List<SyntaxTokenSpan>,
    ): List<SyntaxTokenSpan> {
        entries[key]?.let { return it }
        val value = produce()
        entries[key] = value
        while (entries.size > maxEntries) {
            entries.remove(entries.keys.first())
        }
        return value
    }
}

val spans = cache.getOrPut(
    TokenizationKey(languageLabel, code.hashCode(), code.length),
) {
    engine.tokenize(code = code, languageLabel = languageLabel)
}
""".trimIndent()
