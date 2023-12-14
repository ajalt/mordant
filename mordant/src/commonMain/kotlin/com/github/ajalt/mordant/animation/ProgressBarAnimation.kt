package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.widgets.progress.ProgressState
import com.github.ajalt.mordant.widgets.progress.TaskId

// TODO docs
interface ProgressTaskUpdateScope<T> {
    var context: T
    var completed: Long
    var total: Long?
    var visible: Boolean
}

interface ProgressTask<T> {
    /**
     * Update the task's state.
     *
     * If the completed count is equal to the total, the task will be marked as [finished].
     *
     * If the task is already finished, this method will still update the task's state, but it will
     * remain marked as finished. Use [reset] if you want to start the task again.
     */
    fun update(block: ProgressTaskUpdateScope<T>.() -> Unit)
    fun start() //TODO doc that this is for calculating elapsed times
    fun pause()

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

    fun makeState(): ProgressState<T>

    val finished: Boolean
    val started: Boolean
    val paused: Boolean
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
fun ProgressTask<*>.advance(amount: Int) = advance(amount.toLong())

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
fun ProgressTask<*>.update(completed: Int) = update(completed.toLong())

interface ProgressBarAnimation<T> {
    fun addTask(
        context: T,
        total: Long? = null,
        completed: Long = 0,
        start: Boolean = true,
        visible: Boolean = true,
    ): ProgressTask<T>

    /**
     * Remove a task from the progress bar.
     *
     * @return `true` if the task was removed, `false` if it was not found.
     */
    fun removeTask(task: ProgressTask<T>): Boolean

    // TODO mention this is called automatically
    /**
     * Refresh the progress and draw it to the screen.
     */
    fun refresh()

    fun clear()

    val finished: Boolean
}

fun ProgressBarAnimation<Unit>.addTask(
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
): ProgressTask<Unit> {
    return addTask(Unit, total, completed, start, visible)
}

