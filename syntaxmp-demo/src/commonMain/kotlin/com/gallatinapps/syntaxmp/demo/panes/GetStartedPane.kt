package com.gallatinapps.syntaxmp.demo.panes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import com.gallatinapps.syntaxmp.compose.rememberSyntaxAnnotatedString
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.demo.components.DemoTextButton
import com.gallatinapps.syntaxmp.demo.components.DemoThemeToggleButton
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme
import com.gallatinapps.syntaxmp.demo.theme.DemoThemeMode
import com.gallatinapps.syntaxmp.demo.theme.diagnosticToggled
import com.gallatinapps.syntaxmp.demo.theme.demoCodeFontFamily
import com.gallatinapps.syntaxmp.demo.theme.syntaxTheme
import com.gallatinapps.syntaxmp.demo.theme.toggled

private const val SyntaxMpGitHubUrl = "https://github.com/GallatinApps/SyntaxMP"
private const val SyntaxMpLicenseUrl = "https://github.com/GallatinApps/SyntaxMP/blob/main/LICENSE"
private const val SyntaxMpContributingUrl = "https://github.com/GallatinApps/SyntaxMP/blob/main/CONTRIBUTING.md"
private const val DemoThirdPartyNoticesUrl =
    "https://github.com/GallatinApps/SyntaxMP/blob/main/syntaxmp-demo/THIRD_PARTY_NOTICES.md"
private const val JetBrainsMonoLicenseUrl =
    "https://github.com/GallatinApps/SyntaxMP/blob/main/syntaxmp-demo/third_party/licenses/JetBrainsMono-OFL-1.1.txt"

@Composable
internal fun GetStartedPane(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    onModeChanged: (DemoThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val engine = remember { SyntaxTokenizer() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paneBackground)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GetStartedHeader(
            mode = mode,
            colors = colors,
            onModeChanged = onModeChanged,
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            IntroSection(colors = colors)
            InstallationSection(
                mode = mode,
                colors = colors,
                engine = engine,
            )
            QuickStartSection(
                mode = mode,
                colors = colors,
                engine = engine,
            )
            ThemeSection(
                mode = mode,
                colors = colors,
                engine = engine,
            )
            LanguageSubsetSection(
                mode = mode,
                colors = colors,
                engine = engine,
            )
            CustomLanguageSection(
                mode = mode,
                colors = colors,
                engine = engine,
            )
            FaqSection(colors = colors)
            LicensingSection(colors = colors)
        }
    }
}

@Composable
private fun GetStartedHeader(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    onModeChanged: (DemoThemeMode) -> Unit,
    onBack: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            DemoTextButton(
                label = "Back",
                colors = colors,
                onClick = onBack,
            )
        }
        BasicText(
            text = "Get Started",
            modifier = Modifier.weight(1f),
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        DemoThemeToggleButton(
            mode = mode,
            colors = colors,
            onClick = { onModeChanged(mode.toggled) },
            onLongClick = { onModeChanged(mode.diagnosticToggled) },
        )
    }
}

@Composable
private fun IntroSection(colors: DemoColorScheme) {
    val uriHandler = LocalUriHandler.current
    ContentSection(
        title = "SyntaxMP",
        colors = colors,
    ) {
        BodyText(
            text = "Kotlin Multiplatform syntax highlighting for Compose. SyntaxMP ships hand-written tokenizers, role-based themes, and Compose text helpers for JVM, Android, iOS, and Wasm.",
            colors = colors,
        )
        BulletText("39 built-in languages.", colors)
        BulletText("Common Kotlin tokenizer engine with no platform parser bridge.", colors)
        BulletText("Compose primitives for BasicText, BasicTextField, and custom renderers.", colors)
        BulletText("Small, opinionated theme surface. Fully customizable for each role or language.", colors)

        DemoTextButton(
            label = "GitHub",
            colors = colors,
            onClick = { uriHandler.openUri(SyntaxMpGitHubUrl) },
        )
    }
}

@Composable
private fun InstallationSection(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    ContentSection(
        title = "Installation",
        colors = colors,
    ) {
        BodyText(
            text = "Add SyntaxMP to your version catalog, then depend on it from commonMain.",
            colors = colors,
        )
        CodeExample(
            code = InstallationCatalogSample,
            language = "toml",
            mode = mode,
            colors = colors,
            engine = engine,
        )
        CodeExample(
            code = InstallationBuildGradleSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
    }
}

@Composable
private fun QuickStartSection(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    ContentSection(
        title = "Quick Start",
        colors = colors,
    ) {
        BodyText(
            text = "For display-only code, use rememberSyntaxAnnotatedString with BasicText.",
            colors = colors,
        )
        CodeExample(
            code = BasicTextSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
        BodyText(
            text = "For editable code, build styled spans from the engine output and apply them in a BasicTextField outputTransformation.",
            colors = colors,
        )
        CodeExample(
            code = EditableCodeSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
    }
}

@Composable
private fun ThemeSection(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    ContentSection(
        title = "Theming",
        colors = colors,
    ) {
        BodyText(
            text = "DefaultLight and DefaultDark are starter themes, but most apps should define a theme that fits their own editor surface.",
            colors = colors,
        )
        CodeExample(
            code = ThemeSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
        BodyText(
            text = "Language overrides can restyle the same role for one language family.",
            colors = colors,
        )
        CodeExample(
            code = PerLanguageThemeSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
    }
}

@Composable
private fun LanguageSubsetSection(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    ContentSection(
        title = "Choosing A Language Subset",
        colors = colors,
    ) {
        BodyText(
            text = "The default engine enables every built-in language. Apps can pass a smaller Set<LanguageId> when they only need a focused subset.",
            colors = colors,
        )
        CodeExample(
            code = LanguageSetSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
    }
}

@Composable
private fun CustomLanguageSection(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    ContentSection(
        title = "Adding Your Own Language",
        colors = colors,
    ) {
        BodyText(
            text = "Implement LanguageTokenizer, wrap it in a LanguageExtension, register the extension on the engine, and your tokenizer runs alongside the built-ins:",
            colors = colors,
        )
        CodeExample(
            code = CustomLanguageSample,
            language = "kotlin",
            mode = mode,
            colors = colors,
            engine = engine,
        )
    }
}

@Composable
private fun FaqSection(colors: DemoColorScheme) {
    ContentSection(
        title = "FAQ",
        colors = colors,
    ) {
        FaqItem(
            question = "Does SyntaxMP auto-detect languages?",
            answer = "No. Pass a language id or alias. Unknown labels return plain text instead of throwing.",
            colors = colors,
        )
        FaqItem(
            question = "Can it highlight embedded script and style blocks?",
            answer = "Yes for the common built-ins: HTML script/style, Markdown fences, and component file regions such as JSX and TSX.",
            colors = colors,
        )
        FaqItem(
            question = "What if my language is not built in?",
            answer = "Add project-local support with LanguageExtension. For built-in requests, search issues first, then open one with the language, why it belongs, and examples. Built-ins stay focused on broadly useful languages.",
            colors = colors,
        )
        FaqItem(
            question = "Can it tokenize large files?",
            answer = "Yes, but large editors should apply styling to visible windows rather than the full file on every frame.",
            colors = colors,
        )
        FaqItem(
            question = "Is the engine safe to share?",
            answer = "The built-in engine is immutable and safe to share. Custom tokenizers should also stay stateless or guard their own state.",
            colors = colors,
        )
    }
}

@Composable
private fun LicensingSection(colors: DemoColorScheme) {
    val uriHandler = LocalUriHandler.current
    ContentSection(
        title = "Licensing",
        colors = colors,
    ) {
        BodyText(
            text = "SyntaxMP is copyright 2026 Gallatin Applications LLC and is licensed under the Apache License, Version 2.0.",
            colors = colors,
        )
        BodyText(
            text = "This demo bundles JetBrains Mono Regular and Italic. JetBrains Mono is copyright 2020 The JetBrains Mono Project Authors and is licensed under the SIL Open Font License, Version 1.1.",
            colors = colors,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DemoTextButton(
                label = "Project License",
                colors = colors,
                onClick = { uriHandler.openUri(SyntaxMpLicenseUrl) },
            )
            DemoTextButton(
                label = "Contributing",
                colors = colors,
                onClick = { uriHandler.openUri(SyntaxMpContributingUrl) },
            )
            DemoTextButton(
                label = "Demo Notices",
                colors = colors,
                onClick = { uriHandler.openUri(DemoThirdPartyNoticesUrl) },
            )
            DemoTextButton(
                label = "Font License",
                colors = colors,
                onClick = { uriHandler.openUri(JetBrainsMonoLicenseUrl) },
            )
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    colors: DemoColorScheme,
) {
    SelectionContainer {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = question,
                style = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                ),
            )
            BasicText(
                text = answer,
                style = TextStyle(
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
            )
        }
    }
}

@Composable
private fun ContentSection(
    title: String,
    colors: DemoColorScheme,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SelectionContainer {
            BasicText(
                text = title,
                style = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        content()
    }
}

@Composable
private fun BodyText(
    text: String,
    colors: DemoColorScheme,
) {
    SelectionContainer {
        BasicText(
            text = text,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            ),
        )
    }
}

@Composable
private fun BulletText(
    text: String,
    colors: DemoColorScheme,
) {
    SelectionContainer {
        BasicText(
            text = "- $text",
            style = TextStyle(
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
            ),
        )
    }
}

@Composable
private fun CodeExample(
    code: String,
    language: String,
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
) {
    val codeFontFamily = demoCodeFontFamily()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.codeBackground)
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SelectionContainer {
            BasicText(
                text = rememberSyntaxAnnotatedString(
                    code = code,
                    languageLabel = language,
                    engine = engine,
                    theme = mode.syntaxTheme(colors),
                ),
                style = TextStyle(
                    color = colors.textPrimary,
                    fontFamily = codeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                ),
            )
        }
    }
}
