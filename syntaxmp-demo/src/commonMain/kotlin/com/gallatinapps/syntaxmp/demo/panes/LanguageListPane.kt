package com.gallatinapps.syntaxmp.demo.panes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallatinapps.syntaxmp.demo.model.DemoLanguage
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme

@Composable
internal fun LanguageListPane(
    languages: List<DemoLanguage>,
    getStartedSelected: Boolean,
    selectedRouteSegment: String?,
    colors: DemoColorScheme,
    onGetStartedSelected: () -> Unit,
    onLanguageSelected: (DemoLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paneBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = "SyntaxMP",
                style = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            BasicText(
                text = "KMP/CMP code syntax highlighter",
                style = TextStyle(
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                ),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item(key = "get-started") {
                NavigationRow(
                    title = "Get Started",
                    selected = getStartedSelected,
                    colors = colors,
                    onClick = onGetStartedSelected,
                )
            }
            item(key = "languages-label") {
                SectionLabel(
                    text = "Languages",
                    colors = colors,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                )
            }
            items(
                items = languages,
                key = { language -> language.id.value },
            ) { language ->
                NavigationRow(
                    title = language.displayName,
                    selected = language.routeSegment == selectedRouteSegment,
                    colors = colors,
                    onClick = { onLanguageSelected(language) },
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    colors: DemoColorScheme,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = TextStyle(
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

@Composable
private fun NavigationRow(
    title: String,
    selected: Boolean,
    colors: DemoColorScheme,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val background = if (selected) colors.selectedBackground else colors.paneBackground
    val borderColor = when {
        selected -> colors.accent
        hovered || focused -> colors.accentMuted
        else -> colors.border
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = shape,
            )
            .hoverable(interactionSource)
            .focusable(interactionSource = interactionSource)
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}
