package com.github.ajalt.mordant.animation

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal interface Ticker {
    fun start(onTick: () -> Unit)
    fun stop()
}

internal fun getTicker(ticksPerSecond: Int?): Ticker {
    return if (ticksPerSecond == null) DisabledTicker() else ExecutorTicker(ticksPerSecond)
}

private class ExecutorTicker(
    private val ticksPerSecond: Int,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor {
        Executors.defaultThreadFactory().newThread(it).apply { isDaemon = true }
    },
) : Ticker {
    private var future: Future<*>? = null
    override fun start(onTick: () -> Unit) {
        if (future != null) return
        val period = 1000L / ticksPerSecond
        future = executor.scheduleAtFixedRate({ onTick() }, period, period, TimeUnit.MILLISECONDS)
    }

    override fun stop() {
        future?.cancel(false)
        future = null
    }
}

private class DisabledTicker : Ticker {
    override fun start(onTick: () -> Unit) {}
    override fun stop() {}
}
