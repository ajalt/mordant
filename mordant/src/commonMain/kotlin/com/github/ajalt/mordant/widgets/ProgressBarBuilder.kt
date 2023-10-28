package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS

// XXX: this is internal for backcompat, could be `private`
internal fun calcHz(completed: Long, elapsed: Duration) = when {
    completed <= 0 || elapsed <= Duration.ZERO -> 0.0
    else -> completed / elapsed.toDouble(SECONDS)
}

data class ProgressState<T>(
    val context: T,
    val total: Long?,
    val completed: Long,
    val elapsed: Duration,
    val completedPerSecond: Double = calcHz(completed, elapsed),
) {
    val isIndeterminate: Boolean get() = total == null
    val isFinished: Boolean get() = total?.let { completed >= it } ?: false
}


/**
 * Create a [ProgressState] with no context.
 */
fun ProgressState(
    total: Long?,
    completed: Long,
    elapsed: Duration = Duration.ZERO,
    completedPerSecond: Double = calcHz(completed, elapsed),
): ProgressState<Unit> {
    return ProgressState(Unit, total, completed, elapsed, completedPerSecond)
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
    fun build(vararg states: ProgressState<T>): Widget
}

fun <T> ProgressBarWidgetFactory<T>.build(
    context: T,
    total: Long?,
    completed: Long,
    elapsed: Duration,
    completedPerSecond: Double = calcHz(completed, elapsed),
): Widget {
    return build(ProgressState(context, total, completed, elapsed, completedPerSecond))
}

// TODO test these
fun ProgressBarWidgetFactory<Unit>.build(
    total: Long?,
    completed: Long,
    elapsed: Duration,
    completedPerSecond: Double = calcHz(completed, elapsed),
): Widget {
    return build(ProgressState(total, completed, elapsed, completedPerSecond))
}

// TODO: move impls to bottom of file
private class ProgressBarWidgetFactoryImpl<T>(
    // TODO var alignColumns: Boolean = true (linear layouts vs table)
    private val spacing: Int,
    private val cells: MutableList<ProgressBarWidgetBuilder.Cell<T>>,
) : ProgressBarWidgetFactory<T> {

    override fun build(vararg states: ProgressState<T>): Widget {
        return table {
//          TODO  verticalAlign = verticalAlign
            cellBorders = Borders.NONE
            padding = Padding { left = spacing }
            column(0) { padding = Padding(0) }
            cells.forEachIndexed { i, it ->
                column(i) {
                    align = it.align ?: TextAlign.RIGHT
                    width = when (val w = it.columnWidth) {
                        // The fixed width cells don't include padding, so add it here
                        is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + spacing)
                        else -> w
                    }
                }
            }
            body {
                for (state in states) {
                    rowFrom(cells.map { cell ->
                        cell.builder(
                            ProgressState(
                                state.context,
                                state.total,
                                state.completed,
                                state.elapsed,
                                state.completedPerSecond
                            )
                        )
                    })
                }
            }
        }
    }
}

// XXX: this is internal for backcompat, could be `private`
internal class ProgressBarWidgetBuilder<T> : ProgressBarBuilder<T> {
    class Cell<T>(
        val columnWidth: ColumnWidth,
        val align: TextAlign?,
        val builder: ProgressState<T>.() -> Widget,
    )

    private val cells: MutableList<Cell<T>> = mutableListOf()

    override fun cell(
        width: ColumnWidth,
        fps: Int,
        align: TextAlign?,
        builder: ProgressState<T>.() -> Widget
    ) {
        cells += Cell(width, align, builder)
    }

    fun build(spacing: Int): ProgressBarWidgetFactory<T> {
        return ProgressBarWidgetFactoryImpl(spacing, cells)
    }
}

fun <T> progressBarContextLayout(
    spacing: Int = 2,
    init: ProgressBarBuilder<T>.() -> Unit,
): ProgressBarWidgetFactory<T> {
    return ProgressBarWidgetBuilder<T>().apply(init).build(spacing)
}

// TODO test this
fun progressBarLayout(
    spacing: Int = 2,
    init: ProgressBarBuilder<Unit>.() -> Unit,
): ProgressBarWidgetFactory<Unit> {
    return progressBarContextLayout(spacing, init)
}
