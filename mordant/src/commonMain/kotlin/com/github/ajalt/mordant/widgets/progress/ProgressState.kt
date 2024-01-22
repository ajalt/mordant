package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.widgets.progress.ProgressState.Status
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

// TODO: docs
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
    get() = when (this) {
        is Status.NotStarted -> null
        is Status.Running -> startTime
        is Status.Paused -> startTime
        is Status.Finished -> startTime
    }

/**
 * The time that this task was paused, or `null` if it isn't paused.
 */
val Status.pauseTime: ComparableTimeMark?
    get() = when (this) {
        is Status.Paused -> pauseTime
        else -> null
    }

/**
 * The time that this task finished, or `null` if it isn't finished.
 */
val Status.finishTime: ComparableTimeMark?
    get() = when (this) {
        is Status.Finished -> finishTime
        else -> null
    }

/**
 * Calculate the estimated time remaining for this task, or `null` if the time cannot be estimated.
 *
 * @param elapsedWhenFinished If `true`, return the total elapsed time when the task is finished.
 */
fun ProgressState<*>.calculateTimeRemaining(elapsedWhenFinished: Boolean = true): Duration? {
    return when {
        status is Status.Finished && elapsedWhenFinished -> {
            status.finishTime - status.startTime
        }

        status is Status.Running && speed != null && speed > 0 && total != null -> {
            ((total - completed) / speed).seconds
        }

        else -> null
    }
}

/**
 * Calculate the time elapsed for this task, or `null` if the task hasn't started.
 *
 * If the task is finished or paused, the elapsed time is the time between the start and
 * finish/pause times. If the task is running, the elapsed time is the time between the start and
 * now.
 */
fun ProgressState<*>.calculateTimeElapsed(): Duration? {
    return when (status) {
        Status.NotStarted -> null
        is Status.Finished -> status.finishTime - status.startTime
        is Status.Paused -> status.pauseTime - status.startTime
        is Status.Running -> status.startTime.elapsedNow()
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
