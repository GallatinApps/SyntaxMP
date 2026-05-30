package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Path

fun main() {
    val sourcePaths = aggregateRunPathsFromProperty(
        System.getProperty("syntaxmp.benchmark.aggregateRuns"),
    )
    val outputParent = System.getProperty("syntaxmp.benchmark.aggregateOutputDir")
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it) }
        ?: Path.of(
            System.getProperty("syntaxmp.benchmark.reportDir")
                ?: "syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks",
        ).resolve("aggregates")

    val aggregate = buildBenchmarkRunAggregate(sourcePaths)
    aggregate.printToConsole()
    val paths = aggregate.writeTo(outputParent)
    println("Wrote benchmark aggregate report: ${paths.index}")
    println("Wrote benchmark aggregate JSON: ${paths.dataJson}")
}

private fun aggregateRunPathsFromProperty(rawValue: String?): List<Path> {
    val value = rawValue?.trim().orEmpty()
    require(value.isNotEmpty()) {
        "Set -Dsyntaxmp.benchmark.aggregateRuns=<run-dir-or-metadata-json>[,<run-dir-or-metadata-json>...]"
    }
    return value
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { Path.of(it) }
}
