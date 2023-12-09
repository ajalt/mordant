package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.CachedProgressBarWidgetFactory
import com.github.ajalt.mordant.widgets.ProgressState
import com.github.ajalt.mordant.widgets.TaskId
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.TimeSource

class BaseProgressBarAnimation<T>(
    // TODO: param docs
    private val terminal: Terminal,
    private val factory: CachedProgressBarWidgetFactory<T>,
    private val timeSource: TimeSource.WithComparableMarks,
    private val speedEstimateDuration: Duration,
) : ProgressBarAnimation<T> {
    private val state = MppAtomicRef(ProgressAnimationState<T>(false, emptyList()))
    private val animationTime = timeSource.markNow()
    private val animation = terminal.animation<List<ProgressState<T>>> { factory.build(it) }

    override fun addTask(
        context: T,
        total: Long?,
        completed: Long,
        start: Boolean,
        visible: Boolean,
    ): ProgressTask<T> {
        val ts = TaskState(
            context, total, completed, visible,
            startedTime = if (start) timeSource.markNow() else null,
        )
        val task = ProgressTaskImpl(ts, animationTime, timeSource, speedEstimateDuration)
        check(state.update { copy(tasks = tasks + task) } != null) { "Failed to add task" }
        return task
    }

    override fun removeTask(task: ProgressTask<T>): Boolean {
        val result = state.update { copy(tasks = tasks.filter { it.id != task.id }) }
        return result != null && result.first.tasks.any { it.id == task.id }
    }

    override fun refresh() {
        // If we can't update the state, skip the animation refresh
        val result = state.update { copy(started = true) } ?: return
        val (_, newState) = result
        animation.update(newState.tasks.map { it.makeState() })
    }

    override fun clear() {
        animation.clear()
        factory.invalidateCache()
    }

    override val finished: Boolean
        get() = state.value.tasks.all { it.finished }
}

private class HistoryEntry(val time: ComparableTimeMark, val completed: Long)


private data class ProgressAnimationState<T>(
    val started: Boolean,
    val tasks: List<ProgressTaskImpl<T>>,
)

private data class TaskState<T>(
    val context: T,
    val total: Long?,
    val completed: Long,
    val visible: Boolean,
    val startedTime: ComparableTimeMark?,
    // newest samples are at the end
    val samples: List<HistoryEntry> = emptyList(),
    val pausedTime: ComparableTimeMark? = null,
    val finishedTime: ComparableTimeMark? = null,
    val finishedSpeed: Double? = null,
)


private class UpdateScopeImpl<T>(
    override var context: T,
    override var completed: Long,
    override var total: Long?,
    override var visible: Boolean,
) : ProgressTaskUpdateScope<T>

private class ProgressTaskImpl<T>(
    state: TaskState<T>,
    private val animationTime: ComparableTimeMark,
    private val timeSource: TimeSource.WithComparableMarks,
    private val speedEstimateDuration: Duration,
) : ProgressTask<T> {
    override val id: TaskId = TaskId()
    private val state = MppAtomicRef(state)

    override fun update(block: ProgressTaskUpdateScope<T>.() -> Unit) {
        state.update {
            val scope = UpdateScopeImpl(context, completed, total, visible)
            scope.block()

            // Remove samples older than the speed estimate duration
            val oldestSampleTime = timeSource.markNow() - speedEstimateDuration
            val entry = HistoryEntry(timeSource.markNow(), scope.completed)
            val samples = samples.dropWhile { it.time < oldestSampleTime } + entry
            var finishedTime = finishedTime
            var finishedSpeed = finishedSpeed
            val total = scope.total
            if (finishedTime == null && total != null && scope.completed == total) {
                finishedTime = timeSource.markNow()
                finishedSpeed = estimateSpeed(this)
            }

            copy(
                samples = samples,
                context = scope.context,
                completed = scope.completed,
                total = total,
                visible = scope.visible,
                finishedTime = finishedTime,
                finishedSpeed = finishedSpeed,
            )
        }
    }

    override fun start() {
        state.update {
            copy(startedTime = startedTime ?: timeSource.markNow())
        }
    }

    override fun pause() {
        state.update {
            copy(pausedTime = pausedTime ?: timeSource.markNow())
        }
    }

    override fun reset(
        start: Boolean,
        block: ProgressTaskUpdateScope<T>.() -> Unit,
    ) {
        state.update {
            val s = state.value
            val scope = UpdateScopeImpl(s.context, s.completed, s.total, s.visible)
            scope.block()
            s.copy(
                samples = emptyList(),
                context = scope.context,
                completed = scope.completed,
                total = scope.total,
                visible = scope.visible,
                startedTime = if (start) timeSource.markNow() else null,
                pausedTime = null,
                finishedTime = null,
                finishedSpeed = null,
            )
        }
    }

    override fun makeState(): ProgressState<T> {
        return state.value.run {
            ProgressState(
                context = context,
                total = total,
                completed = completed,
                animationTime = animationTime,
                startedTime = startedTime,
                pausedTime = pausedTime,
                finishedTime = finishedTime,
                speed = finishedSpeed ?: estimateSpeed(this),
                taskId = id,
            )
        }
    }

    override val finished: Boolean get() = state.value.finishedTime != null
    override val started: Boolean get() = state.value.startedTime != null
    override val paused: Boolean get() = state.value.pausedTime != null

}

private fun <T> estimateSpeed(state: TaskState<T>): Double? = state.run {
    if (startedTime == null || samples.size < 2) return null
    val sampleTimespan = samples.first().time.elapsedNow().toDouble(SECONDS)
    val complete = samples.last().completed - samples.first().completed
    if (complete <= 0 || sampleTimespan <= 0.0) null else complete / sampleTimespan
}
