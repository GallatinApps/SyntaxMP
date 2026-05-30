package com.gallatinapps.syntaxmp.benchmarks.fixtures

import com.gallatinapps.syntaxmp.benchmarks.lineCount

internal data class LanguageBenchmarkFixture(
    val fixtureId: String,
    val languageLabel: String,
    val displayName: String,
    val family: LanguageBenchmarkFamily,
    val targetBodyLines: Int,
    val header: String = "",
    val body: String,
    val footer: String = "",
) {
    val normalizedHeader: String = header.normalizedFixtureSection()
    val normalizedBody: String = body.normalizedFixtureSection()
    val normalizedFooter: String = footer.normalizedFixtureSection()
    val bodyLineCount: Int = lineCount(normalizedBody)
    val targetLineWarning: String? = bodyLineTargetWarning(
        fixtureId = fixtureId,
        bodyLineCount = bodyLineCount,
        targetBodyLines = targetBodyLines,
    )

    init {
        require(fixtureId.matches(FixtureIdPattern)) { "fixtureId must be a stable lowercase slug: $fixtureId" }
        require(languageLabel.isNotBlank()) { "languageLabel must not be blank for $fixtureId" }
        require(displayName.isNotBlank()) { "displayName must not be blank for $fixtureId" }
        require(targetBodyLines > 0) { "targetBodyLines must be positive for $fixtureId" }
        require(normalizedBody.isNotBlank()) { "body must not be blank for $fixtureId" }
    }

    fun multipliers(): List<LanguageBenchmarkMultiplier> =
        LanguageBenchmarkMultiplier.entries

    fun expand(multiplier: LanguageBenchmarkMultiplier): ExpandedLanguageBenchmarkFixture {
        val code = buildString {
            appendSection(normalizedHeader)
            repeat(multiplier.repetitions) { repeatIndex ->
                appendSection(
                    normalizedBody.replace(
                        oldValue = IndexPlaceholder,
                        newValue = (repeatIndex + 1).toString(),
                    ),
                )
            }
            appendSection(normalizedFooter)
        }
        return ExpandedLanguageBenchmarkFixture(
            caseId = "languages/representative/$fixtureId/${multiplier.idSegment}",
            fixtureId = fixtureId,
            languageLabel = languageLabel,
            displayName = "$displayName ${multiplier.displayName}",
            family = family,
            multiplier = multiplier,
            code = code,
        )
    }

    private fun StringBuilder.appendSection(section: String) {
        if (section.isBlank()) return
        if (isNotEmpty()) {
            append("\n\n")
        }
        append(section)
    }
}

internal data class ExpandedLanguageBenchmarkFixture(
    val caseId: String,
    val fixtureId: String,
    val languageLabel: String,
    val displayName: String,
    val family: LanguageBenchmarkFamily,
    val multiplier: LanguageBenchmarkMultiplier,
    val code: String,
) {
    val chars: Int = code.length
    val lines: Int = lineCount(code)
    val sizeName: String = "Representative ${multiplier.displayName}"
}

internal enum class LanguageBenchmarkMultiplier(
    val displayName: String,
    val idSegment: String,
    val repetitions: Int,
) {
    OneX(displayName = "1x", idSegment = "1x", repetitions = 1),
    TenX(displayName = "10x", idSegment = "10x", repetitions = 10),
}

internal enum class LanguageBenchmarkFamily(
    val displayName: String,
) {
    GeneralSource("General source"),
    WebScriptSource("Web script source"),
    ScriptLikeSource("Script-like source"),
    MarkupComponent("Markup/component"),
    Stylesheet("Stylesheet"),
    MarkdownPlain("Markdown plain"),
    MarkdownEmbedded("Markdown embedded"),
    QuerySchema("Query/schema"),
    ConfigData("Config data"),
    CompactLineFormat("Compact line format"),
}

internal fun bodyLineTargetWarning(
    fixtureId: String,
    bodyLineCount: Int,
    targetBodyLines: Int,
): String? {
    val lowerBound = targetBodyLines * 0.7
    val upperBound = targetBodyLines * 1.3
    return if (bodyLineCount < lowerBound || bodyLineCount > upperBound) {
        "Fixture '$fixtureId' body has $bodyLineCount lines; target is $targetBodyLines (+/-30%)."
    } else {
        null
    }
}

private fun String.normalizedFixtureSection(): String =
    trimIndent().trim('\n')

private const val IndexPlaceholder = "{{index}}"

private val FixtureIdPattern = Regex("[a-z0-9]+(?:-[a-z0-9]+)*")
