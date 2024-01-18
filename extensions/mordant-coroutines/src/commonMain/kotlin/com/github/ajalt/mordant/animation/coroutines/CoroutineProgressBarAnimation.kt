package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.progress.BaseProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


class CoroutineProgressBarAnimation<T> private constructor(
    private val terminal: Terminal,
    private val base: BaseProgressBarAnimation<T>,
    private val rate: Long,
) : ProgressBarAnimation<T> by base {
    constructor(
        terminal: Terminal,
        factory: CachedProgressBarWidgetMaker<T>,
        clearWhenFinished: Boolean = false,
        speedEstimateDuration: Duration = 30.seconds,
    ) : this(
        terminal,
        BaseProgressBarAnimation(terminal, factory, clearWhenFinished, speedEstimateDuration),
        factory.refreshPeriod.inWholeMilliseconds
    )

    private var stopped = false
    private val mutex = Mutex()

    /**
     * Start the animation and refresh it until all its tasks are finished.
     */
    suspend fun run() {
        mutex.withLock {
            stopped = false
            terminal.cursor.hide(showOnExit = true)
        }
        try {
            while (mutex.withLock { !stopped && !base.finished}) {
                mutex.withLock { base.refresh() }
                delay(rate)
            }
            mutex.withLock {
                // final refresh to show finished state
                if (!stopped) base.refresh(refreshAll = true)
            }
        } finally {
            mutex.withLock { terminal.cursor.show() }
        }
    }

    /**
     * Stop the animation, but leave it on the screen.
     */
    suspend fun stop(): Unit = mutex.withLock {
        stopped = true
        base.stop()
    }

    /**
     * Stop the animation and remove it from the screen.
     */
    suspend fun clear(): Unit = mutex.withLock {
        stopped = true
        base.clear()
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
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): CoroutineProgressBarAnimation<T> {
    return CoroutineProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        clearWhenFinished,
        speedEstimateDuration,
    )
}

