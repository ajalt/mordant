package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.AbstractProgressBarAnimation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.CachedProgressBarWidgetFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class CoroutinesProgressBarAnimation<T>(
    terminal: Terminal,
    factory: CachedProgressBarWidgetFactory<T>,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    speedEstimateDuration: Duration = 30.seconds,
) : AbstractProgressBarAnimation<T>(terminal, factory, timeSource, speedEstimateDuration) {
    override fun <T> withLock(block: () -> T): T {
        TODO()
    }
}
