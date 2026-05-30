package com.gallatinapps.syntaxmp.benchmarks

import java.lang.management.ManagementFactory
import kotlin.math.ceil

internal class BenchmarkTimer {
    private val allocationTracker = ThreadAllocationTracker.create()

    fun run(case: BenchmarkCase): BenchmarkCaseResult {
        repeat(case.warmupIterations) {
            BenchmarkSink.consume(case.run().checksum)
        }

        val sampleNanos = LongArray(case.measuredIterations)
        val sampleAllocations = mutableListOf<Long>()
        var lastIteration = BenchmarkIteration(spanCount = 0, checksum = 0L)
        var combinedChecksum = 0L
        repeat(case.measuredIterations) { index ->
            val allocationBefore = allocationTracker?.currentThreadAllocatedBytes()
            val startNanos = System.nanoTime()
            lastIteration = case.run()
            sampleNanos[index] = System.nanoTime() - startNanos
            val allocationAfter = allocationTracker?.currentThreadAllocatedBytes()
            if (allocationBefore != null && allocationAfter != null) {
                sampleAllocations += (allocationAfter - allocationBefore).coerceAtLeast(0L)
            }
            combinedChecksum = combinedChecksum.mix(lastIteration.checksum)
            BenchmarkSink.consume(combinedChecksum)
        }

        return BenchmarkCaseResult(
            id = case.id,
            group = case.group,
            name = case.name,
            workloadKind = case.workloadKind,
            sizeName = case.sizeName,
            inputChars = case.inputChars,
            inputLines = case.inputLines,
            spanCount = lastIteration.spanCount,
            samples = BenchmarkSamples(sampleNanos.toList()),
            allocatedBytes = sampleAllocations
                .takeIf { it.size == case.measuredIterations }
                ?.let(::BenchmarkByteSamples),
            checksum = combinedChecksum,
            warmupIterations = case.warmupIterations,
            measuredIterations = case.measuredIterations,
        )
    }
}

private class ThreadAllocationTracker(
    private val bean: com.sun.management.ThreadMXBean,
) {
    fun currentThreadAllocatedBytes(): Long? =
        bean.getThreadAllocatedBytes(Thread.currentThread().id).takeIf { it >= 0L }

    companion object {
        fun create(): ThreadAllocationTracker? =
            runCatching {
                val bean = ManagementFactory.getThreadMXBean()
                if (bean !is com.sun.management.ThreadMXBean) return null
                if (!bean.isThreadAllocatedMemorySupported()) return null
                if (!bean.isThreadAllocatedMemoryEnabled()) {
                    bean.setThreadAllocatedMemoryEnabled(true)
                }
                ThreadAllocationTracker(bean)
            }.getOrNull()
    }
}

internal object BenchmarkSink {
    @Volatile
    private var value: Long = 0L

    fun consume(checksum: Long) {
        value = value.mix(checksum)
    }

    fun current(): Long = value
}

internal fun Long.mix(value: Long): Long =
    (this xor value)
        .rotateLeft(13)
        .let { it * -7046029254386353131L + 0x632BE59BD9B4E019L }

internal fun Long.mix(value: Int): Long = mix(value.toLong())

internal fun List<Long>.percentile(percentile: Double): Long {
    if (isEmpty()) return 0L
    val sorted = sorted()
    val rawIndex = ceil(percentile * sorted.size).toInt() - 1
    return sorted[rawIndex.coerceIn(0, sorted.lastIndex)]
}
