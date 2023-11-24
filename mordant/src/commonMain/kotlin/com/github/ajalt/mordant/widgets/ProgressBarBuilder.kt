package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.TimeSource

interface TaskId

// TODO: docs
// TODO: make total and completed `Double`?
data class ProgressState<T>(
    /** The context object passed to the progress task. */
    val context: T,
    /** The total number of steps needed to complete the progress task, or `null` if it is indeterminate. */
    val total: Long?,
    /** The number of steps currently completed in the progress task. */
    val completed: Long,
    /**
     * The time that the progress layout was first constructed.
     *
     * Use this for continuous animations, since it's the same for all tasks.
     */
    val animationTime: ComparableTimeMark,
    /** The time that the progress task was started, or `null` if it hasn't been started. */
    val startedTime: ComparableTimeMark? = null,
    /** The time that the progress task was paused, or `null` if it isn't paused. */
    val pausedTime: ComparableTimeMark? = null,
    /** The time that the progress task was finished, or `null` if it isn't finished. */
    val finishedTime: ComparableTimeMark? = null,
    /**
     * The estimated speed of the progress task, in steps per second, or `null` if it hasn't started.
     *
     * If the task is finished or paused, this is the speed at the time it finished.
     */
    val speed: Double? = null,

    val taskId: TaskId = object : TaskId {},
) {
    val isIndeterminate: Boolean get() = total == null
    val isPaused: Boolean get() = pausedTime != null
    val isStarted: Boolean get() = startedTime != null
    val isFinished: Boolean get() = finishedTime != null
}

/**
 * Create a [ProgressState] with no context.
 */
fun ProgressState(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
): ProgressState<Unit> {
    return ProgressState(
        Unit, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
    )
}


interface ProgressBarBuilder<T> {
    /**
     * Add a cell to this layout.
     */
    fun cell(
        // TODO document params
        width: ColumnWidth = ColumnWidth.Auto,
        fps: Int = 5,
        align: TextAlign? = null,
        builder: ProgressState<T>.() -> Widget,
    )
}

interface ProgressBarWidgetFactory<T> {
    fun build(states: List<ProgressState<T>>): Widget
}

fun <T> ProgressBarWidgetFactory<T>.build(vararg states: ProgressState<T>): Widget {
    return build(states.asList())
}

interface CachedProgressBarWidgetFactory<T> : ProgressBarWidgetFactory<T> {
    fun invalidateCache()
}

fun <T> ProgressBarWidgetFactory<T>.build(
    context: T,
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
): Widget {
    return build(
        ProgressState(
            context, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
        )
    )
}

// TODO test these
fun ProgressBarWidgetFactory<Unit>.build(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    startedTime: ComparableTimeMark? = null,
    pausedTime: ComparableTimeMark? = null,
    finishedTime: ComparableTimeMark? = null,
    speed: Double? = null,
): Widget {
    return build(
        ProgressState(
            Unit, total, completed, displayedTime, startedTime, pausedTime, finishedTime, speed
        )
    )
}

fun <T> progressBarContextLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    init: ProgressBarBuilder<T>.() -> Unit,
): ProgressBarWidgetFactory<T> {
    return ProgressBarFactoryBuilderImpl<T>().apply(init).build(spacing, alignColumns)
}

// TODO test this
fun progressBarLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    init: ProgressBarBuilder<Unit>.() -> Unit,
): ProgressBarWidgetFactory<Unit> {
    return progressBarContextLayout(spacing, alignColumns, init)
}

// <editor-fold desc="Implementations">
internal class ProgressBarWidgetFactoryImpl<T>(
    private val spacing: Int,
    private val alignColumns: Boolean = true,
    private val cells: List<ProgressBarFactoryBuilderImpl.Cell<T>>,
) : ProgressBarWidgetFactory<T> {

    override fun build(vararg states: ProgressState<T>): Widget {
        return if (alignColumns) makeTable(states) else makeLinearLayout(states)
    }

    private fun makeLinearLayout(states: Array<out ProgressState<T>>): Widget {
        return verticalLayout {
            for (state in states) {
                cell(horizontalLayout {
                    spacing = this@ProgressBarWidgetFactoryImpl.spacing
                    cells.forEachIndexed { i, cell ->
                        column(i) {
                            align = cell.align ?: TextAlign.RIGHT
                            width = cell.columnWidth
                        }
                    }
                    cellsFrom(makeWidgets(state))
                })
            }
        }
    }

    private fun makeTable(states: Array<out ProgressState<T>>): Table {
        return table {
            //TODO  verticalAlign = verticalAlign
            cellBorders = Borders.NONE
            padding = Padding { left = spacing }
            column(0) { padding = Padding(0) }
            cells.forEachIndexed { i, cell ->
                column(i) {
                    align = cell.align ?: TextAlign.RIGHT
                    val w = cell.columnWidth
                    width = when {
                        // The fixed width cells don't include padding, so add it here
                        i > 0 && w is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + spacing)
                        else -> w
                    }
                }
            }
            body {
                for (state in states) {
                    rowFrom(makeWidgets(state))
                }
            }
        }
    }

    private fun makeWidgets(state: ProgressState<T>): List<Widget> {
        return cells.map { it.builder(state) }
    }
}

internal class CachedProgressBarWidgetFactoryImpl<T>(
    spacing: Int,
    alignColumns: Boolean,
    cells: List<ProgressBarFactoryBuilderImpl.Cell<T>>,
    private val timeSource: TimeSource.WithComparableMarks,
) : CachedProgressBarWidgetFactory<T> {
    private var invalidatedAt = timeSource.markNow()

    private val factory = ProgressBarWidgetFactoryImpl(spacing, alignColumns, cells.map(::makeCell))

    private fun makeCell(
        cell: ProgressBarFactoryBuilderImpl.Cell<T>,
    ): ProgressBarFactoryBuilderImpl.Cell<T> {
        // Wrap the cell builder in a block that caches the widget
        val cachedWidgets = mutableMapOf<TaskId, Pair<ComparableTimeMark, Widget>>()
        return ProgressBarFactoryBuilderImpl.Cell(cell.columnWidth, cell.fps, cell.align) {
            getCachedWidget(cell, cachedWidgets) ?: cell.builder(this).also {
                cachedWidgets[taskId] = timeSource.markNow() to it
            }
        }
    }

    private fun ProgressState<T>.getCachedWidget(
        cell: ProgressBarFactoryBuilderImpl.Cell<T>,
        cachedWidgets: MutableMap<TaskId, Pair<ComparableTimeMark, Widget>>,
    ): Widget? {
        val (lastFrameTime, widget) = cachedWidgets[taskId] ?: return null
        if (lastFrameTime < invalidatedAt) return null
        val timeSinceLastFrame = lastFrameTime.elapsedNow()
        val timePerFrame = (1.0 / cell.fps).seconds
        if (timeSinceLastFrame < timePerFrame) return widget
        return null
    }

    override fun build(vararg states: ProgressState<T>): Widget = factory.build(*states)

    override fun invalidateCache() {
        invalidatedAt = timeSource.markNow()
    }
}

// XXX: this interface is just for backcompat with ProgressBuilder
internal interface ProgressBarFactoryBuilder<T> : ProgressBarBuilder<T> {
    fun build(spacing: Int, alignColumns: Boolean): ProgressBarWidgetFactory<T>
    fun buildCached(
        spacing: Int,
        alignColumns: Boolean,
        timeSource: TimeSource.WithComparableMarks,
    ): CachedProgressBarWidgetFactory<T>
}

// XXX: this is internal for backcompat, could be `private`
internal class ProgressBarFactoryBuilderImpl<T> : ProgressBarFactoryBuilder<T> {
    class Cell<T>(
        val columnWidth: ColumnWidth,
        val fps: Int,
        val align: TextAlign?,
        val builder: ProgressState<T>.() -> Widget,
    )

    private val cells: MutableList<Cell<T>> = mutableListOf()

    override fun cell(
        width: ColumnWidth,
        fps: Int,
        align: TextAlign?,
        builder: ProgressState<T>.() -> Widget,
    ) {
        cells += Cell(width, fps, align, builder)
    }

    override fun build(spacing: Int, alignColumns: Boolean): ProgressBarWidgetFactory<T> {
        return ProgressBarWidgetFactoryImpl(spacing, alignColumns, cells)
    }

    override fun buildCached(
        spacing: Int,
        alignColumns: Boolean,
        timeSource: TimeSource.WithComparableMarks,
    ): CachedProgressBarWidgetFactory<T> {
        return CachedProgressBarWidgetFactoryImpl(spacing, alignColumns, cells, timeSource)
    }
}

// XXX: this is internal for backcompat, could be `private`
internal fun calcHz(completed: Long, elapsed: Duration): Double = when {
    completed <= 0 || elapsed <= Duration.ZERO -> 0.0
    else -> completed / elapsed.toDouble(SECONDS)
}
// </editor-fold>
