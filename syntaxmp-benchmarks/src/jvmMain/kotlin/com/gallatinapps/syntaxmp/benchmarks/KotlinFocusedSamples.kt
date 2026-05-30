package com.gallatinapps.syntaxmp.benchmarks

/**
 * Focused Kotlin shapes (raw strings, interpolation, imports, identifiers, annotations) that isolate
 * specific scanner hot paths for the diagnostic rows and the hot-path profiler. Distinct from the
 * representative `fixtures/` matrix and from [BenchmarkSamplesFactory] synthetic stress inputs.
 */
internal object KotlinFocusedSamples {
    fun tripleQuotedStringNoInterpolation(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/raw-string-no-interpolation/${size.idSegment}",
            name = "Kotlin triple-quoted string no interpolation ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = tripleQuotedStringDocument(
                targetChars = size.targetChars,
                includeSparseInterpolation = false,
            ),
            sizeName = size.displayName,
            workloadKind = if (size.targetChars >= BenchmarkTargetSize.LargeFile.targetChars) {
                BenchmarkWorkloadKind.LowSpanPayload
            } else {
                BenchmarkWorkloadKind.FocusedScannerShape
            },
        )

    fun tripleQuotedStringSparseInterpolation(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/raw-string-sparse-interpolation/${size.idSegment}",
            name = "Kotlin triple-quoted string sparse interpolation ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = tripleQuotedStringDocument(
                targetChars = size.targetChars,
                includeSparseInterpolation = true,
            ),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.FocusedScannerShape,
        )

    fun manyImportPackageLines(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/imports-packages/${size.idSegment}",
            name = "Kotlin many import/package lines ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = importPackageLinesDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.FocusedScannerShape,
        )

    fun manyOrdinaryIdentifiers(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/ordinary-identifiers/${size.idSegment}",
            name = "Kotlin many ordinary identifiers ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = ordinaryIdentifiersDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.FocusedScannerShape,
        )

    fun manyNamedArgumentsAndCallChains(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/named-arguments-call-chains/${size.idSegment}",
            name = "Kotlin many named arguments and nested call chains ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = namedArgumentsAndCallChainsDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.FocusedScannerShape,
        )

    fun manyAnnotations(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/kotlin-focused/annotations-use-site/${size.idSegment}",
            name = "Kotlin many annotations and use-site annotations ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = annotationsDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.FocusedScannerShape,
        )
}

private fun tripleQuotedStringDocument(
    targetChars: Int,
    includeSparseInterpolation: Boolean,
): String {
    val prefix = """
        package bench.generated

        fun rawPayload(name: String, count: Int): String {
            val payload = ""${'"'}
    """.trimIndent()
    val suffix = """
            ""${'"'}.trimIndent()
            return payload
        }

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        append(prefix)
        append('\n')
        var line = 0
        while (length + suffix.length < targetChars) {
            if (includeSparseInterpolation && line % 48 == 0) {
                append("marker ")
                append(line)
                append(": ")
                append("${'$'}name")
                append(" -> ")
                append("${'$'}{count + ")
                append(line)
                append("}\n")
            } else {
                append("plain text line ")
                append(line)
                append(" alpha beta gamma delta epsilon zeta eta theta\n")
            }
            line++
        }
        append(suffix)
    }
}

private fun importPackageLinesDocument(targetChars: Int): String =
    buildString(targetChars + 512) {
        var index = 0
        while (length < targetChars) {
            appendLine("package bench.generated.module$index")
            appendLine("import bench.generated.module$index.Type$index")
            appendLine("import bench.generated.module$index.submodule.Helper$index as HelperAlias$index")
            appendLine("import kotlin.collections.List")
            appendLine()
            index++
        }
        appendLine("fun importPackageMarker(): Unit = Unit")
    }

private fun ordinaryIdentifiersDocument(targetChars: Int): String {
    val suffix = """
            return accumulator
        }

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        appendLine("fun ordinaryIdentifierWorkload(seed: Int): Int {")
        appendLine("    var accumulator = seed")
        var index = 0
        while (length + suffix.length < targetChars) {
            appendLine("    val value$index = accumulator + $index")
            appendLine("    val alpha$index = value$index + accumulator")
            appendLine("    val beta$index = alpha$index + value$index")
            appendLine("    accumulator += beta$index")
            index++
        }
        append(suffix)
    }
}

private fun namedArgumentsAndCallChainsDocument(targetChars: Int): String {
    val prefix = """
        data class ChainItem(
            val title: String,
            val folder: FolderPath,
        )

        fun nestedCallChainWorkload(item: ChainItem): String {
    """.trimIndent()
    val suffix = """
            return rendered0
        }

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        append(prefix)
        append('\n')
        var index = 0
        do {
            appendLine("    val rendered$index = item.title")
            appendLine("        .trim()")
            appendLine("        .replace(oldValue = \"draft\", newValue = \"ready\")")
            appendLine("        .substring(startIndex = 0, endIndex = item.title.length.coerceAtMost(maximumValue = 24))")
            appendLine("        .plus(other = item.folder.resolveSegment(segment = \"archive\").normalizePath(strict = false))")
            appendLine("        .formatForDisplay(prefix = \"item-$index\", maxLines = 3)")
            index++
        } while (length + suffix.length < targetChars)
        append(suffix)
    }
}

private fun annotationsDocument(targetChars: Int): String =
    buildString(targetChars + 1024) {
        appendLine("@file:Suppress(\"unused\", \"UNCHECKED_CAST\")")
        appendLine()
        appendLine("@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)")
        appendLine("annotation class BenchMarker(val value: String = \"\")")
        appendLine()
        var index = 0
        while (length < targetChars) {
            appendLine("@BenchMarker(\"type-$index\")")
            appendLine("@Deprecated(\"bench-$index\")")
            appendLine("data class AnnotatedItem$index(")
            appendLine("    @field:BenchMarker(\"field-$index\")")
            appendLine("    @get:BenchMarker(\"getter-$index\")")
            appendLine("    @param:BenchMarker(\"param-$index\")")
            appendLine("    val title$index: String,")
            appendLine("    @setparam:BenchMarker(\"setter-$index\")")
            appendLine("    var enabled$index: Boolean,")
            appendLine(")")
            appendLine()
            appendLine("@receiver:BenchMarker(\"receiver-$index\")")
            appendLine("fun String.annotatedExtension$index(")
            appendLine("    @param:BenchMarker(\"argument-$index\") input$index: String,")
            appendLine("): String = this + input$index")
            appendLine()
            index++
        }
    }
