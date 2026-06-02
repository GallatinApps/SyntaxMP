package com.gallatinapps.syntaxmp.demo.panes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import com.gallatinapps.syntaxmp.compose.rememberSyntaxAnnotatedString
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import com.gallatinapps.syntaxmp.demo.components.DemoTextButton
import com.gallatinapps.syntaxmp.demo.components.DemoThemeToggleButton
import com.gallatinapps.syntaxmp.demo.model.DemoLanguage
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme
import com.gallatinapps.syntaxmp.demo.theme.DemoThemeMode
import com.gallatinapps.syntaxmp.demo.theme.diagnosticToggled
import com.gallatinapps.syntaxmp.demo.theme.demoCodeFontFamily
import com.gallatinapps.syntaxmp.demo.theme.syntaxTheme
import com.gallatinapps.syntaxmp.demo.theme.toggled

@Composable
internal fun SyntaxPreviewPane(
    language: DemoLanguage,
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PreviewHeader(
            language = language,
            mode = mode,
            colors = colors,
            onModeChanged = onModeChanged,
            onBack = onBack,
        )
        CodePreview(
            language = language,
            mode = mode,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            engine = engine,
        )
    }
}

@Composable
private fun PreviewHeader(
    language: DemoLanguage,
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    onModeChanged: (DemoThemeMode) -> Unit,
    onBack: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            LanguageTitle(
                language = language,
                modifier = Modifier.weight(1f),
                colors = colors,
            )
            DemoThemeToggleButton(
                mode = mode,
                colors = colors,
                onClick = { onModeChanged(mode.toggled) },
                onLongClick = { onModeChanged(mode.diagnosticToggled) },
            )
        }
    }
}

@Composable
private fun LanguageTitle(
    language: DemoLanguage,
    colors: DemoColorScheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            text = language.displayName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun CodePreview(
    language: DemoLanguage,
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    engine: SyntaxTokenizer,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val codeFontFamily = demoCodeFontFamily()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.codeBackground)
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(8.dp))
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .padding(18.dp),
    ) {
        SelectionContainer {
            BasicText(
                text = rememberSyntaxAnnotatedString(
                    code = language.sample,
                    languageLabel = language.id.value,
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
