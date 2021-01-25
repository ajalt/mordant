package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.ProgressBuilder
import com.github.ajalt.mordant.components.ProgressLayout
import com.github.ajalt.mordant.components.ProgressState
import com.github.ajalt.mordant.internal.nanosToSeconds
import com.github.ajalt.mordant.terminal.Terminal
import java.util.concurrent.TimeUnit


private class ProgressHistoryEntry(val timeNs: Long, val completed: Long)
private class ProgressHistory(windowLengthSeconds: Float, private val timeSource: () -> Long) {
    private var startTime: Long = -1
    private val samples = ArrayDeque<ProgressHistoryEntry>()
    private val windowLengthNs = (TimeUnit.SECONDS.toNanos(1) * windowLengthSeconds).toLong()

    fun start() {
        if (!started) {
            startTime = timeSource()
        }
    }

    fun clear() {
        startTime = -1
        samples.clear()
    }

    fun update(completed: Long) {
        start()
        val now = timeSource()
        val keepTime = now - windowLengthNs
        while (samples.firstOrNull().let { it != null && it.timeNs < keepTime }) {
            samples.removeFirst()
        }
        samples.addLast(ProgressHistoryEntry(now, completed))
    }

    fun makeState(total: Long?) = ProgressState(
        completed = completed,
        total = total,
        completedPerSecond = completedPerSecond,
        elapsedSeconds = elapsedSeconds,
    )

    val started: Boolean get() = startTime >= 0

    val completed: Long
        get() = samples.lastOrNull()?.completed ?: 0

    private val elapsedSeconds: Double
        get() = if (startTime >= 0) nanosToSeconds(timeSource() - startTime) else 0.0

    private val completedPerSecond: Double
        get() {
            if (startTime < 0 || samples.size < 2) return 0.0
            val sampleTimespan = nanosToSeconds(samples.last().timeNs - samples.first().timeNs)
            val complete = samples.last().completed - samples.first().completed
            return if (complete <= 0 || sampleTimespan <= 0) 0.0 else complete / sampleTimespan
        }
}

class ProgressAnimation internal constructor(
    t: Terminal,
    private val layout: ProgressLayout,
    historyLength: Float,
    private val ticker: Ticker,
    timeSource: () -> Long,
) {
    private var total: Long? = null
    private val history = ProgressHistory(historyLength, timeSource)
    private val animation = t.animation<Unit> {
        val state = history.makeState(total)
        layout.build(state.completed, state.total, state.elapsedSeconds, state.completedPerSecond)
    }

    fun update(completed: Long) {
        synchronized(history) {
            val started = history.started
            history.update(completed)
            if (!started) {
                animation.update(Unit)
            }
        }
    }

    fun update() {
        update(history.completed)
    }

    fun update(completed: Long, total: Long?) {
        updateTotalWithoutAnimation(total)
        update(completed)
    }

    fun updateTotal(total: Long?) {
        updateTotalWithoutAnimation(total)
        update()
    }

    private fun updateTotalWithoutAnimation(total: Long?) {
        this.total = total?.takeIf { it > 0 }
    }

    fun advance(amount: Long = 1) {
        update(history.completed + amount)
    }

    fun start() {
        history.start()
        ticker.start {
            update()
            animation.update(Unit)
        }
    }

    fun stop() {
        ticker.stop()
    }

    fun restart() {
        stop()
        update(0)
        start()
    }

    fun clear() {
        stop()
        history.clear()
        animation.clear()
    }
}

class ProgressAnimationBuilder internal constructor() : ProgressBuilder() {
    /** The maximum number of times per second to update idle animations like the progress bar pulse. */
    var frameRate: Int = 10
    var historyLength: Float = 30f
    var autoUpdate: Boolean = true

    // for testing
    internal var timeSource: () -> Long = { System.nanoTime() }
}

// TODO[next]: move throttling here
fun Terminal.progressAnimation(init: ProgressAnimationBuilder.() -> Unit): ProgressAnimation {
    val builder = ProgressAnimationBuilder().apply(init)

    return ProgressAnimation(
        t = this,
        layout = builder.build(),
        historyLength = builder.historyLength,
        ticker = getTicker(builder.frameRate),
        timeSource = builder.timeSource
    )
}
