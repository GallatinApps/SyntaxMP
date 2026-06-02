package com.gallatinapps.syntaxmp.benchmarks.fixtures

import com.gallatinapps.syntaxmp.language.LanguageId
import java.util.Locale

internal object LanguageBenchmarkFixtureSelfCheck {
    fun run() {
        val fixture = LanguageBenchmarkFixture(
            fixtureId = "self-check",
            languageLabel = "kotlin",
            displayName = "Self Check",
            family = LanguageBenchmarkFamily.GeneralSource,
            targetBodyLines = 2,
            header = "package bench",
            body = """
                fun sample{{index}}(): String =
                    "value-{{index}}"
            """,
            footer = "fun done(): Unit = Unit",
        )

        check(fixture.bodyLineCount == 2) {
            "Expected self-check body to have 2 lines, got ${fixture.bodyLineCount}"
        }
        check(fixture.multipliers() == LanguageBenchmarkMultiplier.entries) {
            "Expected representative fixtures to expose every configured multiplier."
        }

        val oneX = fixture.expand(LanguageBenchmarkMultiplier.OneX)
        check(oneX.code == ExpectedOneX) {
            "Unexpected 1x fixture expansion:\n${oneX.code}"
        }

        val tenX = fixture.expand(LanguageBenchmarkMultiplier.TenX)
        check(tenX.code.contains("fun sample10(): String =")) {
            "10x expansion should apply the index placeholder through the final repetition."
        }
        check(tenX.lines == 33) {
            "Expected 10x fixture expansion to have 33 lines, got ${tenX.lines}"
        }

        val warning = bodyLineTargetWarning(
            fixtureId = "warning-check",
            bodyLineCount = 1,
            targetBodyLines = 10,
        )
        check(warning != null) {
            "Expected target-line warning for a body far below its target."
        }

        check(LanguageBenchmarkCatalog.fixtures.size == ExpectedFixtureCount) {
            "Expected $ExpectedFixtureCount representative fixtures, got ${LanguageBenchmarkCatalog.fixtures.size}."
        }

        val fixtureSortKeys = LanguageBenchmarkCatalog.fixtures.map { fixture ->
            fixture.displayName.lowercase(Locale.ROOT) to fixture.fixtureId
        }
        check(fixtureSortKeys == fixtureSortKeys.sortedWith(compareBy({ it.first }, { it.second }))) {
            "Representative fixtures should be sorted alphabetically by display name."
        }

        val duplicateFixtureIds = LanguageBenchmarkCatalog.fixtures
            .groupBy { it.fixtureId }
            .filterValues { it.size > 1 }
            .keys
        check(duplicateFixtureIds.isEmpty()) {
            "Representative fixtures must have unique fixture ids: $duplicateFixtureIds"
        }

        val coveredLanguageLabels = LanguageBenchmarkCatalog.fixtures.map { it.languageLabel }.toSet()
        val missingLanguageLabels = LanguageId.BuiltIns.map { it.value }.toSet() - coveredLanguageLabels
        check(missingLanguageLabels.isEmpty()) {
            "Representative fixtures missing built-in language labels: $missingLanguageLabels"
        }

        val targetLineWarnings = LanguageBenchmarkCatalog.fixtures.mapNotNull { it.targetLineWarning }
        check(targetLineWarnings.isEmpty()) {
            "Representative fixtures outside target body-line ranges:\n${targetLineWarnings.joinToString("\n")}"
        }

        val expectedMatrixIds = LanguageBenchmarkCatalog.fixtures.flatMap { fixture ->
            LanguageBenchmarkMultiplier.entries.map { multiplier ->
                "languages/representative/${fixture.fixtureId}/${multiplier.idSegment}"
            }
        }
        check(LanguageBenchmarkCatalog.expandedFixtures().map { it.caseId } == expectedMatrixIds) {
            "Representative matrix should contain every fixture at 1x and 10x in alphabetical run order."
        }
    }
}

private val ExpectedOneX = """
    package bench

    fun sample1(): String =
        "value-1"

    fun done(): Unit = Unit
""".trimIndent()

private val ExpectedFixtureCount = LanguageId.BuiltIns.size + 1
