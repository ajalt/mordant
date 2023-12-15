package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.BaseProgressBarAnimation
import com.github.ajalt.mordant.animation.ProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


class CoroutinesProgressBarAnimation<T> private constructor(
    private val terminal: Terminal,
    private val base: ProgressBarAnimation<T>,
    private val rate: Long,
) : ProgressBarAnimation<T> by base {
    constructor(
        terminal: Terminal,
        factory: CachedProgressBarWidgetMaker<T>,
        timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
        speedEstimateDuration: Duration = 30.seconds,
    ) : this(
        terminal,
        BaseProgressBarAnimation(terminal, factory, timeSource, speedEstimateDuration),
        (1.0 / factory.refreshRate).seconds.inWholeMilliseconds
    )

    /**
     * Start the animation and refresh it until all its tasks are finished.
     */
    suspend fun run() {
        terminal.cursor.hide(showOnExit = true)
        try {
            while (!base.finished) {
                base.refresh()
                delay(rate)
            }
        } finally {
            terminal.cursor.show()
        }
    }
}

/**
 * Create a progress bar animation that runs in a coroutine.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateInCoroutine(terminal)
 * val task = animation.addTask()
 * launch { animation.run() }
 * task.update { ... }
 * ```
 */
fun <T> ProgressBarDefinition<T>.animateInCoroutine(
    terminal: Terminal,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): CoroutinesProgressBarAnimation<T> {
    return CoroutinesProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        timeSource,
        speedEstimateDuration,
    )
}

