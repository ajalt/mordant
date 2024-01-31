package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.RefreshableAnimation
import com.github.ajalt.mordant.animation.asRefreshable
import com.github.ajalt.mordant.animation.progress.BaseProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
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

class BaseCoroutineAnimator(
    private val terminal: Terminal,
    private val animation: RefreshableAnimation,
    private val rate: Duration,
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
            delay(rate)
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
        animation: BaseProgressBarAnimation<T>,
        rate: Duration,
    ) : this(animation, BaseCoroutineAnimator(terminal, animation, rate))

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
 * Create a progress bar animation that runs in a coroutine.
 *
 * ### Example
 *
 * ```
 * val animation = progressBarLayout { ... }.animateInCoroutine(terminal)
 * val task = animation.addTask()
 * launch { animation.run() }
 * task.update { ... }
 * ```
 */
fun <T> ProgressBarDefinition<T>.animateInCoroutine(
    terminal: Terminal,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    clearWhenFinished: Boolean = false,
    speedEstimateDuration: Duration = 30.seconds,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): CoroutineProgressBarAnimation<T> {
    return CoroutineProgressBarAnimation(
        terminal,
        cache(timeSource, maker),
        clearWhenFinished,
        speedEstimateDuration,
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
    return BaseCoroutineAnimator(terminal, asRefreshable(finished), (1.0 / fps).seconds)
}
