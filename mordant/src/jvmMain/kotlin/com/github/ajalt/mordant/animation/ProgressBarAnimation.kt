//package com.github.ajalt.mordant.animation
//
//import com.github.ajalt.mordant.widgets.ProgressBarBuilder
//import kotlin.time.TimeSource
//
//interface ProgressTask<T> {
//    fun update(context: T, completed: Long? = null, total: Long? = null)
//    fun update(completed: Long? = null, total: Long? = null)
//    fun advance(amount: Long = 1)
//    fun start() // doc that this is for calculating elapsed times
//    fun stop()
//    fun reset(
//        context: T,
//        completed: Long = 0,
//        total: Long = 0,
//        start: Boolean = true,
//        visible: Boolean = true,
//    )
//}
//
//fun ProgressTask<Unit>.reset(
//    completed: Long = 0,
//    total: Long = 0,
//    start: Boolean = true,
//    visible: Boolean = true,
//) {
//    reset(Unit, total, completed, start, visible)
//}
//
//interface ProgressBarAnimation<T> {
//    fun addTask(
//        context: T,
//        completed: Long = 0,
//        total: Long = 0,
//        start: Boolean = true,
//        visible: Boolean = true,
//    ): ProgressTask<T>
//
//    // TODO mention this is called automatically
//    /**
//     * Refresh the progress and draw it to the screen.
//     */
//    fun refresh() // was tick()
//
////    fun start()
////    fun pause()
//}
//
//fun ProgressBarAnimation<Unit>.addTask(
//    total: Long = 0,
//    completed: Long = 0,
//): ProgressTask<Unit> {
//    return addTask(Unit, total, completed)
//}
//
//fun <T> progressBarContextAnimation(
//    spacing: Int = 2,
//    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
//    init: ProgressBarBuilder<T>.() -> Unit,
//): ProgressBarAnimation<T> {
//    TODO()
////    return ProgressBarWidgetBuilder<T>().apply(init).build(spacing)
//}
//
//// <editor-fold desc="Implementations">
//
//// </editor-fold>
