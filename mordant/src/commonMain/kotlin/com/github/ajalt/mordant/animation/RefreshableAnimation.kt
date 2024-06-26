package com.github.ajalt.mordant.animation

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface Refreshable {
    /**
     * `true` if this animation has finished and should be stopped or cleared.
     */
    val finished: Boolean

    /**
     * Draw the animation to the screen.
     *
     * This is called automatically when the animation is running, so you don't usually need to call
     * it manually.
     *
     * @param refreshAll If `true`, refresh all contents, ignoring their fps.
     */
    fun refresh(refreshAll: Boolean = false)
}

/**
 * A version of [Animation] that has a parameterless [refresh] method instead of `update`.
 *
 * Implementations will need to handle concurrently updating their state.
 */
interface RefreshableAnimation : Refreshable, StoppableAnimation {

    /**
     * Stop this animation and remove it from the screen.
     *
     * Future calls to [refresh] will cause the animation to resume.
     */
    override fun clear()

    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     *
     * Future calls to [refresh] will cause the animation to start again.
     */
    override fun stop()

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
