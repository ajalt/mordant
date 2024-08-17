package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.Refreshable
import com.github.ajalt.mordant.widgets.progress.ProgressBarCell
import com.github.ajalt.mordant.widgets.progress.ProgressBarDefinition
import com.github.ajalt.mordant.widgets.progress.ProgressState
import com.github.ajalt.mordant.widgets.progress.TaskId

interface ProgressTaskUpdateScope<T> {
    /** The context is used to pass custom information to the task */
    var context: T

    /** The number of units of work that have been completed */
    var completed: Long

    /** The total number of units of work, or null if the total is unknown */
    var total: Long?

    /** Whether the task should be visible in the progress bar */
    var visible: Boolean

    /** Whether the task has been started */
    var started: Boolean

    /** Whether the task is currently paused */
    var paused: Boolean
}

interface ProgressTask<T> {
    /**
     * Update the task's state.
     *
     * If the completed count is equal to the total, the task will be marked as [finished].
     *
     * If the task is already finished, this method will still update the task's state, but it will
     * remain marked as finished. Use [reset] if you want to start the task again.
     *
     * Note that if you are calling this method concurrently, the [block] may be called more than
     * once, so it should not have any side effects.
     */
    fun update(block: ProgressTaskUpdateScope<T>.() -> Unit)

    /**
     * Reset the task so its completed count is 0 and its clock is reset.
     *
     * @param start If true, start the task after resetting it and running [block].
     * @param block A block to [update] the task's state after resetting it.
     */
    fun reset(
        start: Boolean = true,
        block: ProgressTaskUpdateScope<T>.() -> Unit = {},
    )

    /** Create a [ProgressState] for this task's current state */
    fun makeState(): ProgressState<T>

    /** `true` if this task's [completed] count is equal to its [total] */
    val finished: Boolean

    /** The context is used to pass custom information to the task */
    val context: T

    /** The number of units of work that have been completed */
    val completed: Long

    /** The total number of units of work, or null if the total is unknown */
    val total: Long?

    /** Whether the task should be visible in the progress bar */
    val visible: Boolean

    /** Whether the task has been started */
    val started: Boolean

    /** Whether the task is currently paused */
    val paused: Boolean

    /** The unique id of this task */
    val id: TaskId
}

/**
 * Advance the completed progress of this task by [amount].
 *
 * This is a shortcut for `update { completed += amount }`.
 */
fun ProgressTask<*>.advance(amount: Long = 1) = update { completed += amount }

/**
 * Advance the completed progress of this task by [amount].
 *
 * This is a shortcut for `update { completed += amount }`.
 */
fun ProgressTask<*>.advance(amount: Number) = advance(amount.toLong())

/**
 * Set the completed progress of this task to [completed].
 *
 * This is a shortcut for `update { this.completed += completed }`.
 */
fun ProgressTask<*>.update(completed: Long) = update { this.completed = completed }

/**
 * Set the completed progress of this task to [completed].
 *
 * This is a shortcut for `update { this.completed += completed }`.
 */
fun ProgressTask<*>.update(completed: Number) = update(completed.toLong())

// This isn't a RefreshableAnimation because the coroutine animator needs its methods to be
// suspending
/**
 * An animation that can draw one or more progress [tasks][addTask] to the screen.
 */
interface ProgressBarAnimation : Refreshable {
    /**
     * Add a new task to the progress bar with the given [definition] and [context].
     *
     * @param definition The definition of the progress bar to add
     * @param context The context to pass to the task
     * @param total The total number of steps needed to complete the progress task, or `null` if it is indeterminate.
     * @param completed The number of steps currently completed in the progress task.
     * @param start If `true`, start the task immediately.
     * @param visible If `false`, the task will not be drawn to the screen.
     */
    fun <T> addTask(
        definition: ProgressBarDefinition<T>,
        context: T,
        total: Long? = null,
        completed: Long = 0,
        start: Boolean = true,
        visible: Boolean = true,
    ): ProgressTask<T>

    /**
     * Remove a task with the given [taskId] from the progress bar.
     *
     * @return `true` if the task was removed, `false` if it was not found.
     */
    fun removeTask(taskId: TaskId): Boolean
}

/**
 * Remove a task from the progress bar.
 *
 * @return `true` if the task was removed, `false` if it was not found.
 */
fun ProgressBarAnimation.removeTask(task: ProgressTask<*>) = removeTask(task.id)

/**
 * Add a new task to the progress bar with the given [definition].
 */
fun ProgressBarAnimation.addTask(
    definition: ProgressBarDefinition<Unit>,
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
): ProgressTask<Unit> {
    return addTask(definition, Unit, total, completed, start, visible)
}

