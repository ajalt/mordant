package com.github.ajalt.mordant.widgets.progress

import kotlin.time.ComparableTimeMark

// TODO: docs
// TODO: make total and completed `Double`?
// TODO: test paused/started time in animation
// TODO: move eta into here
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
    /** The time that the progress task was started, or `null` if it hasn't been started. */
    val startedTime: ComparableTimeMark? = null,
    /** The time that the progress task was paused, or `null` if it isn't paused. */
    val pausedTime: ComparableTimeMark? = null,
    /** The time that the progress task was finished, or `null` if it isn't finished. */
    val finishedTime: ComparableTimeMark? = null,
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
    val isIndeterminate: Boolean get() = total == null
    val isPaused: Boolean get() = pausedTime != null
    val isStarted: Boolean get() = startedTime != null
    val isFinished: Boolean get() = finishedTime != null
}

/**
 * Create a [ProgressState] with no context.
 */
fun ProgressState(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
): ProgressState<Unit> {
    return ProgressState(
        Unit, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
    )
}
