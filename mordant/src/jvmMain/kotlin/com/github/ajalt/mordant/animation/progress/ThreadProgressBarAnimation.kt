package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import java.util.concurrent.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class BlockingProgressBarAnimation<T> private constructor(
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
        factory.refreshPeriod.inWholeMilliseconds
    )

    /**
     * Start the animation and refresh it until all its tasks are finished.
     *
     * This calls [Thread.sleep] between each frame, so it should usually be run in a separate
     * thread so that you can update the state concurrently.
     *
     * @see execute
     */
    fun runBlocking() {
        terminal.cursor.hide(showOnExit = true)
        try {
            while (!base.finished) {
                base.refresh()
                Thread.sleep(rate)
            }
        } finally {
            terminal.cursor.show()
        }
    }
}

/**
 * Create a progress bar animation that runs synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateOnThread(terminal)
 * val task = animation.addTask()
 * animation.execute()
 * task.update { ... }
 * ```
 */
fun <T> ProgressBarDefinition<T>.animateOnThread(
    terminal: Terminal,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): BlockingProgressBarAnimation<T> {
    return BlockingProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        timeSource,
        speedEstimateDuration,
    )
}

/**
 * Run the animation in a background thread on an [executor].
 *
 * @return a [Future] that can be used to cancel the animation.
 */
fun <T> BlockingProgressBarAnimation<T>.execute(
    executor: ExecutorService = Executors.newSingleThreadExecutor(DaemonThreadFactory()),
): Future<*> {
    return executor.submit(::runBlocking)
}

private class DaemonThreadFactory: ThreadFactory {
    override fun newThread(r: Runnable): Thread = Thread(r).also {
        it.isDaemon = true
    }
}
