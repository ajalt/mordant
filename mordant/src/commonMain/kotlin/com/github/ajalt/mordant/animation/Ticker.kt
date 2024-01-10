package com.github.ajalt.mordant.animation

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

internal interface Ticker {
    suspend fun start(onTick: suspend () -> Unit)
    fun stop()
}

internal fun getTicker(ticksPerSecond: Int?): Ticker {
    return if (ticksPerSecond == null) DisabledTicker else TickerImpl(ticksPerSecond)
}

private class TickerImpl(private val ticksPerSecond: Int) : Ticker {
    private val scope = CoroutineScope(Job()) +
            Dispatchers.Unconfined +
            CoroutineName("TickerImpl(ticksPerSecond=$ticksPerSecond)") +
            CoroutineExceptionHandler { _, throwable ->
                if (throwable != stopTicker) {
                    throw throwable
                }
            }
    private val tickActiveLock: Mutex = Mutex(locked = false)

    override suspend fun start(onTick: suspend () -> Unit) {
        if (tickActiveLock.isLocked) return
        tickActiveLock.withLock(owner = this) {
            scope.launch {
                while (true) {
                    onTick()
                    delay(1.seconds / ticksPerSecond)
                }
            }
        }
    }

    override fun stop() {
        scope.coroutineContext.cancelChildren(stopTicker)
    }

    companion object {
        private val stopTicker = CancellationException("stop ticker")
    }
}

private object DisabledTicker : Ticker {
    override suspend fun start(onTick: suspend () -> Unit) {}
    override fun stop() {}
}
