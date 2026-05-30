package com.gallatinapps.syntaxmp.demo.model.samples

internal val KotlinSample = """
    @file:Suppress("unused", "MagicNumber")

    package demo

    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.text.BasicText
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.Immutable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp

    /**
     * Preview model for a note row.
     *
     * KDoc, annotations, generics, defaults, string interpolation, labels,
     * and Compose call sites all show up in the SyntaxMP Kotlin tokenizer.
     */
    @Immutable
    data class NoteSummary<TMeta>(
        val title: String,
        val words: Int,
        val pinned: Boolean = false,
        val status: NoteStatus = NoteStatus.Draft,
        val tags: List<String> = emptyList(),
        val metadata: TMeta? = null,
    )

    enum class NoteStatus {
        Draft,
        Review,
        Pinned,
        Archived,
    }

    private const val WordsPerMinute = 220
    private val MentionRegex = Regex("@[A-Za-z0-9_]+")

    class NoteListScope(private val owner: String) {
        fun NoteSummary<*>.ownerLabel(): String = "${'$'}{this@NoteListScope.owner}: ${'$'}title"
    }

    fun NoteSummary<*>.`reading label`(): String {
        val minutes = maxOf(1, words / WordsPerMinute)
        val tagLabel = tags.joinToString(prefix = "[", postfix = "]")
        val preview = ${"\"\"\""}
            ${'$'}title
            ${'$'}words words
        ${"\"\"\""}.trimIndent()

        return if (preview.isNotBlank()) {
            "${'$'}title • ${'$'}minutes min ${'$'}tagLabel"
        } else {
            "Untitled"
        }
    }

    fun firstUsefulTag(note: NoteSummary<Map<String, String>>): String? {
        loop@ for (tag in note.tags) {
            if (tag.startsWith("#draft")) continue@loop
            if (tag.length > 24) break@loop
            return tag
        }
        return null
    }

    fun NoteSummary<*>.mentionLabels(): List<String> =
        MentionRegex.findAll(title).map mapper@{
            val mention = it.value.removePrefix("@")
            if (mention.isBlank()) return@mapper "unknown"
            "@${'$'}mention"
        }.toList()

    @Deprecated("Use NoteSummaryCard after token fixture review")
    @Composable
    fun LegacyNoteCard(note: NoteSummary<Map<String, String>>) {
        BasicText(text = note.`reading label`())
    }

    @Composable
    fun NoteSummaryCard(
        note: NoteSummary<Map<String, String>>,
        onPinChanged: (Boolean) -> Unit,
    ) {
        var expanded by remember(key1 = note.title) { mutableStateOf(false) }
        val mentions = remember(key1 = note.title) {
            note.mentionLabels()
        }
        val selectedTag = firstUsefulTag(note) ?: "untagged"
        val highlight = note.pinned == true
        val status = if (highlight) NoteStatus.Pinned else note.status

        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(text = note.title)
                BasicText(text = note.`reading label`())
                if (expanded) {
                    BasicText(
                        text = "Pinned=${'$'}highlight, status=${'$'}status, tag=${'$'}selectedTag, mentions=${'$'}mentions",
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            BasicText(
                text = if (note.pinned) "Pinned" else "Pin",
                modifier = Modifier.clickable { onPinChanged(!note.pinned) },
                style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.SemiBold),
            )
            BasicText(
                text = if (expanded) "Less" else "More",
                modifier = Modifier.clickable {
                    expanded = !expanded
                },
            )
        }
    }
""".trimIndent()
