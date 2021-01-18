package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.ProgressBuilder
import com.github.ajalt.mordant.components.ProgressLayout
import com.github.ajalt.mordant.components.ProgressState
import com.github.ajalt.mordant.internal.nanosToSeconds
import com.github.ajalt.mordant.terminal.Terminal
import java.util.concurrent.TimeUnit


private class ProgressHistoryEntry(val timeNs: Long, val completed: Int)
private class ProgressHistory(windowLengthSeconds: Float, private val timeSource: () -> Long) {
    private var startTime: Long = -1
    private val samples = ArrayDeque<ProgressHistoryEntry>()
    private val windowLengthNs = (TimeUnit.SECONDS.toNanos(1) * windowLengthSeconds).toLong()

    fun start() {
        if (startTime < 0) {
            startTime = timeSource()
        }
    }

    fun clear() {
        startTime = -1
        samples.clear()
    }

    fun update(completed: Int) {
        start()
        val now = timeSource()
        val keepTime = now - windowLengthNs
        while (samples.firstOrNull().let { it != null && it.timeNs < keepTime }) {
            samples.removeFirst()
        }
        samples.addLast(ProgressHistoryEntry(now, completed))
    }

    fun makeState(total: Int?, frameRate: Int) = ProgressState(
        completed = completed,
        total = total,
        completedPerSecond = completedPerSecond,
        elapsedSeconds = elapsedSeconds,
        frameRate = frameRate,
    )

    val completed: Int
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

// TODO: thread safety
class ProgressAnimation internal constructor(
    t: Terminal,
    private val layout: ProgressLayout,
    private val frameRate: Int,
    historyLength: Float,
    private val ticker: Ticker,
    timeSource: () -> Long,
) {
    private var total: Int? = null
    private val history = ProgressHistory(historyLength, timeSource)
    private val animation = t.animation<Unit> {
            val state = history.makeState(total, frameRate)
        layout.build(state.completed, state.total, state.elapsedSeconds, state.completedPerSecond)
    }

    fun update() {
        update(history.completed)
    }

    // TODO: don't update animation when started
    fun update(completed: Int) {
        history.update(completed)
        animation.update(Unit)
    }

    fun update(completed: Int, total: Int?) {
        updateTotalWithoutAnimation(total)
        update(completed)
    }

    fun updateTotal(total: Int?) {
        updateTotalWithoutAnimation(total)
        update()
    }

    private fun updateTotalWithoutAnimation(total: Int?) {
        this.total = total?.takeIf { it > 0 }
    }

    fun advance(amount: Int = 1) {
        update(history.completed + amount)
    }

    fun start() {
        history.start()
        ticker.start { update() }
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
    var historyLength: Float = 30f
    var autoUpdate: Boolean = true

    // for testing
    internal var timeSource: () -> Long = { System.nanoTime() }
}

// TODO[next]: move throttling here
fun Terminal.progressAnimation(init: ProgressAnimationBuilder.() -> Unit): ProgressAnimation {
    val builder = ProgressAnimationBuilder().apply(init)

    return ProgressAnimation(this,
        layout = builder.build(),
        frameRate = builder.frameRate,
        historyLength = builder.historyLength,
        ticker = getTicker(builder.frameRate),
        timeSource = builder.timeSource
    )
}
