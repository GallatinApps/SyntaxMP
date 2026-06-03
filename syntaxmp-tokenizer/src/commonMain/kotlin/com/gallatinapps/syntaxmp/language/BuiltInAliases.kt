package com.gallatinapps.syntaxmp.language

// Keep this map grouped by target LanguageId in LanguageId.value alphabetical order.
// Include only non-canonical aliases here; canonical id labels come from BuiltInIdsByLabel.
// Languages without aliases are omitted.
private val BuiltInAliasesByLanguage: Map<LanguageId, Set<String>> = mapOf(
    LanguageId.C to setOf("h"),
    LanguageId.Cpp to setOf("c++", "cc", "cxx", "hpp"),
    LanguageId.CSharp to setOf("c#", "cs"),
    LanguageId.Diff to setOf("patch"),
    LanguageId.Dockerfile to setOf("containerfile", "docker"),
    LanguageId.Dotenv to setOf("env"),
    LanguageId.Go to setOf("golang"),
    LanguageId.GraphQl to setOf("gql"),
    LanguageId.Html to setOf("htm"),
    LanguageId.JavaScript to setOf("js"),
    LanguageId.Kotlin to setOf("gradle.kts", "kt", "kts"),
    LanguageId.Makefile to setOf("make", "mk"),
    LanguageId.Markdown to setOf("md"),
    LanguageId.Postgresql to setOf("pgsql", "postgres"),
    LanguageId.PowerShell to setOf("ps", "ps1"),
    LanguageId.Protobuf to setOf("proto"),
    LanguageId.Python to setOf("py"),
    LanguageId.Ruby to setOf("rb"),
    LanguageId.Rust to setOf("rs"),
    LanguageId.Shell to setOf("sh"),
    LanguageId.Sqlite to setOf("sqlite3"),
    LanguageId.TypeScript to setOf("ts"),
    LanguageId.Yaml to setOf("yml"),
)

private val BuiltInAliases: Map<String, LanguageId> =
    BuiltInAliasesByLanguage
        .flatMap { (languageId, aliases) ->
            aliases.map { alias -> alias to languageId }
        }
        .toMap()

private val BuiltInIdsByLabel: Map<String, LanguageId> =
    LanguageId.BuiltIns.associateBy { it.value }

internal fun resolveBuiltInLanguageLabel(label: String): LanguageId? {
    val normalized = label.normalizeLanguageValue() ?: return null
    return BuiltInAliases[normalized]
        ?: BuiltInIdsByLabel[normalized]
}

internal fun builtInLanguageLabelsFor(languageId: LanguageId): Set<String> {
    if (languageId !in LanguageId.BuiltIns) return emptySet()

    val labels = mutableSetOf(languageId.value)
    labels += BuiltInAliasesByLanguage[languageId].orEmpty()
    return labels
}
