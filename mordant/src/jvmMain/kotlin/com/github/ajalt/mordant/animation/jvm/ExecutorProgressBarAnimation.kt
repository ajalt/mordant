package com.github.ajalt.mordant.animation.jvm

import com.github.ajalt.mordant.animation.BaseProgressBarAnimation
import com.github.ajalt.mordant.animation.ProgressBarAnimation
import com.github.ajalt.mordant.animation.ProgressTask
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class ExecutorProgressBarAnimation<T> private constructor(
    private val terminal: Terminal,
    private val base: ProgressBarAnimation<T>,
    private val executor: ScheduledExecutorService,
    private val rate: Long,
) : AutoCloseable, ProgressBarAnimation<T> by base {
    constructor(
        terminal: Terminal,
        factory: CachedProgressBarWidgetMaker<T>,
        executor: ScheduledExecutorService = defaultExecutor(),
        timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
        speedEstimateDuration: Duration = 30.seconds,
    ) : this(
        terminal,
        BaseProgressBarAnimation(terminal, factory, timeSource, speedEstimateDuration),
        executor,
        (1.0 / factory.refreshRate).seconds.inWholeMilliseconds
    )

    private val lock = Any()
    private var future: ScheduledFuture<*>? = null

    fun start() = synchronized(lock) {
        if (future == null) {
            terminal.cursor.hide(showOnExit = true)
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
    fun pause() = synchronized(lock) {
        future?.cancel(false)
        future = null
    }

    /**
     * Shutdown the executor and show the terminal cursor.
     */
    override fun close() = synchronized(lock) {
        try {
            executor.shutdown()
        } finally {
            terminal.cursor.show()
        }
    }

    override fun clear() = synchronized(lock) {
        pause()
        base.clear()
    }

    private fun execute() = synchronized(lock) {
        refresh()
        if (finished) {
            close()
        } else {
            future = executor.schedule(::execute, rate, TimeUnit.MILLISECONDS)
        }
    }
}

// TODO docs
fun <T> ProgressBarDefinition<T>.animateOnExecutor(
    terminal: Terminal,
    executor: ScheduledExecutorService = defaultExecutor(),
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): ExecutorProgressBarAnimation<T> {
    return ExecutorProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        executor,
        timeSource,
        speedEstimateDuration,
    )
}

private fun defaultExecutor(): ScheduledExecutorService {
    return Executors.newSingleThreadScheduledExecutor {
        Thread().also {
            it.isDaemon = true
        }
    }
}
