package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.RefreshableAnimation
import com.github.ajalt.mordant.animation.asRefreshable
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
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

class BaseBlockingAnimator(
    private val terminal: Terminal,
    private val animation: RefreshableAnimation,
    private val rate: Duration,
) : BlockingAnimator {

    private var stopped = false
    private val lock = Any()

    override fun runBlocking() {
        synchronized(lock) {
            stopped = false
            terminal.cursor.hide(showOnExit = true)
        }
        try {
            while (synchronized(lock) { !stopped && !animation.finished }) {
                synchronized(lock) { animation.refresh(refreshAll = false) }
                Thread.sleep(rate.inWholeMilliseconds)
            }
            synchronized(lock) {
                // final refresh to show finished state
                if (!stopped) animation.refresh(refreshAll = true)
            }
        } finally {
            synchronized(lock) { terminal.cursor.show() }
        }
    }

    override fun stop(): Unit = synchronized(lock) {
        stopped = true
        animation.stop()
    }

    override fun clear(): Unit = synchronized(lock) {
        stopped = true
        animation.clear()
    }

}

class BlockingProgressBarAnimation<T> private constructor(
    private val animation: ProgressBarAnimation<T>,
    private val animator: BlockingAnimator,
) : ProgressBarAnimation<T> by animation, BlockingAnimator by animator {
    private constructor(
        terminal: Terminal,
        animation: BaseProgressBarAnimation<T>,
        rate: Duration,
    ) : this(animation, BaseBlockingAnimator(terminal, animation, rate))

    constructor(
        terminal: Terminal,
        factory: CachedProgressBarWidgetMaker<T>,
        clearWhenFinished: Boolean = false,
        speedEstimateDuration: Duration = 30.seconds,
    ) : this(
        terminal,
        BaseProgressBarAnimation(terminal, factory, clearWhenFinished, speedEstimateDuration),
        factory.refreshPeriod
    )
}

/**
 * Create a progress bar animation that runs synchronously.
 *
 * Use [execute] to run the animation on a background thread.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateOnThread(terminal)
 * val task = animation.addTask()
 * animation.execute()
 * task.update { ... }
 * ```
 */
fun <T> ProgressBarDefinition<T>.animateOnThread(
    terminal: Terminal,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): BlockingProgressBarAnimation<T> {
    return BlockingProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        clearWhenFinished,
        speedEstimateDuration,
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
    return BaseBlockingAnimator(terminal, asRefreshable(finished), (1.0 / fps).seconds)
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
