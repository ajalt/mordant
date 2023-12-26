package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import com.github.ajalt.mordant.widgets.progress.ProgressState.Status
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.TimeSource

class BaseProgressBarAnimation<T>(
    // TODO: param docs
    terminal: Terminal,
    private val maker: CachedProgressBarWidgetMaker<T>,
    private val speedEstimateDuration: Duration = 30.seconds,
) : ProgressBarAnimation<T> {
    private data class State<T>(val visible: Boolean, val tasks: List<ProgressTaskImpl<T>>)

    private val state = MppAtomicRef(State(true, listOf<ProgressTaskImpl<T>>()))
    private val timeSource get() = maker.timeSource
    private val animationTime = timeSource.markNow()
    private val animation = terminal.animation<List<ProgressState<T>>> { maker.build(it) }

    override fun addTask(
        context: T,
        total: Long?,
        completed: Long,
        start: Boolean,
        visible: Boolean,
    ): ProgressTask<T> {
        val task = ProgressTaskImpl(
            context = context,
            total = total,
            completed = completed,
            visible = visible,
            now = timeSource.markNow(),
            start = start,
            maker = maker,
            animationTime = animationTime,
            timeSource = timeSource,
            speedEstimateDuration = speedEstimateDuration
        )
        state.update { copy(tasks = tasks + task) }
        return task
    }

    override fun removeTask(task: ProgressTask<T>): Boolean {
        val (old, _) = state.update { copy(tasks = tasks.filter { it.id != task.id }) }
        return old.tasks.any { it.id == task.id }
    }

    override fun refresh() {
        val s = state.value
        if (!s.visible) return
        animation.update(s.tasks.filter { it.visible }.map { it.makeState() })
    }

    override var visible: Boolean
        get() = state.value.visible
        set(value) {
            state.update { copy(visible = value) }
            if (!value) animation.clear()
            maker.invalidateCache()
        }

    override val finished: Boolean
        get() = state.value.tasks.all { it.finished }
}

private class HistoryEntry(val time: ComparableTimeMark, val completed: Long)

private data class TaskState<T>(
    val context: T,
    val total: Long?,
    val completed: Long,
    val visible: Boolean,
    val samples: List<HistoryEntry>, // newest samples are at the end
    val status: Status,
    val finishedSpeed: Double?,
) {
    constructor(
        context: T,
        total: Long?,
        completed: Long,
        visible: Boolean,
        now: ComparableTimeMark,
        start: Boolean,
    ) : this(
        context = context,
        total = total,
        completed = completed,
        visible = visible,
        samples = if (start) listOf(HistoryEntry(now, completed)) else emptyList(),
        status = if (start) Status.Running(now) else Status.NotStarted,
        finishedSpeed = null,
    )
}

private class UpdateScopeImpl<T>(
    override var context: T,
    override var completed: Long,
    override var total: Long?,
    override var visible: Boolean,
    override var started: Boolean,
    override var paused: Boolean,
) : ProgressTaskUpdateScope<T>

private class ProgressTaskImpl<T>(
    context: T,
    total: Long?,
    completed: Long,
    visible: Boolean,
    now: ComparableTimeMark,
    start: Boolean,
    private val maker: CachedProgressBarWidgetMaker<T>,
    private val animationTime: ComparableTimeMark,
    private val timeSource: TimeSource.WithComparableMarks,
    private val speedEstimateDuration: Duration,
) : ProgressTask<T> {
    override val id: TaskId = TaskId()
    private val state = MppAtomicRef(TaskState(context, total, completed, visible, now, start))

    override fun update(block: ProgressTaskUpdateScope<T>.() -> Unit) {
        state.update {
            val scope = UpdateScopeImpl(
                context = context,
                completed = completed,
                total = total,
                visible = visible,
                started = status !is Status.NotStarted,
                paused = status is Status.Paused
            )
            scope.block()

            // Remove samples older than the speed estimate duration
            val oldestSampleTime = timeSource.markNow() - speedEstimateDuration
            val entry = HistoryEntry(timeSource.markNow(), scope.completed)
            val samples = samples.dropWhile { it.time < oldestSampleTime } + entry
            val total = scope.total

            val startTime = status.pauseTime ?: timeSource.markNow()
            val finishTime = status.finishTime ?: timeSource.markNow()
            val pauseTime = status.pauseTime ?: timeSource.markNow()

            val status = when {
                total != null && scope.completed >= total -> Status.Finished(startTime, finishTime)
                scope.started && scope.paused -> Status.Paused(startTime, pauseTime)
                scope.started -> Status.Running(startTime)
                else -> Status.NotStarted
            }

            val finishedSpeed = when (status) {
                is Status.Finished -> finishedSpeed ?: estimateSpeed(startTime, samples)
                else -> null
            }

            copy(
                samples = samples,
                context = scope.context,
                completed = scope.completed,
                total = total,
                visible = scope.visible,
                status = status,
                finishedSpeed = finishedSpeed,
            )
        }
    }

    override fun reset(
        start: Boolean,
        block: ProgressTaskUpdateScope<T>.() -> Unit,
    ) {
        state.update {
            val s = state.value
            val scope = UpdateScopeImpl(s.context, 0, s.total, s.visible, start, false)
            scope.block()
            TaskState(
                context = scope.context,
                total = scope.total,
                completed = scope.completed,
                visible = scope.visible,
                now = timeSource.markNow(),
                start = scope.started,
            )
        }
        maker.invalidateCache(id)
    }

    override fun makeState(): ProgressState<T> {
        return state.value.run {
            ProgressState(
                context = context,
                total = total,
                completed = completed,
                animationTime = animationTime,
                status = status,
                speed = finishedSpeed ?: estimateSpeed(status.startTime, samples),
                taskId = id,
            )
        }
    }

    override val finished: Boolean get() = state.value.status is Status.Finished
    override val started: Boolean get() = state.value.status !is Status.NotStarted
    override val paused: Boolean get() = state.value.status is Status.Paused
    override val visible: Boolean get() = state.value.visible
}

private fun estimateSpeed(
    startedTime: ComparableTimeMark?,
    samples: List<HistoryEntry>,
): Double? {
    if (startedTime == null || samples.size < 2) return null
    val sampleTimespan = samples.first().time.elapsedNow().toDouble(SECONDS)
    val complete = samples.last().completed - samples.first().completed
    return if (complete <= 0 || sampleTimespan <= 0.0) null else complete / sampleTimespan
}
