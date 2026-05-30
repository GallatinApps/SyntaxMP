package com.gallatinapps.syntaxmp.benchmarks.fixtures

import com.gallatinapps.syntaxmp.benchmarks.fixtures.compact.CompactLineFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.config.ConfigDataFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.markdown.MarkdownFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.markup.MarkupComponentFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.query.QueryInfrastructureFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.source.GeneralSourceFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.source.ScriptLikeFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.source.WebScriptFixtures
import com.gallatinapps.syntaxmp.benchmarks.fixtures.styles.StylesheetFixtures
import java.util.Locale

internal object LanguageBenchmarkCatalog {
    val fixtures: List<LanguageBenchmarkFixture> =
        (
            GeneralSourceFixtures +
                WebScriptFixtures +
                ScriptLikeFixtures +
                MarkupComponentFixtures +
                MarkdownFixtures +
                ConfigDataFixtures +
                CompactLineFixtures +
                StylesheetFixtures +
                QueryInfrastructureFixtures
            ).sortedWith(FixtureDisplayNameComparator)

    fun expandedFixtures(): List<ExpandedLanguageBenchmarkFixture> =
        fixtures.flatMap { fixture ->
            fixture.multipliers().map(fixture::expand)
        }
}

private val FixtureDisplayNameComparator =
    compareBy<LanguageBenchmarkFixture> { it.displayName.lowercase(Locale.ROOT) }
        .thenBy { it.fixtureId }
