package com.github.ajalt.mordant.animation.jvm

import com.github.ajalt.mordant.animation.AbstractProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.CachedProgressBarWidgetFactory
import com.github.ajalt.mordant.widgets.ProgressBarWidgetFactory
import com.github.ajalt.mordant.widgets.cache
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class ExecutorProgressBarAnimation<T>(
    terminal: Terminal,
    factory: CachedProgressBarWidgetFactory<T>,
    private val executor: ScheduledExecutorService = defaultExecutor(),
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
) : AbstractProgressBarAnimation<T>(terminal, factory, timeSource, speedEstimateDuration) {
    private val lock = Any()
    private var future: ScheduledFuture<*>? = null
    private val rate = (1.0 / factory.refreshRate).seconds.inWholeMilliseconds

    override fun <T> withLock(block: () -> T): T = synchronized(lock) { block() }

    fun start() = withLock {
        if (future.let { it == null || !it.isDone }) {
            execute()
        }
    }

    /**
     * Pause refreshing the progress bar.
     *
     * Call [start] again to resume the animation.
     *
     * Not that this does not pause any of the tasks, only the animation. Call [ProgressTask.pause]
     * if you want to pause a task's speed calculation.
     */
    fun pause() = withLock {
        future?.cancel(false)
        future = null
    }

    /**
     * Shutdown the executor and show the terminal cursor.
     */
    fun shutdown() = withLock {
        try {
            executor.shutdown()
        } finally {
            terminal.cursor.show()
        }
    }

    // TODO docs
    override fun clear() = withLock {
        pause()
        super.clear()
    }

    private fun execute() = withLock {
        refresh()
        if (finished) {
            shutdown()
        } else {
            future = executor.schedule(::execute, rate, TimeUnit.MILLISECONDS)
        }
    }
}

private fun defaultExecutor(): ScheduledExecutorService {
    return Executors.newSingleThreadScheduledExecutor {
        Thread().also {
            it.isDaemon = true
        }
    }
}

// TODO docs
fun <T> ProgressBarWidgetFactory<T>.animateOnExecutor(
    terminal: Terminal,
    executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
): ExecutorProgressBarAnimation<T> {
    return ExecutorProgressBarAnimation(
        terminal,
        cache(timeSource),
        executor,
        timeSource,
        speedEstimateDuration
    )
}
