package com.gallatinapps.syntaxmp.benchmarks

/**
 * Focused Kotlin/Compose sample for the diagnostic Kotlin tokenization rows and the hot-path
 * profiler. Distinct from the representative `fixtures/` matrix (realistic per-language baselines)
 * and from [BenchmarkSamplesFactory] (synthetic stress inputs): this isolates Compose-shaped Kotlin.
 */
internal object KotlinComposeSamples {
    fun notesDashboard(multiplier: Int): SourceSample {
        require(multiplier >= 1) { "multiplier must be at least 1" }
        return SourceSample(
            caseId = "diagnostics/kotlin-focused/compose-dashboard/${multiplier}x",
            name = if (multiplier == 1) {
                "Kotlin Compose dashboard sample"
            } else {
                "Kotlin Compose dashboard sample x$multiplier"
            },
            languageLabel = "kotlin",
            code = List(multiplier) { notesDashboardSample }.joinToString(separator = "\n\n"),
            sizeName = if (multiplier == 1) {
                "Compose dashboard sample"
            } else {
                "Compose dashboard sample x$multiplier"
            },
            workloadKind = BenchmarkWorkloadKind.Representative,
        )
    }

    private val notesDashboardSample = """
        package samples.notekit
        @file:Suppress("unused")

        import androidx.compose.foundation.ExperimentalFoundationApi
        import androidx.compose.foundation.clickable
        import androidx.compose.foundation.layout.Arrangement
        import androidx.compose.foundation.layout.Column
        import androidx.compose.foundation.layout.Row
        import androidx.compose.foundation.layout.Spacer
        import androidx.compose.foundation.layout.fillMaxWidth
        import androidx.compose.foundation.layout.padding
        import androidx.compose.foundation.layout.width
        import androidx.compose.foundation.lazy.LazyColumn
        import androidx.compose.foundation.lazy.items
        import androidx.compose.foundation.lazy.stickyHeader
        import androidx.compose.material3.Badge
        import androidx.compose.material3.Checkbox
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.OutlinedTextField
        import androidx.compose.material3.Surface
        import androidx.compose.material3.Text
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.LaunchedEffect
        import androidx.compose.runtime.derivedStateOf
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateListOf
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.saveable.rememberSaveable
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.semantics.contentDescription
        import androidx.compose.ui.semantics.semantics
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.unit.dp
        import org.jetbrains.compose.ui.tooling.preview.Preview

        @JvmInline
        value class NoteId(val value: String)

        enum class SyncState {
            Clean,
            Dirty,
            Conflict,
        }

        sealed interface NoteEvent {
            data class Open(val id: NoteId) : NoteEvent
            data class TogglePin(val id: NoteId) : NoteEvent
            data object CreateToday : NoteEvent
        }

        data class Note(
            val id: NoteId,
            val title: String,
            val tags: Set<String> = emptySet(),
            val syncState: SyncState = SyncState.Clean,
            val pinned: Boolean = false,
        )

        val Note.isActionable: Boolean
            get() = pinned || syncState != SyncState.Clean

        fun List<Note>.visibleFor(query: String): List<Note> =
            asSequence()
                .filter { note ->
                    query.isBlank() ||
                        note.title.contains(query, ignoreCase = true) ||
                        note.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
                .sortedWith(compareByDescending<Note> { it.pinned }.thenBy { it.title.lowercase() })
                .toList()

        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun NotesDashboard(
            notes: List<Note>,
            onEvent: (NoteEvent) -> Unit,
            modifier: Modifier = Modifier,
        ) {
            var query by rememberSaveable { mutableStateOf("") }
            val draftNotes = remember(notes) { mutableStateListOf<Note>().also { it.addAll(notes) } }
            val visibleNotes by remember(draftNotes, query) {
                derivedStateOf { draftNotes.visibleFor(query) }
            }

            LaunchedEffect(visibleNotes.size) {
                println("Showing ${'$'}{visibleNotes.size} notes on ${'$'}{platformName()}")
            }

            Surface(modifier = modifier.semantics { contentDescription = "Notes dashboard" }) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "NoteKit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search notes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stickyHeader {
                            Text("Pinned and recent", style = MaterialTheme.typography.labelLarge)
                        }
                        items(visibleNotes, key = { it.id.value }) { note ->
                            NoteRow(
                                note = note,
                                onOpen = { onEvent(NoteEvent.Open(note.id)) },
                                onTogglePin = { onEvent(NoteEvent.TogglePin(note.id)) },
                            )
                        }
                    }
                }
            }
        }

        @Composable
        private fun NoteRow(
            note: Note,
            onOpen: () -> Unit,
            onTogglePin: () -> Unit,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                    Text(note.tags.joinToString(prefix = "#", separator = " #"))
                }
                Row {
                    Checkbox(checked = note.pinned, onCheckedChange = { onTogglePin() })
                    Spacer(Modifier.width(8.dp))
                    if (note.isActionable) {
                        Badge { Text(note.syncState.name.lowercase()) }
                    }
                }
            }
        }

        @Preview
        @Composable
        fun NotesDashboardPreview() {
            NotesDashboard(
                notes = sampleNotes(),
                onEvent = { event ->
                    when (event) {
                        is NoteEvent.Open -> println("Open ${'$'}{event.id.value}")
                        is NoteEvent.TogglePin -> println("Pin ${'$'}{event.id.value}")
                        NoteEvent.CreateToday -> println("Create today's note")
                    }
                },
            )
        }

        fun sampleNotes(): List<Note> = listOf(
            Note(
                id = NoteId("daily-2026-05-20"),
                title = "Daily Note",
                tags = setOf("compose", "kmp"),
                pinned = true,
            ),
            Note(
                id = NoteId("settings-json"),
                title = "settings.json",
                tags = setOf("config", "syntaxmp"),
                syncState = SyncState.Dirty,
            ),
        )

        fun buildStatusReport(notes: List<Note>): String {
            val firstTitle = notes.firstOrNull()?.title ?: "Untitled"
            val rawPath = ""${'"'}C:\NoteKit\Notes\${'$'}firstTitle""${'"'}
            return ""${'"'}
                Workspace: ${'$'}{notes.size} notes
                First: ${'$'}firstTitle
                Raw path text keeps backslashes: ${'$'}rawPath
                Dirty count: ${'$'}{notes.count { it.syncState == SyncState.Dirty }}
            ""${'"'}.trimIndent()
        }

        expect fun platformName(): String

        fun main() {
            val notes = sampleNotes()
            println(buildStatusReport(notes))
            println(notes.visibleFor("compose"))
        }

        @Deprecated("fixture")
        class Outer {
            fun run(values: List<Int>) {
                loop@ for (value in values) {
                    if (value == 0) break@loop
                }
                values.map mapper@{
                    return@mapper it.toString()
                }
                this@Outer.toString()
            }
        }
    """.trimIndent()
}
