import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvmToolchain(17)

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":syntaxmp"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}

val benchmarkReportDir = layout.buildDirectory.dir("reports/syntaxmp-benchmarks")
val profilingReportDir = layout.buildDirectory.dir("reports/syntaxmp-profiling")

tasks.register<JavaExec>("runSyntaxMpBenchmarks") {
    group = "benchmark"
    description = "Runs the local SyntaxMP JVM benchmark harness."

    dependsOn("jvmJar")
    mainClass.set("com.gallatinapps.syntaxmp.benchmarks.MainKt")
    classpath(
        tasks.named("jvmJar"),
        configurations.named("jvmRuntimeClasspath"),
    )
    val benchmarkTaskName = "runSyntaxMpBenchmarks"
    val compareTo = providers.systemProperty("syntaxmp.benchmark.compareTo").orNull
    val invocationProperties = listOfNotNull(
        compareTo?.let { "-Dsyntaxmp.benchmark.compareTo=$it" },
    )
    outputs.dir(benchmarkReportDir)
    outputs.upToDateWhen { false }
    systemProperty(
        "syntaxmp.benchmark.gradleInvocation",
        (listOf("../gradlew") + invocationProperties + listOf(
            "-p",
            ".",
            ":syntaxmp-benchmarks:$benchmarkTaskName",
        )).joinToString(separator = " "),
    )
    systemProperty(
        "syntaxmp.benchmark.reportDir",
        benchmarkReportDir.get().asFile.absolutePath,
    )
    compareTo?.let { compareTo ->
        systemProperty("syntaxmp.benchmark.compareTo", compareTo)
    }
}

tasks.register<JavaExec>("aggregateSyntaxMpBenchmarkRuns") {
    group = "benchmark"
    description = "Aggregates historical SyntaxMP benchmark metadata runs."

    dependsOn("jvmJar")
    mainClass.set("com.gallatinapps.syntaxmp.benchmarks.BenchmarkAggregateMainKt")
    workingDir = rootProject.projectDir
    classpath(
        tasks.named("jvmJar"),
        configurations.named("jvmRuntimeClasspath"),
    )
    val aggregateRuns = providers.systemProperty("syntaxmp.benchmark.aggregateRuns").orNull
    val aggregateOutputDir = providers.systemProperty("syntaxmp.benchmark.aggregateOutputDir").orNull
    outputs.dir(benchmarkReportDir)
    outputs.upToDateWhen { false }
    systemProperty(
        "syntaxmp.benchmark.reportDir",
        benchmarkReportDir.get().asFile.absolutePath,
    )
    aggregateRuns?.let { runs ->
        systemProperty("syntaxmp.benchmark.aggregateRuns", runs)
    }
    aggregateOutputDir?.let { outputDir ->
        systemProperty("syntaxmp.benchmark.aggregateOutputDir", outputDir)
    }
}

tasks.register<JavaExec>("runKotlinHotPathProfiling") {
    group = "benchmark"
    description = "Runs Kotlin-focused JVM/JFR profiling diagnostics for SyntaxMP benchmarks."

    dependsOn("jvmJar")
    mainClass.set("com.gallatinapps.syntaxmp.benchmarks.KotlinProfilingMainKt")
    classpath(
        tasks.named("jvmJar"),
        configurations.named("jvmRuntimeClasspath"),
    )
    outputs.dir(profilingReportDir)
    outputs.upToDateWhen { false }
    systemProperty(
        "syntaxmp.profiling.gradleInvocation",
        "../gradlew -p . :syntaxmp-benchmarks:runKotlinHotPathProfiling",
    )
    systemProperty(
        "syntaxmp.profiling.reportDir",
        profilingReportDir.get().asFile.absolutePath,
    )
    systemProperty(
        "syntaxmp.profiling.referenceBenchmarkReport",
        benchmarkReportDir.get().asFile.resolve("latest/report.md").absolutePath,
    )
}
