package com.github.ajalt.mordant.animation.coroutines
//
//import com.github.ajalt.mordant.animation.BaseProgressBarAnimation
//import com.github.ajalt.mordant.terminal.Terminal
//import com.github.ajalt.mordant.widgets.CachedProgressBarWidgetFactory
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.internal.LockFreeLinkedListHead
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.seconds
//import kotlin.time.TimeSource
//
//class CoroutinesProgressBarAnimation<T>(
//    terminal: Terminal,
//    factory: CachedProgressBarWidgetFactory<T>,
//    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
//    speedEstimateDuration: Duration = 30.seconds,
//) : BaseProgressBarAnimation<T>(terminal, factory, timeSource, speedEstimateDuration) {
//
//}
//
//fun foo() {
//    Dispatchers.Default.limitedParallelism()
//}
