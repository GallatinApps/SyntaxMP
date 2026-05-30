package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Path

fun main() {
    val reportDir = System.getProperty("syntaxmp.profiling.reportDir")
        ?: "syntaxmp-benchmarks/build/reports/syntaxmp-profiling"
    val reportPath = KotlinHotPathProfiler.run(reportDir = Path.of(reportDir))
    println("Wrote Kotlin hot-path profile report: $reportPath")
}
