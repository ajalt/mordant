package com.github.ajalt.mordant.animation

import java.util.*
import kotlin.concurrent.timer

internal interface Ticker {
    fun start(onTick: () -> Unit)
    fun stop()
}

internal fun getTicker(ticksPerSecond: Int?): Ticker {
    return if (ticksPerSecond == null) DisabledTicker() else JvmTicker(ticksPerSecond)
}

private class JvmTicker(private val ticksPerSecond: Int) : Ticker {
    private var timer: Timer? = null
    override fun start(onTick: () -> Unit) {
        if (timer != null) return
        val period = 1000L / ticksPerSecond
        timer = timer(startAt = Date(0), period = period, daemon = true) { onTick() }
    }

    override fun stop() {
        timer?.cancel()
        timer = null
    }
}

private class DisabledTicker : Ticker {
    override fun start(onTick: () -> Unit) {}
    override fun stop() {}
}
