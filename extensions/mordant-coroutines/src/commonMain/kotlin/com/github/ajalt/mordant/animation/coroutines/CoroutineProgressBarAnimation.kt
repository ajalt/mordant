package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.progress.BaseProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


class CoroutineProgressBarAnimation<T> private constructor(
    private val terminal: Terminal,
    private val base: ProgressBarAnimation<T>,
    private val rate: Long,
) : ProgressBarAnimation<T> by base {
    constructor(
        terminal: Terminal,
        factory: CachedProgressBarWidgetMaker<T>,
        speedEstimateDuration: Duration = 30.seconds,
    ) : this(
        terminal,
        BaseProgressBarAnimation(terminal, factory, speedEstimateDuration),
        factory.refreshPeriod.inWholeMilliseconds
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
            base.refresh(refreshAll = true) // final refresh to show finished state
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
): CoroutineProgressBarAnimation<T> {
    return CoroutineProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        speedEstimateDuration,
    )
}

