package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.RefreshableAnimation
import com.github.ajalt.mordant.animation.asRefreshable
import com.github.ajalt.mordant.animation.refreshPeriod
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.MultiProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.ProgressBarDefinition
import com.github.ajalt.mordant.widgets.progress.ProgressBarWidgetMaker
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


interface BlockingAnimator {
    /**
     * Start the animation and refresh it until all its tasks are finished.
     *
     * This calls [Thread.sleep] between each frame, so it should usually be run in a separate
     * thread so that you can update the state concurrently.
     *
     * @see execute
     */
    fun runBlocking()

    /**
     * Stop the animation, but leave it on the screen.
     */
    fun stop()

    /**
     * Stop the animation and remove it from the screen.
     */
    fun clear()
}

/**
 * A [BlockingAnimator] for a single [task][ProgressTask].
 */
interface ThreadProgressTaskAnimator<T> : BlockingAnimator, ProgressTask<T>

/**
 * A [BlockingAnimator] for a [ProgressBarAnimation].
 */
interface ThreadProgressAnimator<T> : BlockingAnimator, ProgressBarAnimation<T>

class BaseBlockingAnimator(
    private val terminal: Terminal,
    private val animation: RefreshableAnimation,
) : BlockingAnimator {
    private var stopped = false
    private val lock = Any()

    override fun runBlocking() {
        synchronized(lock) {
            stopped = false
            terminal.cursor.hide(showOnExit = true)
        }
        while (synchronized(lock) { !stopped && !animation.finished }) {
            synchronized(lock) { animation.refresh(refreshAll = false) }
            Thread.sleep(animation.refreshPeriod.inWholeMilliseconds)
        }
        synchronized(lock) {
            // final refresh to show finished state
            if (!stopped) animation.refresh(refreshAll = true)
        }
    }

    override fun stop(): Unit = synchronized(lock) {
        if (stopped) return@synchronized
        animation.stop()
        terminal.cursor.show()
        stopped = true
    }

    override fun clear(): Unit = synchronized(lock) {
        if (stopped) return@synchronized
        animation.clear()
        terminal.cursor.show()
        stopped = true
    }

}

class BlockingProgressBarAnimation<T> private constructor(
    private val animation: ProgressBarAnimation<T>,
    private val animator: BlockingAnimator,
) : ProgressBarAnimation<T> by animation, BlockingAnimator by animator {
    private constructor(
        terminal: Terminal,
        animation: MultiProgressBarAnimation<T>,
    ) : this(animation, BaseBlockingAnimator(terminal, animation))

    constructor(
        terminal: Terminal,
        clearWhenFinished: Boolean = false,
        speedEstimateDuration: Duration = 30.seconds,
        maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
        timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    ) : this(
        terminal,
        MultiProgressBarAnimation(
            terminal, clearWhenFinished, speedEstimateDuration, maker, timeSource
        ),
    )
}

/**
 * Create a progress bar animation with a single task that runs synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarContextLayout<String> { ... }.animateOnThread(terminal, "context")
 * animation.execute()
 * animation.update { ... }
 * ```
 *
 * @param terminal The terminal to draw the progress bar to
 * @param context The context to pass to the task
 * @param total The total number of steps needed to complete the progress task, or `null` if it is indeterminate.
 * @param completed The number of steps currently completed in the progress task.
 * @param start If `true`, start the task immediately.
 * @param visible If `false`, the task will not be drawn to the screen.
 * @param clearWhenFinished If `true`, the animation will be cleared when all tasks are finished. Otherwise, the animation will stop when all tasks are finished, but remain on the screen.
 * @param speedEstimateDuration The duration over which to estimate the speed of the progress tasks. This estimate will be a rolling average over this duration.
 * @param timeSource The time source to use for the animation.
 * @param maker The widget maker to use to lay out the progress bars.
 */
fun <T> ProgressBarDefinition<T>.animateOnThread(
    terminal: Terminal,
    context: T,
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
): ThreadProgressTaskAnimator<T> {
    val animation = BlockingProgressBarAnimation<T>(
        terminal,
        clearWhenFinished,
        speedEstimateDuration,
        maker,
        timeSource
    )
    val task = animation.addTask(this, context, total, completed, start, visible)
    return ThreadProgressTaskAnimatorImpl(task, animation)
}

/**
 * Create a progress bar animation for a single task that runs synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateOnThread(terminal)
 * animation.execute()
 * animation.update { ... }
 * ```
 * @param terminal The terminal to draw the progress bar to
 * @param total The total number of steps needed to complete the progress task, or `null` if it is indeterminate.
 * @param completed The number of steps currently completed in the progress task.
 * @param start If `true`, start the task immediately.
 * @param visible If `false`, the task will not be drawn to the screen.
 * @param clearWhenFinished If `true`, the animation will be cleared when all tasks are finished. Otherwise, the animation will stop when all tasks are finished, but remain on the screen.
 * @param speedEstimateDuration The duration over which to estimate the speed of the progress tasks. This estimate will be a rolling average over this duration.
 * @param timeSource The time source to use for the animation.
 * @param maker The widget maker to use to lay out the progress bars.
 */
fun ProgressBarDefinition<Unit>.animateOnThread(
    terminal: Terminal,
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
): ThreadProgressTaskAnimator<Unit> {
    return animateOnThread(
        terminal = terminal,
        context = Unit,
        total = total,
        completed = completed,
        start = start,
        visible = visible,
        clearWhenFinished = clearWhenFinished,
        speedEstimateDuration = speedEstimateDuration,
        timeSource = timeSource,
        maker = maker
    )
}

/**
 * Create an animator that runs this animation synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animation = terminal.animation<Unit>{ ... }.animateOnThread(terminal)
 * animation.execute()
 * ```
 */
inline fun Animation<Unit>.animateOnThread(
    fps: Int = 30,
    crossinline finished: () -> Boolean = { false },
): BlockingAnimator {
    return asRefreshable(fps, finished).animateOnThread(terminal)
}

/**
 * Create an animator that runs this animation synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animator = animation.animateOnThread(terminal)
 * animator.execute()
 * ```
 */
fun RefreshableAnimation.animateOnThread(terminal: Terminal): BlockingAnimator {
    return BaseBlockingAnimator(terminal, this)
}

/**
 * Create an animator that runs this animation synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animator = animation.animateOnThread(terminal)
 * animator.execute()
 * ```
 */
fun <T, U> T.animateOnThread(terminal: Terminal): ThreadProgressAnimator<U>
        where T : RefreshableAnimation, T : ProgressBarAnimation<U> {
    val animator = (this as RefreshableAnimation).animateOnThread(terminal)
    return ThreadProgressAnimatorImpl(this, animator)
}

/**
 * Run the animation in a background thread on an [executor].
 *
 * @return a [Future] that can be used to cancel the animation.
 */
fun BlockingAnimator.execute(
    executor: ExecutorService = Executors.newSingleThreadExecutor(DaemonThreadFactory()),
): Future<*> {
    return executor.submit(::runBlocking)
}

private class DaemonThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable): Thread = Thread(r).also {
        it.name = "${it.name}-mordant-animator"
        it.isDaemon = true
    }
}

private class ThreadProgressTaskAnimatorImpl<T>(
    private val task: ProgressTask<T>,
    private val animator: BlockingAnimator,
) : ThreadProgressTaskAnimator<T>, BlockingAnimator by animator, ProgressTask<T> by task

private class ThreadProgressAnimatorImpl<T>(
    private val animation: ProgressBarAnimation<T>,
    private val animator: BlockingAnimator,
) : ThreadProgressAnimator<T>, BlockingAnimator by animator, ProgressBarAnimation<T> by animation
