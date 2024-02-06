package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.RefreshableAnimation
import com.github.ajalt.mordant.animation.asRefreshable
import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressTask
import com.github.ajalt.mordant.animation.refreshPeriod
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.MultiProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.ProgressBarDefinition
import com.github.ajalt.mordant.widgets.progress.ProgressBarWidgetMaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

interface CoroutineAnimator {
    /**
     * Start the animation and refresh it until all its tasks are finished.
     */
    suspend fun run()

    /**
     * Stop the animation, but leave it on the screen.
     */
    suspend fun stop()

    /**
     * Stop the animation and remove it from the screen.
     */
    suspend fun clear()
}


/**
 * A [CoroutineAnimator] for a single [task][ProgressTask].
 */
interface CoroutineTaskAnimator<T> : CoroutineAnimator, ProgressTask<T>

class BaseCoroutineAnimator(
    private val terminal: Terminal,
    private val animation: RefreshableAnimation,
) : CoroutineAnimator {
    private var stopped = false
    private val mutex = Mutex()

    override suspend fun run() {
        mutex.withLock {
            stopped = false
            terminal.cursor.hide(showOnExit = true)
        }
        while (mutex.withLock { !stopped && !animation.finished }) {
            mutex.withLock { animation.refresh(refreshAll = false) }
            delay(animation.refreshPeriod.inWholeMilliseconds)
        }
        mutex.withLock {
            // final refresh to show finished state
            if (!stopped) animation.refresh(refreshAll = true)
        }
    }

    override suspend fun stop(): Unit = mutex.withLock {
        if (stopped) return@withLock
        animation.stop()
        terminal.cursor.show()
        stopped = true
    }

    override suspend fun clear(): Unit = mutex.withLock {
        if (stopped) return@withLock
        animation.clear()
        terminal.cursor.show()
        stopped = true
    }
}

class CoroutineProgressBarAnimation<T> private constructor(
    private val animation: ProgressBarAnimation<T>,
    private val animator: CoroutineAnimator,
) : ProgressBarAnimation<T> by animation, CoroutineAnimator by animator {
    private constructor(
        terminal: Terminal,
        animation: MultiProgressBarAnimation<T>,
    ) : this(animation, BaseCoroutineAnimator(terminal, animation))

    constructor(
        terminal: Terminal,
        maker: ProgressBarWidgetMaker,
        clearWhenFinished: Boolean = false,
        speedEstimateDuration: Duration = 30.seconds,
        timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    ) : this(
        terminal,
        MultiProgressBarAnimation(
            terminal,
            clearWhenFinished,
            speedEstimateDuration,
            maker,
            timeSource
        ),
    )
}

/**
 * Create a progress bar animation that runs in a coroutine.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarContextLayout<String> { ... }.animateInCoroutine(terminal, "context")
 * launch { animation.run() }
 * animation.update { ... }
 * ```
 */
fun <T> ProgressBarDefinition<T>.animateInCoroutine(
    // TODO param docs (copy from addTask)
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
): CoroutineTaskAnimator<T> {
    val animation = CoroutineProgressBarAnimation<T>(
        terminal,
        maker,
        clearWhenFinished,
        speedEstimateDuration,
        timeSource
    )
    val task = animation.addTask(this, context, total, completed, start, visible)
    return CoroutineTaskAnimatorImpl(task, animation)
}

/**
 * Create a progress bar animation for a single task that runs synchronously.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateInCoroutine(terminal)
 * launch { animation.run() }
 * animation.update { ... }
 * ```
 */
fun ProgressBarDefinition<Unit>.animateInCoroutine(
    // TODO param docs (copy from addTask)
    terminal: Terminal,
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
): CoroutineTaskAnimator<Unit> {
    return animateInCoroutine(
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
 * Create an animator that runs this animation in a coroutine.
 *
 * ### Example
 * ```
 * val animation = terminal.animation<Unit> { ... }.animateInCoroutine(terminal)
 * launch { animation.run() }
 * ```
 */
inline fun Animation<Unit>.animateInCoroutine(
    fps: Int = 30,
    crossinline finished: () -> Boolean = { false },
): CoroutineAnimator {
    return BaseCoroutineAnimator(terminal, asRefreshable(fps, finished))
}

private class CoroutineTaskAnimatorImpl<T>(
    private val task: ProgressTask<T>,
    private val animator: CoroutineAnimator,
) : CoroutineTaskAnimator<T>, CoroutineAnimator by animator, ProgressTask<T> by task