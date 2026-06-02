package com.gallatinapps.syntaxmp.primitives.strings

internal data class StringLiteralOptions(
    val startRules: List<StringLiteralStartRule>,
    val regex: RegexLiteralOptions = RegexLiteralOptions.Disabled,
) {
    private val rulesByLeadingChar: Map<Char, List<StringLiteralStartRule>> =
        startRules
            .flatMap { rule -> rule.leadingChars.map { char -> char to rule } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    fun rulesStartingWith(char: Char): List<StringLiteralStartRule> =
        rulesByLeadingChar[char].orEmpty()

    companion object {
        fun cLikeDefaults(): StringLiteralOptions =
            StringLiteralOptions(
                startRules = listOf(
                    QuotedStringRule(
                        quotes = setOf('"', '\''),
                        escapes = EscapeMode.Backslash,
                    ),
                ),
            )

        fun scriptLikeDefaults(): StringLiteralOptions =
            StringLiteralOptions(
                startRules = listOf(
                    QuotedStringRule(
                        quotes = setOf('"', '\''),
                        escapes = EscapeMode.Backslash,
                    ),
                ),
            )
    }
}

internal data class RegexLiteralOptions(
    val enabled: Boolean,
) {
    companion object {
        val Disabled = RegexLiteralOptions(enabled = false)
        val SlashDelimited = RegexLiteralOptions(enabled = true)
    }
}
