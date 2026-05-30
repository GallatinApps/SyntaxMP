package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixtureSelfCheck
import java.nio.file.Path
import java.util.Locale

fun main() {
    LanguageBenchmarkFixtureSelfCheck.run()
    val timer = BenchmarkTimer()
    val cases = benchmarkCases()

    println("Running SyntaxMP benchmarks")
    val results = cases.mapIndexed { index, case ->
        println("Running ${index + 1}/${cases.size}: ${case.group} - ${case.name}")
        timer.run(case).also { result ->
            val allocationSummary = result.allocatedBytes
                ?.let { " alloc=${it.medianBytes.formatBenchmarkKiB()} KiB" }
                .orEmpty()
            println(
                "Finished ${index + 1}/${cases.size}: " +
                    "median=${result.samples.medianMs.formatBenchmarkMs()} ms" +
                    allocationSummary +
                    " " +
                    "checksum=${result.checksum}",
            )
        }
    }
    val report = BenchmarkReport(
        metadata = collectBenchmarkMetadata(),
        results = results,
        sinkChecksum = BenchmarkSink.current(),
        comparison = runCatching {
            loadBenchmarkComparison(
                compareToPath = System.getProperty("syntaxmp.benchmark.compareTo"),
                currentResults = results,
            )
        }.onFailure { error ->
            println("Benchmark comparison skipped: ${error.message}")
        }.getOrNull(),
    )
    report.printToConsole()

    val reportDir = System.getProperty("syntaxmp.benchmark.reportDir")
        ?: "syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks"
    val reportPaths = report.writeTo(Path.of(reportDir))
    println("Wrote historical benchmark report: ${reportPaths.historicalReport}")
    println("Wrote latest benchmark report: ${reportPaths.latestReport}")
}

private fun benchmarkCases(): List<BenchmarkCase> =
    RepresentativeLanguageBenchmarks.cases() + diagnosticCases()

private fun diagnosticCases(): List<BenchmarkCase> =
    TokenizationBenchmarks.diagnosticCases() +
        NormalizerBenchmarks.cases() +
        EmbeddedSubstringBenchmarks.cases() +
        ComposeSpanBenchmarks.cases()

private fun Double.formatBenchmarkMs(): String =
    "%,.3f".format(Locale.US, this)

private fun Long.formatBenchmarkKiB(): String =
    "%,.1f".format(Locale.US, this / 1024.0)
