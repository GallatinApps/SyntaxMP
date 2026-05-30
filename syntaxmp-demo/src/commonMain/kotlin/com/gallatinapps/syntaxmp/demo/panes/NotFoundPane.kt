package com.gallatinapps.syntaxmp.demo.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallatinapps.syntaxmp.demo.components.DemoTextButton
import com.gallatinapps.syntaxmp.demo.components.DemoThemeToggleButton
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme
import com.gallatinapps.syntaxmp.demo.theme.DemoThemeMode
import com.gallatinapps.syntaxmp.demo.theme.diagnosticToggled
import com.gallatinapps.syntaxmp.demo.theme.toggled

@Composable
internal fun NotFoundPane(
    originalPath: String,
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    onModeChanged: (DemoThemeMode) -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paneBackground)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        NotFoundHeader(
            mode = mode,
            colors = colors,
            onModeChanged = onModeChanged,
            onBack = onBack,
        )
        SelectionContainer {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "No SyntaxMP demo page exists for this path.",
                    style = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    ),
                )
                BasicText(
                    text = originalPath.ifBlank { "/" },
                    style = TextStyle(
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    ),
                )
            }
        }
        DemoTextButton(
            label = "Get Started",
            colors = colors,
            onClick = onGetStarted,
        )
    }
}

@Composable
private fun NotFoundHeader(
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
            text = "Page Not Found",
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
