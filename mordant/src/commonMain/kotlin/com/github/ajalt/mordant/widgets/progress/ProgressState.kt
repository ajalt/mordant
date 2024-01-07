package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.widgets.progress.ProgressState.Status
import kotlin.time.ComparableTimeMark
import kotlin.time.DurationUnit

// TODO: docs
// TODO: make total and completed `Double`?
// TODO: sealed class for all the time combos?
data class ProgressState<T>(
    /** The context object passed to the progress task. */
    val context: T,
    /** The total number of steps needed to complete the progress task, or `null` if it is indeterminate. */
    val total: Long?,
    /** The number of steps currently completed in the progress task. */
    val completed: Long,
    /**
     * The time that the progress layout was first constructed.
     *
     * Use this for continuous animations, since it's the same for all tasks.
     */
    val animationTime: ComparableTimeMark,

    /** The running status of the task. */
    val status: Status,

    /**
     * The estimated speed of the progress task, in steps per second, or `null` if it hasn't started.
     *
     * If the task is finished or paused, this is the speed at the time it finished.
     */
    val speed: Double? = null,

    /**
     * The unique id of this state's task.
     */
    val taskId: TaskId = TaskId(),
) {

    sealed class Status {
        data object NotStarted : Status()
        data class Running(
            val startTime: ComparableTimeMark,
        ) : Status()

        data class Paused(
            val startTime: ComparableTimeMark,
            val pauseTime: ComparableTimeMark,
        ) : Status()

        data class Finished(
            val startTime: ComparableTimeMark,
            val finishTime: ComparableTimeMark,
        ) : Status()
    }
}

/** `true` if the task does not have a [total][ProgressState.total] specified. */
val <T> ProgressState<T>.isIndeterminate: Boolean get() = total == null

/** `true if the task's status is [Paused][Status.Paused]. */
val <T> ProgressState<T>.isPaused: Boolean get() = status is Status.Paused

/** `true` if the task's status is [Running][Status.Running]. */
val <T> ProgressState<T>.isRunning: Boolean get() = status is Status.Running

/** `true` if the task's status is [NotStarted][Status.NotStarted]. */
val <T> ProgressState<T>.isFinished: Boolean get() = status is Status.Finished

/**
 * The time that this task started, or `null` if it hasn't started.
 */
val Status.startTime: ComparableTimeMark?
    get() {
        return when (this) {
            is Status.NotStarted -> null
            is Status.Running -> startTime
            is Status.Paused -> startTime
            is Status.Finished -> startTime
        }
    }

/**
 * The time that this task was paused, or `null` if it isn't paused.
 */
val Status.pauseTime: ComparableTimeMark?
    get() {
        return when (this) {
            is Status.Paused -> pauseTime
            else -> null
        }
    }

/**
 * The time that this task finished, or `null` if it isn't finished.
 */
val Status.finishTime: ComparableTimeMark?
    get() {
        return when (this) {
            is Status.Finished -> finishTime
            else -> null
        }
    }

/**
 * Return the number of frames that have elapsed at the given [fps] since the start of the
 * [animationTime][ProgressState.animationTime].
 */
fun ProgressState<*>.frameCount(fps: Int): Int {
    return (animationTime.elapsedNow().toDouble(DurationUnit.SECONDS) * fps).toInt()
}

/**
 * Create a [ProgressState] with no context.
 */
fun ProgressState(
    total: Long?,
    completed: Long,
    animationTime: ComparableTimeMark,
    status: Status,
    speed: Double? = null,
): ProgressState<Unit> {
    return ProgressState(
        Unit, total, completed, animationTime, status, speed
    )
}
