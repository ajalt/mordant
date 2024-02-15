package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.VerticalAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import kotlin.time.ComparableTimeMark

internal const val TEXT_FPS = 5
internal const val ANIMATION_FPS = 30

/** A unique identifier for a task in a progress bar. */
class TaskId

interface ProgressLayoutScope<T> {

    /** The default framerate for text based cells */
    val textFps: Int

    /** The default framerate for animation cells */
    val animationFps: Int

    /** The default horizontal alignment for cells */
    val align: TextAlign

    /** The default vertical alignment for cells */
    val verticalAlign: VerticalAlign

    /**
     * Add a cell to this layout.
     *
     * The [content] will be called every time the cell is rendered. In the case of
     * animations, that will usually be at its [fps].
     *
     * @param width The width of the cell.
     * @param fps The number of times per second to refresh the cell when animated. If 0, the cell
     *   should not be refreshed.
     * @param align The text alignment for the cell when multiple tasks are present and cells are
     *   aligned, or `null` to use [the default][ProgressLayoutScope.align].
     * @param verticalAlign The vertical alignment for the cell if there are other taller cells in
     *   the layout, or `null` to use [the default][ProgressLayoutScope.verticalAlign].
     * @param content A lambda returning the widget to display in this cell.
     */
    fun cell(
        width: ColumnWidth = ColumnWidth.Auto,
        fps: Int = textFps,
        align: TextAlign? = null,
        verticalAlign: VerticalAlign? = null,
        content: ProgressState<T>.() -> Widget,
    )
}

data class ProgressBarCell<T>(
    val columnWidth: ColumnWidth = ColumnWidth.Auto,
    val fps: Int = TEXT_FPS,
    val align: TextAlign = TextAlign.RIGHT,
    val verticalAlign: VerticalAlign = VerticalAlign.BOTTOM,
    val content: ProgressState<T>.() -> Widget,
) {
    init {
        require(fps >= 0) { "fps cannot be negative" }
    }
}

/**
 * The cells and configuration for a progress bar layout.
 */
interface ProgressBarDefinition<T> {
    /**
     * The cells in this layout.
     */
    val cells: List<ProgressBarCell<T>>

    /**
     * The spacing between cells in this layout.
     */
    val spacing: Int

    /**
     * How to align the columns of the progress bar when multiple tasks are present.
     */
    val alignColumns: Boolean
}

private class ProgressBarDefinitionImpl<T>(
    override val cells: List<ProgressBarCell<T>>,
    override val spacing: Int,
    override val alignColumns: Boolean,
) : ProgressBarDefinition<T>

/** Create a new progress bar layout definition. */
fun <T> ProgressBarDefinition(
    cells: List<ProgressBarCell<T>>,
    spacing: Int,
    alignColumns: Boolean,
): ProgressBarDefinition<T> {
    return ProgressBarDefinitionImpl(cells, spacing, alignColumns)
}

/** Create a widget for this [ProgressBarDefinition] with the given state. */
fun <T> ProgressBarDefinition<T>.build(
    context: T,
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    status: ProgressState.Status = ProgressState.Status.NotStarted,
    speed: Double? = null,
    maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
): Widget {
    val state = ProgressState(
        context, total, completed, displayedTime, status, speed
    )
    return maker.build(ProgressBarMakerRow(this, state))
}

/** Create a widget for this [ProgressBarDefinition] with the given state. */
fun ProgressBarDefinition<Unit>.build(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    status: ProgressState.Status = ProgressState.Status.NotStarted,
    speed: Double? = null,
    maker: ProgressBarWidgetMaker = MultiProgressBarWidgetMaker,
): Widget {
    return build(Unit, total, completed, displayedTime, status, speed, maker)
}

/**
 * Create a progress bar layout with that has a context of type [T].
 *
 * If you don't need a context, you can use [progressBarLayout] instead. If you need a builder
 * rather than a DSL, you can use [ProgressLayoutBuilder].
 *
 * @param spacing The number of spaces between cells in this layout.
 * @param alignColumns How to align the columns of the progress bar when multiple tasks are present.
 * If `true`, the cells in each column will have the same width. Widths are only aligned for
 * contiguous cells, so if you have a row with `alignColumns=false` between rows with
 * `alignColumns=true`, none of the three will be aligned.
 * @param textFps The default framerate for text based cells like `timeRemaining`
 * @param animationFps The default framerate for animation cells like `progressBar`
 * @param align The default horizontal alignment for cells
 * @param verticalAlign The default vertical alignment for cells
 * @param init A lambda that adds cells to the layout
 */
fun <T> progressBarContextLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    textFps: Int = TEXT_FPS,
    animationFps: Int = ANIMATION_FPS,
    align: TextAlign = TextAlign.RIGHT,
    verticalAlign: VerticalAlign = VerticalAlign.BOTTOM,
    init: ProgressLayoutScope<T>.() -> Unit,
): ProgressBarDefinition<T> {
    return ProgressLayoutBuilder<T>(textFps, animationFps, align, verticalAlign)
        .apply(init)
        .build(spacing, alignColumns)
}

/**
 * Create a progress bar layout that doesn't use a context.
 *
 * If you need a context, you can use [progressBarContextLayout] instead.
 *
 * @param spacing The number of spaces between cells in this layout.
 * @param alignColumns How to align the columns of the progress bar when multiple tasks are present.
 * If `true`, the cells in each column will have the same width. Width are only aligned for
 * contiguous cells, so if you have a row with `alignColumns=false` between rows with
 * `alignColumns=true`, none of the three will be aligned.
 * @param textFps The default framerate for text based cells like `timeRemaining`
 * @param animationFps The default framerate for animation cells like `progressBar`
 * @param align The default horizontal alignment for cells
 * @param verticalAlign The default vertical alignment for cells
 * @param init A lambda that adds cells to the layout
 */
fun progressBarLayout(
    spacing: Int = 2,
    alignColumns: Boolean = true,
    textFps: Int = TEXT_FPS,
    animationFps: Int = ANIMATION_FPS,
    align: TextAlign = TextAlign.RIGHT,
    verticalAlign: VerticalAlign = VerticalAlign.BOTTOM,
    init: ProgressLayoutScope<Unit>.() -> Unit,
): ProgressBarDefinition<Unit> {
    return progressBarContextLayout(
        spacing, alignColumns, textFps, animationFps, align, verticalAlign, init
    )
}

/**
 * A builder for creating a progress bar layout.
 *
 * If you don't want to use the [progressBarLayout] DSL, you can use this builder instead, and call
 * [build] when you're done adding cells.
 */
class ProgressLayoutBuilder<T>(
    override val textFps: Int = TEXT_FPS,
    override val animationFps: Int = ANIMATION_FPS,
    override val align: TextAlign = TextAlign.RIGHT,
    override val verticalAlign: VerticalAlign = VerticalAlign.BOTTOM,
) : ProgressLayoutScope<T> {
    private val cells: MutableList<ProgressBarCell<T>> = mutableListOf()

    override fun cell(
        width: ColumnWidth,
        fps: Int,
        align: TextAlign?,
        verticalAlign: VerticalAlign?,
        content: ProgressState<T>.() -> Widget,
    ) {
        cells += ProgressBarCell(
            width, fps, align ?: this.align, verticalAlign ?: this.verticalAlign, content
        )
    }

    /** Build the progress bar layout. */
    fun build(spacing: Int = 2, alignColumns: Boolean = true): ProgressBarDefinition<T> {
        return ProgressBarDefinition(cells, spacing, alignColumns)
    }
}
