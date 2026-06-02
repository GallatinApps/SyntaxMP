package com.gallatinapps.syntaxmp.demo.model

import com.gallatinapps.syntaxmp.language.LanguageId

internal data class DemoLanguage(
    val id: LanguageId,
    val displayName: String,
    val routeSegment: String = id.value,
    val aliases: List<String> = emptyList(),
    val sample: String,
)
