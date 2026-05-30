package com.gallatinapps.syntaxmp.demo.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.gallatinapps.syntaxmp.demo.generated.resources.JetBrainsMono
import com.gallatinapps.syntaxmp.demo.generated.resources.JetBrainsMono_Italic
import com.gallatinapps.syntaxmp.demo.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
internal fun demoCodeFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.JetBrainsMono, weight = FontWeight.Normal),
        Font(Res.font.JetBrainsMono_Italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    )
