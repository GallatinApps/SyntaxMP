package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.Locale
import jdk.jfr.FlightRecorder
import jdk.jfr.Recording
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordingFile
import kotlin.io.path.writeText
import kotlin.system.measureNanoTime

internal object KotlinHotPathProfiler {
    private const val REPORT_NAME = "kotlin-hot-path-profile-report.md"

    fun run(reportDir: Path): Path {
        require(FlightRecorder.isAvailable()) { "Java Flight Recorder is not available on this JVM." }

        Files.createDirectories(reportDir)
        val results = profilingTargets().map { target ->
            println("Profiling ${target.displayName}")
            profileTarget(target = target, reportDir = reportDir).also { result ->
                println(
                    "Finished ${target.displayName}: " +
                        "samples=${result.executionSampleCount} " +
                        "allocEvents=${result.allocationEventCount} " +
                        "checksum=${result.combinedChecksum}",
                )
            }
        }
        val report = KotlinProfileReport(
            startedAt = Instant.now(),
            metadata = collectBenchmarkMetadata(),
            referenceBenchmarkReportPath = referenceBenchmarkReportPath(),
            referenceBenchmarkSinkChecksum = referenceBenchmarkSinkChecksum(),
            results = results,
        )
        val reportPath = reportDir.resolve(REPORT_NAME)
        reportPath.writeText(report.toMarkdown())
        return reportPath
    }

    private fun profilingTargets(): List<KotlinProfileTarget> =
        listOf(
            KotlinProfileTarget(
                id = "kotlin-representative-medium",
                displayName = "Kotlin representative medium",
                sample = BenchmarkSamplesFactory.kotlinRepresentative(BenchmarkTargetSize.MediumEditorFile),
                measuredIterations = 12,
            ),
            KotlinProfileTarget(
                id = "kotlin-compose-dashboard-x10",
                displayName = "Kotlin Compose dashboard x10",
                sample = KotlinComposeSamples.notesDashboard(multiplier = 10),
                measuredIterations = 8,
            ),
            KotlinProfileTarget(
                id = "kotlin-raw-string-no-interpolation-large",
                displayName = "Kotlin raw string no interpolation large",
                sample = KotlinFocusedSamples.tripleQuotedStringNoInterpolation(BenchmarkTargetSize.LargeFile),
                measuredIterations = 16,
                allocationProfile = true,
            ),
            KotlinProfileTarget(
                id = "kotlin-raw-string-sparse-interpolation-large",
                displayName = "Kotlin raw string sparse interpolation large",
                sample = KotlinFocusedSamples.tripleQuotedStringSparseInterpolation(BenchmarkTargetSize.LargeFile),
                measuredIterations = 8,
                allocationProfile = true,
            ),
            KotlinProfileTarget(
                id = "kotlin-import-package-medium",
                displayName = "Kotlin import/package lines medium",
                sample = KotlinFocusedSamples.manyImportPackageLines(BenchmarkTargetSize.MediumEditorFile),
                measuredIterations = 10,
            ),
            KotlinProfileTarget(
                id = "kotlin-ordinary-identifiers-medium",
                displayName = "Kotlin ordinary identifiers medium",
                sample = KotlinFocusedSamples.manyOrdinaryIdentifiers(BenchmarkTargetSize.MediumEditorFile),
                measuredIterations = 10,
            ),
            KotlinProfileTarget(
                id = "kotlin-named-arguments-call-chains-medium",
                displayName = "Kotlin named arguments and call chains medium",
                sample = KotlinFocusedSamples.manyNamedArgumentsAndCallChains(BenchmarkTargetSize.MediumEditorFile),
                measuredIterations = 10,
            ),
        )

    private fun profileTarget(
        target: KotlinProfileTarget,
        reportDir: Path,
    ): KotlinProfileResult {
        val engine = SyntaxTokenizer()
        repeat(target.warmupIterations) {
            val spans = engine.tokenize(code = target.sample.code, languageLabel = target.sample.languageLabel)
            BenchmarkSink.consume(checksumTokenSpans(target.sample.code, spans))
        }

        val recordingPath = reportDir.resolve("${target.id}.jfr")
        val recording = Recording().apply {
            name = target.displayName
            enable("jdk.ExecutionSample")
                .withPeriod(Duration.ofMillis(1))
                .withStackTrace()
            if (target.allocationProfile) {
                enable("jdk.ObjectAllocationInNewTLAB")
                    .withThreshold(Duration.ZERO)
                    .withStackTrace()
                enable("jdk.ObjectAllocationOutsideTLAB")
                    .withThreshold(Duration.ZERO)
                    .withStackTrace()
            }
        }

        var lastSpanCount = 0
        var lastChecksum = 0L
        var combinedChecksum = 0x6B2D4B5B7342A1F1L.mix(target.sample.chars)
        val elapsedNanos = recording.use {
            it.start()
            measureNanoTime {
                repeat(target.measuredIterations) { iteration ->
                    val spans = engine.tokenize(code = target.sample.code, languageLabel = target.sample.languageLabel)
                    lastSpanCount = spans.size
                    lastChecksum = checksumTokenSpans(target.sample.code, spans)
                    combinedChecksum = combinedChecksum.mix(iteration).mix(lastChecksum)
                    BenchmarkSink.consume(combinedChecksum)
                }
            }.also { _ ->
                it.stop()
                it.dump(recordingPath)
            }
        }

        val events = RecordingFile.readAllEvents(recordingPath)
        val executionEvents = events.filter { it.eventType.name == "jdk.ExecutionSample" }
        val allocationEvents = events.filter { it.eventType.name in allocationEventNames }
        return KotlinProfileResult(
            target = target,
            elapsedNanos = elapsedNanos,
            lastSpanCount = lastSpanCount,
            lastChecksum = lastChecksum,
            combinedChecksum = combinedChecksum,
            executionSampleCount = executionEvents.size,
            allocationEventCount = allocationEvents.size,
            capturedAllocationBytes = allocationEvents.sumOf { it.allocationSize() },
            topSelfMethods = topSelfMethods(executionEvents),
            executionCategories = stackCategorySamples(executionEvents),
            topAllocationMethods = topAllocationMethods(allocationEvents),
            allocationCategories = stackCategoryAllocations(allocationEvents),
            recordingPath = recordingPath,
        )
    }

    private fun topSelfMethods(events: List<RecordedEvent>): List<MethodSampleStat> =
        events
            .mapNotNull { it.topSyntaxFrame() }
            .groupingBy { it.methodDisplayName() }
            .eachCount()
            .map { (method, count) -> MethodSampleStat(method = method, samples = count) }
            .sortedByDescending { it.samples }
            .take(12)

    private fun topAllocationMethods(events: List<RecordedEvent>): List<MethodAllocationStat> =
        events
            .mapNotNull { event ->
                val method = event.topSyntaxFrame()?.methodDisplayName() ?: return@mapNotNull null
                method to event.allocationSize()
            }
            .groupBy({ it.first }, { it.second })
            .map { (method, sizes) ->
                MethodAllocationStat(
                    method = method,
                    events = sizes.size,
                    bytes = sizes.sum(),
                )
            }
            .sortedByDescending { it.bytes }
            .take(12)

    private fun stackCategorySamples(events: List<RecordedEvent>): List<CategorySampleStat> =
        ProfileCategory.entries.mapNotNull { category ->
            val count = events.count { event -> event.syntaxFrames().any(category::matches) }
            count.takeIf { it > 0 }?.let {
                CategorySampleStat(category = category.displayName, samples = count)
            }
        }.sortedByDescending { it.samples }

    private fun stackCategoryAllocations(events: List<RecordedEvent>): List<CategoryAllocationStat> =
        ProfileCategory.entries.mapNotNull { category ->
            var eventCount = 0
            var bytes = 0L
            events.forEach { event ->
                if (event.syntaxFrames().any(category::matches)) {
                    eventCount++
                    bytes += event.allocationSize()
                }
            }
            eventCount.takeIf { it > 0 }?.let {
                CategoryAllocationStat(
                    category = category.displayName,
                    events = eventCount,
                    bytes = bytes,
                )
            }
        }.sortedByDescending { it.bytes }

    private fun RecordedEvent.topSyntaxFrame(): RecordedFrame? =
        syntaxFrames().firstOrNull()

    private fun RecordedEvent.syntaxFrames(): List<RecordedFrame> =
        stackTrace?.frames
            .orEmpty()
            .filter { frame ->
                val className = frame.method.type.name
                className.startsWith("com.gallatinapps.syntaxmp.") &&
                    !className.startsWith("com.gallatinapps.syntaxmp.benchmarks.")
            }

    private fun RecordedFrame.methodDisplayName(): String {
        val method = method
        val className = method.type.name.removePrefix("com.gallatinapps.syntaxmp.")
        return "$className.${method.name}"
    }

    private fun RecordedEvent.allocationSize(): Long =
        runCatching { getLong("allocationSize") }
            .getOrElse { runCatching { getLong("tlabSize") }.getOrDefault(0L) }

    private fun referenceBenchmarkReportPath(): Path? =
        System.getProperty("syntaxmp.profiling.referenceBenchmarkReport")
            ?.takeIf { it.isNotBlank() }
            ?.let(Path::of)
            ?.takeIf(Files::exists)

    private fun referenceBenchmarkSinkChecksum(): String? =
        referenceBenchmarkReportPath()
            ?.let { path ->
                Files.readAllLines(path)
                    .firstOrNull { it.startsWith("- Sink checksum:") }
                    ?.substringAfter('`')
                    ?.substringBefore('`')
            }
}

private val allocationEventNames = setOf(
    "jdk.ObjectAllocationInNewTLAB",
    "jdk.ObjectAllocationOutsideTLAB",
)

private data class KotlinProfileTarget(
    val id: String,
    val displayName: String,
    val sample: SourceSample,
    val warmupIterations: Int = 4,
    val measuredIterations: Int,
    val allocationProfile: Boolean = false,
)

private data class KotlinProfileResult(
    val target: KotlinProfileTarget,
    val elapsedNanos: Long,
    val lastSpanCount: Int,
    val lastChecksum: Long,
    val combinedChecksum: Long,
    val executionSampleCount: Int,
    val allocationEventCount: Int,
    val capturedAllocationBytes: Long,
    val topSelfMethods: List<MethodSampleStat>,
    val executionCategories: List<CategorySampleStat>,
    val topAllocationMethods: List<MethodAllocationStat>,
    val allocationCategories: List<CategoryAllocationStat>,
    val recordingPath: Path,
) {
    val elapsedMs: Double = elapsedNanos / 1_000_000.0
    val meanIterationMs: Double = elapsedMs / target.measuredIterations
}

private data class MethodSampleStat(
    val method: String,
    val samples: Int,
)

private data class MethodAllocationStat(
    val method: String,
    val events: Int,
    val bytes: Long,
)

private data class CategorySampleStat(
    val category: String,
    val samples: Int,
)

private data class CategoryAllocationStat(
    val category: String,
    val events: Int,
    val bytes: Long,
)

private enum class ProfileCategory(
    val displayName: String,
    private val matcher: (RecordedFrame) -> Boolean,
) {
    StringLiteralScanner(
        displayName = "String literal scanner",
        matcher = { it.className.contains(".primitives.strings.") },
    ),
    CLikeScannerIdentifier(
        displayName = "CLikeScanner.scanIdentifier",
        matcher = { it.className.endsWith(".scanners.clike.CLikeScanner") && it.methodName == "scanIdentifier" },
    ),
    CLikeScannerOther(
        displayName = "CLike scanner other",
        matcher = { it.className.endsWith(".scanners.clike.CLikeScanner") && it.methodName != "scanIdentifier" },
    ),
    QualifiedNameScanning(
        displayName = "Qualified-name scanning",
        matcher = { it.className.contains(".primitives.QualifiedName") },
    ),
    SpanNormalization(
        displayName = "Span construction/normalization",
        matcher = { it.className.contains(".spans.") },
    ),
    LanguageRouting(
        displayName = "Language label/routing",
        matcher = {
            it.className.contains(".tokenizer.SyntaxTokenizer") ||
                it.className.contains(".routing.") ||
                it.className.contains(".language.")
        },
    ),
    KotlinTokenizer(
        displayName = "Kotlin tokenizer wiring",
        matcher = { it.className.contains(".builtins.kotlin.") },
    ),
    ;

    fun matches(frame: RecordedFrame): Boolean = matcher(frame)
}

private val RecordedFrame.className: String
    get() = method.type.name

private val RecordedFrame.methodName: String
    get() = method.name

private data class KotlinProfileReport(
    val startedAt: Instant,
    val metadata: BenchmarkMetadata,
    val referenceBenchmarkReportPath: Path?,
    val referenceBenchmarkSinkChecksum: String?,
    val results: List<KotlinProfileResult>,
) {
    fun toMarkdown(): String =
        buildString {
            appendLine("# Kotlin Hot-Path Profile Report")
            appendLine()
            appendLine("- Started: `${startedAt}`")
            appendLine("- CPU family: `${metadata.cpuFamily}`")
            appendLine("- OS: `${metadata.os}`")
            appendLine("- JDK: `${metadata.jdk}`")
            appendLine("- Kotlin: `${metadata.kotlinVersion}`")
            appendLine("- Gradle invocation: `${System.getProperty("syntaxmp.profiling.gradleInvocation") ?: "unknown"}`")
            appendLine("- Power: `${metadata.powerState}`")
            appendLine("- Reference benchmark report: `${referenceBenchmarkReportPath ?: "not found"}`")
            appendLine("- Reference benchmark sink checksum: `${referenceBenchmarkSinkChecksum ?: "n/a"}`")
            appendLine()
            appendLine("JFR execution samples are grouped by the top SyntaxMP stack frame outside the benchmark harness.")
            appendLine("Allocation rows use captured JFR allocation-event bytes, which are directional samples rather than total process allocation.")
            appendLine()
            appendSummaryTable()
            results.forEach { result ->
                appendLine()
                appendResult(result)
            }
        }

    private fun StringBuilder.appendSummaryTable() {
        appendLine("| Target | Chars | Lines | Spans | Iterations | Mean ms | Execution samples | Allocation events | Captured alloc KiB | Last checksum | Combined checksum | JFR |")
        appendLine("|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|")
        results.forEach { result ->
            appendLine(
                listOf(
                    result.target.displayName,
                    result.target.sample.chars.toString(),
                    result.target.sample.lines.toString(),
                    result.lastSpanCount.toString(),
                    result.target.measuredIterations.toString(),
                    result.meanIterationMs.formatDouble(),
                    result.executionSampleCount.toString(),
                    result.allocationEventCount.toString(),
                    result.capturedAllocationBytes.formatKiB(),
                    result.lastChecksum.toString(),
                    result.combinedChecksum.toString(),
                    result.recordingPath.toString(),
                ).toMarkdownRow(),
            )
        }
    }

    private fun StringBuilder.appendResult(result: KotlinProfileResult) {
        appendLine("## ${result.target.displayName}")
        appendLine()
        appendLine("| Top self method | Samples |")
        appendLine("|---|---:|")
        result.topSelfMethods.ifEmpty { listOf(MethodSampleStat(method = "n/a", samples = 0)) }
            .forEach { stat ->
                appendLine(listOf(stat.method, stat.samples.toString()).toMarkdownRow())
            }
        appendLine()
        appendLine("| Stack category | Samples with category on stack |")
        appendLine("|---|---:|")
        result.executionCategories.ifEmpty { listOf(CategorySampleStat(category = "n/a", samples = 0)) }
            .forEach { stat ->
                appendLine(listOf(stat.category, stat.samples.toString()).toMarkdownRow())
            }
        if (result.target.allocationProfile) {
            appendLine()
            appendLine("| Top allocation method | Events | Captured KiB |")
            appendLine("|---|---:|---:|")
            result.topAllocationMethods.ifEmpty { listOf(MethodAllocationStat(method = "n/a", events = 0, bytes = 0L)) }
                .forEach { stat ->
                    appendLine(listOf(stat.method, stat.events.toString(), stat.bytes.formatKiB()).toMarkdownRow())
                }
            appendLine()
            appendLine("| Allocation stack category | Events | Captured KiB |")
            appendLine("|---|---:|---:|")
            result.allocationCategories.ifEmpty { listOf(CategoryAllocationStat(category = "n/a", events = 0, bytes = 0L)) }
                .forEach { stat ->
                    appendLine(listOf(stat.category, stat.events.toString(), stat.bytes.formatKiB()).toMarkdownRow())
                }
        }
    }
}

private fun Double.formatDouble(): String =
    "%,.3f".format(Locale.US, this)

private fun Long.formatKiB(): String =
    "%,.1f".format(Locale.US, this / 1024.0)

private fun List<String>.toMarkdownRow(): String =
    joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() }

private fun String.escapeTableCell(): String =
    replace("|", "\\|")
