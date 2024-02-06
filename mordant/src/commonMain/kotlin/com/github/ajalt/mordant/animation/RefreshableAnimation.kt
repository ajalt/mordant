package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.widgets.progress.ProgressBarCell
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A version of [Animation] that has a parameterless [refresh] method instead of `update`.
 *
 * Implementations will need to handle concurrently updating their state.
 */
interface RefreshableAnimation {
    /**
     * Draw the animation to the screen.
     *
     * This is called automatically when the animation is running, so you don't usually need to call
     * it manually.
     *
     * @param refreshAll If `true`, refresh all cells, ignoring their [fps][ProgressBarCell.fps].
     */
    fun refresh(refreshAll: Boolean)

    /**
     * Stop this animation and remove it from the screen.
     *
     * Future calls to [refresh] will cause the animation to resume.
     */
    fun clear()

    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     *
     * Future calls to [refresh] will cause the animation to start again.
     */
    fun stop()

    /**
     * `true` if this animation has finished and should be [stopped][stop] or [cleared][clear].
     */
    val finished: Boolean

    /**
     * The rate, in Hz, that this animation should be refreshed, or 0 if it should not be refreshed
     * automatically.
     */
    val fps: Int get() = 5
}

/** The time between refreshes. This is `1 / refreshRate` */
val RefreshableAnimation.refreshPeriod: Duration
    get() = (1.0 / fps).seconds

/**
 * Convert this [Animation] to a [RefreshableAnimation].
 *
 * ### Example
 * ```
 * terminal.animation<Unit> {/*...*/}.asRefreshable().animateOnThread()
 * ```
 *
 * @param fps The rate at which the animation should be refreshed.
 * @param finished A function that returns `true` if the animation has finished.
 */
inline fun Animation<Unit>.asRefreshable(
    fps: Int = 5,
    crossinline finished: () -> Boolean = { false },
): RefreshableAnimation {
    return object : RefreshableAnimation {
        override fun refresh(refreshAll: Boolean) = update(Unit)
        override fun clear() = this@asRefreshable.clear()
        override fun stop() = this@asRefreshable.stop()
        override val finished: Boolean get() = finished()
        override val fps: Int get() = fps
    }
}
