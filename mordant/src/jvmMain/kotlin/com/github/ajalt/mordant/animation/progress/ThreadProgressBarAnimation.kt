package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class BlockingProgressBarAnimation<T> private constructor(
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
    private val lock = Any()

    /**
     * Start the animation and refresh it until all its tasks are finished.
     *
     * This calls [Thread.sleep] between each frame, so it should usually be run in a separate
     * thread so that you can update the state concurrently.
     *
     * @see execute
     */
    fun runBlocking() {
        synchronized(lock) {
            stopped = false
            terminal.cursor.hide(showOnExit = true)
        }
        try {
            while (synchronized(lock) { !stopped && !base.finished }) {
                synchronized(lock) { base.refresh() }
                Thread.sleep(rate)
            }
            synchronized(lock) {
                // final refresh to show finished state
                if (!stopped) base.refresh(refreshAll = true)
            }
        } finally {
            synchronized(lock) { terminal.cursor.show() }
        }
    }

    /**
     * Stop the animation, but leave it on the screen.
     */
    fun stop(): Unit = synchronized(lock) {
        stopped = true
        base.stop()
    }

    /**
     * Stop the animation and remove it from the screen.
     */
    fun clear(): Unit = synchronized(lock) {
        stopped = true
        base.clear()
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
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): BlockingProgressBarAnimation<T> {
    return BlockingProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        clearWhenFinished,
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

private class DaemonThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable): Thread = Thread(r).also {
        it.name = "${it.name}-mordant-progress"
        it.isDaemon = true
    }
}
