package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId

internal data class DemoLanguage(
    val id: SyntaxLanguageId,
    val displayName: String,
    val routeSegment: String = id.value,
    val aliases: List<String> = emptyList(),
    val sample: String,
)
