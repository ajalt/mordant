package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.CachedProgressBarWidgetFactory
import com.github.ajalt.mordant.widgets.ProgressState
import com.github.ajalt.mordant.widgets.TaskId
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.TimeSource

abstract class AbstractProgressBarAnimation<T>(
    private val t: Terminal,
    private val factory: CachedProgressBarWidgetFactory<T>,
    private val timeSource: TimeSource.WithComparableMarks,
    private val speedEstimateDuration: Duration,
) : ProgressBarAnimation<T> {
    // TODO: maybe pass this in instead of being abstract so task doesn't need a reference to animation
    abstract fun <T> withLock(block: () -> T): T

    private val tasks = mutableListOf<ProgressTask<T>>()
    private var started = false
    private val animationTime = timeSource.markNow()
    private val animation = t.animation<List<ProgressState<T>>> { factory.build(it) }

    override fun addTask(
        context: T,
        total: Long?,
        completed: Long,
        start: Boolean,
        visible: Boolean,
    ): ProgressTask<T> = withLock {
        val task = ProgressTaskImpl(
            this,
            context,
            completed,
            total,
            visible,
            animationTime,
            timeSource,
            speedEstimateDuration
        )
        tasks.add(task)
        if (start) task.start()
        task
    }

    override fun removeTask(task: ProgressTask<T>): Boolean = withLock {
        tasks.remove(task)
    }

    override fun refresh() = withLock {
        if (!started) {
            t.cursor.hide(showOnExit = true)
            started = true
        }
        animation.update(tasks.map { it.makeState() })

    }

    override fun clear() = withLock {
        animation.clear()
        factory.invalidateCache()
    }

    override val finished: Boolean
        get() = withLock { tasks.all { it.finished } }
}

private class ProgressTaskImpl<T>(
    private val animation: AbstractProgressBarAnimation<T>,
    override var context: T,
    override var completed: Long,
    override var total: Long?,
    override var visible: Boolean,
    private val animationTime: ComparableTimeMark,
    private val timeSource: TimeSource.WithComparableMarks,
    private val speedEstimateDuration: Duration,
) : ProgressTask<T>, ProgressTaskUpdateScope<T> {
    private class ProgressHistoryEntry(val time: ComparableTimeMark, val completed: Long)

    private var startedTime: ComparableTimeMark? = null
    private var pausedTime: ComparableTimeMark? = null
    private var finishedTime: ComparableTimeMark? = null
    private var finishedSpeed: Double? = null

    // newest samples are at the end
    private val samples = ArrayDeque<ProgressHistoryEntry>()

    override fun update(block: ProgressTaskUpdateScope<T>.() -> Unit) = animation.withLock {
        block()

        // Remove samples older than the speed estimate duration
        val oldestSampleTime = timeSource.markNow() - speedEstimateDuration
        while (samples.firstOrNull()?.let { it.time < oldestSampleTime } == true) {
            samples.removeFirst()
        }

        samples.addLast(ProgressHistoryEntry(timeSource.markNow(), completed))

        if (finishedTime == null && total?.let { completed >= it } == true) {
            finishedTime = timeSource.markNow()
            finishedSpeed = estimateSpeed()
        }
    }

    override fun start(): Unit = animation.withLock {
        if (startedTime == null) startedTime = timeSource.markNow()
    }

    override fun pause(): Unit = animation.withLock {
        if (pausedTime == null) pausedTime = timeSource.markNow()
    }

    override fun reset(
        start: Boolean,
        block: ProgressTaskUpdateScope<T>.() -> Unit,
    ): Unit = animation.withLock {
        startedTime = if (start) timeSource.markNow() else null
        pausedTime = null
        finishedTime = null
        finishedSpeed = null
        samples.clear()
        block()
    }

    override fun makeState() = ProgressState(
        context,
        total,
        completed,
        animationTime,
        startedTime,
        pausedTime,
        finishedTime,
        finishedSpeed ?: estimateSpeed(),
    )

    override val id: TaskId = object : TaskId {}

    override val finished: Boolean
        get() = animation.withLock { finishedTime != null }

    private fun estimateSpeed(): Double? = animation.withLock {
        if (startedTime == null || samples.size < 2) return@withLock null
        val sampleTimespan = (samples.last().time - samples.first().time).toDouble(SECONDS)
        val complete = samples.last().completed - samples.first().completed
        if (complete <= 0 || sampleTimespan <= 0.0) null else complete / sampleTimespan
    }
}
