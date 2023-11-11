package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.nanosToSeconds
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.ProgressBuilder
import com.github.ajalt.mordant.widgets.ProgressCell
import com.github.ajalt.mordant.widgets.ProgressCell.AnimationRate
import com.github.ajalt.mordant.widgets.ProgressLayout
import com.github.ajalt.mordant.widgets.ProgressState
import java.util.concurrent.TimeUnit


class ProgressAnimationBuilder internal constructor() : ProgressBuilder() {
    /**
     * The maximum number of times per second to update idle animations like the progress bar pulse
     * (default: 30fps)
     */
    var animationFrameRate: Int = 30

    /**
     * The maximum number of times per second to update text like the estimated time remaining.
     * (default: 5fps)
     */
    var textFrameRate: Int = 5

    /**
     * The number of seconds worth of history to keep track of for estimating progress speed and
     * time remaining (default: 30s)
     */
    var historyLength: Float = 30f

    // for testing
    internal var timeSource: () -> Long = { System.nanoTime() }
}


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
    val completed: Long get() = samples.lastOrNull()?.completed ?: 0

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

/**
 * A pretty animated progress bar. Manages a timer thread to update the progress bar, so be sure to [stop] it when you're done.
 */
class ProgressAnimation internal constructor(
    private val t: Terminal,
    private val layout: ProgressLayout,
    historyLength: Float,
    private val ticker: Ticker,
    timeSource: () -> Long,
) {
    private var total: Long? = null
    private var tickerStarted: Boolean = false
    private val history = ProgressHistory(historyLength, timeSource)
    private val animation = t.animation<Unit> {
        val state = history.makeState(total)
        layout.build(state.completed, state.total, state.elapsedSeconds, state.completedPerSecond)
    }

    // Locking: all state is protected by this object's monitor. Tick is run on the timer thread.

    /**
     * Set the current progress to the [completed] value.
     */
    @Synchronized
    fun update(completed: Long) {
        history.update(completed)
        if (!tickerStarted) {
            animation.update(Unit)
        }
    }

    /**
     * Set the current progress to the [completed] value.
     */
    @Synchronized
    fun update(completed: Int) {
        update(completed.toLong())
    }

    /**
     * Update the progress bar without changing the current progress amount.
     *
     * This will redraw the animation and update fields like the estimated time remaining.
     */
    @Synchronized
    fun update() {
        update(history.completed)
    }

    /**
     * Set the current progress to the [completed] value, and set the total to the [total] value.
     */
    @Synchronized
    fun update(completed: Long, total: Long?) {
        updateTotalWithoutAnimation(total)
        update(completed)
    }

    /**
     * Set the [total] amount of work to be done, or `null` to make the progress bar indeterminate.
     */
    @Synchronized
    fun updateTotal(total: Long?) {
        updateTotalWithoutAnimation(total)
        update()
    }

    @Synchronized
    private fun updateTotalWithoutAnimation(total: Long?) {
        this.total = total?.takeIf { it > 0 }
    }

    /**
     * Advance the current completed progress by [amount] without changing the total.
     */
    @Synchronized
    fun advance(amount: Long = 1) {
        update(history.completed + amount)
    }

    /**
     * Start the progress bar animation.
     */
    @Synchronized
    fun start() {
        if (tickerStarted) return
        t.cursor.hide(showOnExit = true)
        tickerStarted = true
        history.start()
        ticker.start {
            tick()
        }
    }

    @Synchronized
    private fun tick() {
        // Running on the timer thread.
        if (!tickerStarted)
            return   // This can happen if we're racing with stop().
        update()
        animation.update(Unit)
    }

    /**
     * Stop the progress bar animation.
     *
     * The progress bar will remain on the screen until you call [clear].
     * You can call [start] again to resume the animation.
     */
    @Synchronized
    fun stop() {
        if (!tickerStarted) return
        tickerStarted = false
        try {
            ticker.stop()
            animation.stop()
        } finally {
            t.cursor.show()
        }
    }

    /**
     * Set the progress to 0 and restart the animation.
     */
    @Synchronized
    fun restart() {
        val tickerStarted = tickerStarted
        stop()
        layout.cells.forEach { (it as? CachedProgressCell)?.clear() }
        update(0)
        if (tickerStarted) start()
    }

    /**
     * Stop the animation and remove it from the screen.
     *
     * If you want to leave the animation on the screen, call [stop] instead.
     */
    @Synchronized
    fun clear() {
        stop()
        history.clear()
        animation.clear()
    }
}

private class CachedProgressCell(private val cell: ProgressCell, frameRate: Int?) : ProgressCell {
    override val columnWidth: ColumnWidth get() = cell.columnWidth
    override val animationRate: AnimationRate get() = cell.animationRate
    private val frameDuration = frameRate?.let { 1.0 / it }

    private var widget: Widget? = null
    private var lastFrameTime = 0.0

    fun clear() {
        widget = null
        lastFrameTime = 0.0
    }

    private fun shouldSkipUpdate(elapsed: Double): Boolean {
        if (frameDuration == null) return false
        if ((elapsed - lastFrameTime) < frameDuration) return true
        lastFrameTime = elapsed
        return false
    }

    override fun ProgressState.makeWidget(): Widget {
        var r = widget
        val shouldSkipUpdate = shouldSkipUpdate(elapsedSeconds)
        if (r != null && shouldSkipUpdate) return r
        r = cell.run { makeWidget() }
        widget = r
        return r
    }
}

/**
 * Create an animated progress bar.
 *
 * See [ProgressLayout] for the types of cells that can be added.
 */
fun Terminal.progressAnimation(init: ProgressAnimationBuilder.() -> Unit): ProgressAnimation {
    val builder = ProgressAnimationBuilder().apply(init)

    val layout = ProgressLayout(builder.cells.map {
        val fr = when (it.animationRate) {
            AnimationRate.STATIC -> null
            AnimationRate.ANIMATION -> builder.animationFrameRate
            AnimationRate.TEXT -> builder.textFrameRate
        }
        CachedProgressCell(it, fr)
    }, builder.padding)

    return ProgressAnimation(
        t = this,
        layout = layout,
        historyLength = builder.historyLength,
        ticker = getTicker(builder.animationFrameRate),
        timeSource = builder.timeSource
    )
}
