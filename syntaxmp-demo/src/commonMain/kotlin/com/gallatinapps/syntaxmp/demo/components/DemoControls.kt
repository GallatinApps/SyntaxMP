package com.gallatinapps.syntaxmp.demo.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme
import com.gallatinapps.syntaxmp.demo.theme.DemoThemeMode

@Composable
internal fun DemoTextButton(
    label: String,
    colors: DemoColorScheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val active = hovered || focused
    val background = if (active) colors.hoverBackground else colors.paneBackground
    BasicText(
        text = label,
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(BorderStroke(1.dp, colors.accent), shape)
            .hoverable(interactionSource)
            .focusable(interactionSource = interactionSource)
            .buttonClick(
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        style = TextStyle(
            color = colors.accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

@Composable
internal fun DemoThemeToggleButton(
    mode: DemoThemeMode,
    colors: DemoColorScheme,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DemoTextButton(
        label = mode.label,
        colors = colors,
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.buttonClick(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
): Modifier =
    if (onLongClick == null) {
        clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onLongClickLabel = "Diagnostic",
            onLongClick = onLongClick,
            onClick = onClick,
        )
    }

private val DemoThemeMode.label: String
    get() = when (this) {
        DemoThemeMode.Light -> "Light"
        DemoThemeMode.Dark -> "Dark"
        DemoThemeMode.Diagnostic -> "Diagnostic"
    }
