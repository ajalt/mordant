package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.ProgressBuilder
import com.github.ajalt.mordant.widgets.ProgressCell
import com.github.ajalt.mordant.widgets.ProgressCell.AnimationRate
import com.github.ajalt.mordant.widgets.ProgressLayout
import com.github.ajalt.mordant.widgets.ProgressState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource


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
    internal var timeSource: TimeSource = TimeSource.Monotonic
}

private class ProgressHistory(
    private val windowLength: Duration,
    private val timeSource: TimeSource,
) {
    private var startTime: TimeMark? = null
    private val samples = ArrayDeque<ProgressHistoryEntry>()

    val started: Boolean get() = startTime != null
    val completed: Long get() = samples.lastOrNull()?.completed ?: 0

    private val elapsed: Duration
        get() = startTime?.elapsedNow() ?: Duration.ZERO

    fun start() {
        if (!started) {
            startTime = timeSource.markNow()
        }
    }

    fun clear() {
        startTime = null
        samples.clear()
    }

    fun update(completed: Long) {
        start()
        val now = elapsed
        val keepTime = now - windowLength
        while (samples.firstOrNull().let { it != null && it.elapsed < keepTime }) {
            samples.removeFirst()
        }
        samples.addLast(ProgressHistoryEntry(now, completed))
    }

    fun makeState(total: Long?) = ProgressState(
        completed = completed,
        total = total,
        completedPerSecond = completedPerSecond,
        elapsed = elapsed,
    )

    private val completedPerSecond: Double
        get() {
            if (elapsed < Duration.ZERO || samples.size < 2) return 0.0
            val sampleTimespan = samples.last().elapsed - samples.first().elapsed
            val complete = samples.last().completed - samples.first().completed
            return if (complete <= 0 || sampleTimespan <= Duration.ZERO) {
                0.0
            } else {
                complete / sampleTimespan.toDouble(DurationUnit.SECONDS)
            }
        }

    private data class ProgressHistoryEntry(
        val elapsed: Duration,
        val completed: Long,
    )
}

/**
 * A pretty animated progress bar. Manages a timer thread to update the progress bar, so be sure to [stop] it when you're done.
 */
class ProgressAnimation internal constructor(
    private val t: Terminal,
    private val layout: ProgressLayout,
    historyLength: Duration,
    private val ticker: Ticker,
    timeSource: TimeSource,
) {
    private var total: Long? = null
    private var tickerStarted: Boolean = false
    private val history = ProgressHistory(historyLength, timeSource)
    private val animation = t.animation<Unit> {
        val state = history.makeState(total)
        layout.build(
            state.completed,
            state.total,
            state.elapsed.toDouble(DurationUnit.SECONDS),
            state.completedPerSecond
        )
    }

    /**
     * Set the current progress to the [completed] value.
     */
    fun update(completed: Long) {
        history.update(completed)
        if (!tickerStarted) {
            animation.update(Unit)
        }
    }

    /**
     * Set the current progress to the [completed] value.
     */
    fun update(completed: Int) {
        update(completed.toLong())
    }

    /**
     * Update the progress bar without changing the current progress amount.
     *
     * This will redraw the animation and update fields like the estimated time remaining.
     */
    fun update() {
        update(history.completed)
    }

    /**
     * Set the current progress to the [completed] value, and set the total to the [total] value.
     */
    fun update(completed: Long, total: Long?) {
        updateTotalWithoutAnimation(total)
        update(completed)
    }

    /**
     * Set the [total] amount of work to be done, or `null` to make the progress bar indeterminate.
     */
    fun updateTotal(total: Long?) {
        updateTotalWithoutAnimation(total)
        update()
    }

    private fun updateTotalWithoutAnimation(total: Long?) {
        this.total = total?.takeIf { it > 0 }
    }

    /**
     * Advance the current completed progress by [amount] without changing the total.
     */
    fun advance(amount: Long = 1) {
        update(history.completed + amount)
    }

    /**
     * Start the progress bar animation.
     */
    suspend fun start() {
        if (tickerStarted) return
        t.cursor.hide(showOnExit = true)
        tickerStarted = true
        history.start()
        ticker.start(::tick)
    }

    private fun tick() {
        // Running on the timer thread.
        if (!tickerStarted)
            return    // This can happen if we're racing with stop().
        update()
        animation.update(Unit)
    }

    /**
     * Stop the progress bar animation.
     *
     * The progress bar will remain on the screen until you call [clear].
     * You can call [start] again to resume the animation.
     */
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
    suspend fun restart() {
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
    fun clear() {
        stop()
        history.clear()
        animation.clear()
    }
}

private class CachedProgressCell(private val cell: ProgressCell, maxFramesPerSecond: Int?) : ProgressCell {
    override val columnWidth: ColumnWidth get() = cell.columnWidth
    override val animationRate: AnimationRate get() = cell.animationRate
    /** Maximum expected duration per frame */
    private val maxFrameDuration = maxFramesPerSecond?.let { 1.seconds / it }

    private var widget: Widget? = null
    private var lastFrame = Duration.ZERO

    fun clear() {
        widget = null
        lastFrame = Duration.ZERO
    }

    private fun shouldSkipUpdate(elapsed: Duration): Boolean {
        if (maxFrameDuration == null) return false
        if ((elapsed - lastFrame) < maxFrameDuration) return true
        lastFrame = elapsed
        return false
    }

    override fun ProgressState.makeWidget(): Widget {
        var r = widget
        val shouldSkipUpdate = shouldSkipUpdate(elapsed)
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
        historyLength = builder.historyLength.toDouble().seconds,
        ticker = getTicker(builder.animationFrameRate),
        timeSource = builder.timeSource
    )
}
