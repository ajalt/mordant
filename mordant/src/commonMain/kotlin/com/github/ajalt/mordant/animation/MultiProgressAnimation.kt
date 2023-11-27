package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.widgets.ProgressState
import com.github.ajalt.mordant.widgets.TaskId

interface ProgressTaskUpdateScope<T> {
    var context: T
    var completed: Long
    var total: Long?
    var visible: Boolean
}

interface ProgressTask<T> {
    fun update(block: ProgressTaskUpdateScope<T>.() -> Unit)
    fun start() // doc that this is for calculating elapsed times
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
    val id: TaskId
}

/**
 * Advance the completed progress of this task by [amount].
 *
 * This is a shortcut for `update { completed += amount }`.
 */
fun <T> ProgressTask<T>.advance(amount: Long = 1) = update { completed += amount }


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
    fun refresh() // was tick()

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

