package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val KotlinRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "kotlin",
    languageLabel = "kotlin",
    displayName = "Kotlin",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    header = """
        @file:Suppress("unused", "MagicNumber")

        package benchmark.kotlin

        import androidx.compose.foundation.clickable
        import androidx.compose.foundation.layout.Arrangement
        import androidx.compose.foundation.layout.Column
        import androidx.compose.foundation.layout.Row
        import androidx.compose.foundation.layout.Spacer
        import androidx.compose.foundation.layout.fillMaxWidth
        import androidx.compose.foundation.layout.padding
        import androidx.compose.foundation.layout.width
        import androidx.compose.foundation.text.BasicText
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.Immutable
        import androidx.compose.runtime.Stable
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.text.TextStyle
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.unit.dp
    """,
    body = """
        /**
         * Representative dashboard slice {{index}}.
         *
         * Covers annotations, declarations, generics, labels, lambdas, strings,
         * raw strings, qualified names, named arguments, and Compose call sites.
         */
        @Immutable
        data class DashboardCard{{index}}<TMeta : Any>(
            val id: String,
            val title: String,
            val words: Int,
            val pinned: Boolean = false,
            val state: DashboardState{{index}} = DashboardState{{index}}.Draft,
            val tags: List<String> = emptyList(),
            val metadata: TMeta? = null,
        )

        enum class DashboardState{{index}} {
            Draft,
            Review,
            Pinned,
            Archived,
        }

        @Stable
        class DashboardScope{{index}}(
            private val owner: String,
            private val density: Int = 2,
        ) {
            val DashboardCard{{index}}<*>.ownerLabel: String
                get() = "${'$'}owner:${'$'}title:${'$'}density"

            fun DashboardCard{{index}}<*>.routeName(section: String = "inbox"): String =
                "workspace.${'$'}section.${'$'}id"
        }

        private const val WordsPerMinute{{index}} = 220
        private val MentionRegex{{index}} = Regex("@[A-Za-z0-9_]+")

        fun DashboardCard{{index}}<*>.readingLabel{{index}}(): String {
            val minutes = maxOf(1, words / WordsPerMinute{{index}})
            val tagLabel = tags.joinToString(prefix = "[", postfix = "]")
            val preview = ${"\"\"\""}
                ${'$'}title
                ${'$'}words words
                ${'$'}{metadata?.toString() ?: "none"}
            ${"\"\"\""}.trimIndent()

            return if (preview.isNotBlank()) {
                "${'$'}title - ${'$'}minutes min - ${'$'}tagLabel"
            } else {
                "Untitled"
            }
        }

        fun firstUsefulTag{{index}}(card: DashboardCard{{index}}<Map<String, String>>): String? {
            tagLoop@ for (tag in card.tags) {
                if (tag.startsWith("#draft")) continue@tagLoop
                if (tag.length > 24) break@tagLoop
                return tag
            }
            return null
        }

        fun DashboardCard{{index}}<*>.mentionLabels{{index}}(): List<String> =
            MentionRegex{{index}}.findAll(title).map mapper@{
                val mention = it.value.removePrefix("@")
                if (mention.isBlank()) return@mapper "unknown"
                "@${'$'}mention"
            }.toList()

        @Deprecated("Use DashboardCard{{index}}View after fixture review")
        @Composable
        fun LegacyDashboardCard{{index}}(card: DashboardCard{{index}}<Map<String, String>>) {
            BasicText(text = card.readingLabel{{index}}())
        }

        @Composable
        fun DashboardCard{{index}}View(
            card: DashboardCard{{index}}<Map<String, String>>,
            onPinChanged: (DashboardCard{{index}}<Map<String, String>>, Boolean) -> Unit,
            modifier: Modifier = Modifier,
        ) {
            var expanded by remember(key1 = card.id) { mutableStateOf(false) }
            val mentions = remember(key1 = card.title) { card.mentionLabels{{index}}() }
            val selectedTag = firstUsefulTag{{index}}(card) ?: "untagged"
            val highlighted = card.pinned == true
            val state = if (highlighted) DashboardState{{index}}.Pinned else card.state

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(text = card.title)
                    BasicText(text = card.readingLabel{{index}}())
                    if (expanded) {
                        BasicText(
                            text = "Pinned=${'$'}highlighted, state=${'$'}state, tag=${'$'}selectedTag, mentions=${'$'}mentions",
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                BasicText(
                    text = if (card.pinned) "Pinned" else "Pin",
                    modifier = Modifier.clickable { onPinChanged(card, !card.pinned) },
                    style = TextStyle(fontWeight = FontWeight.SemiBold),
                )
                BasicText(
                    text = if (expanded) "Less" else "More",
                    modifier = Modifier.clickable { expanded = !expanded },
                )
            }
        }
    """,
)

internal val GeneralSourceFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        JavaRepresentativeFixture,
        SwiftRepresentativeFixture,
        CRepresentativeFixture,
        CppRepresentativeFixture,
        CSharpRepresentativeFixture,
        GoRepresentativeFixture,
        RustRepresentativeFixture,
        DartRepresentativeFixture,
        KotlinRepresentativeFixture,
    )
