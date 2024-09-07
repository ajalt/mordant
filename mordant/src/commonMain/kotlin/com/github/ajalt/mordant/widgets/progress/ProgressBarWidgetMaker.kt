package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import com.github.ajalt.mordant.table.ColumnWidth.Companion.Auto
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Padding
import kotlin.math.max

data class ProgressBarMakerRow<T>(
    val definition: ProgressBarDefinition<T>,
    val state: ProgressState<T>,
)

/**
 * Instances of this interface creates widgets for progress bars.
 *
 * The default implementation is [MultiProgressBarWidgetMaker].
 */
interface ProgressBarWidgetMaker {
    /**
     * Build a progress widget with the given [rows].
     */
    fun build(rows: List<ProgressBarMakerRow<*>>): Widget
}

/**
 * The default implementation of [ProgressBarWidgetMaker].
 *
 * It uses a [grid] or [verticalLayout] to render the progress bars.
 */
object MultiProgressBarWidgetMaker : ProgressBarWidgetMaker {
    override fun build(rows: List<ProgressBarMakerRow<*>>): Widget {
        return when {
            rows.isEmpty() -> EmptyWidget
            rows.size == 1 -> makeHorizontalLayout(rows[0])
            rows.all { it.definition.alignColumns } -> makeTable(rows)
            else -> makeVerticalLayout(rows)
        }
    }

    /**
     * Build the widgets for each cell in the progress bar.
     *
     * This can be used if you want to manually include the individual cells in a layout like
     * a table.
     *
     * @return A list of rows, where each row is a list of widgets for the cells in that row.
     */
    fun buildCells(rows: List<ProgressBarMakerRow<*>>): List<List<Widget>> {
        return rows.map { row -> renderRow(row).map { it.second } }
    }

    private fun makeVerticalLayout(rows: List<ProgressBarMakerRow<*>>): Widget {
        return verticalLayout {
            align = TextAlign.LEFT // LEFT instead of NONE so that the widget isn't jagged
            var alignStart = -1
            for ((i, row) in rows.withIndex()) {
                // render contiguous aligned rows as a table.
                // we can't just throw the whole thing in one table, since the unaligned rows will
                // mess up the column widths for the aligned rows since spanned columns have their
                // width divided evenly between the columns they span
                if (!row.definition.alignColumns) {
                    if (alignStart >= 0) {
                        cell(makeTable(rows.subList(alignStart, i)))
                        alignStart = -1
                    }
                    cell(makeHorizontalLayout(row))
                } else if (alignStart < 0) {
                    alignStart = i
                }
            }
            if (alignStart >= 0) {
                cell(makeTable(rows.subList(alignStart, rows.size)))
            }
        }
    }

    private fun <T> makeHorizontalLayout(
        row: ProgressBarMakerRow<T>,
    ): Widget = horizontalLayout {
        spacing = row.definition.spacing
        for ((i, barCell) in row.definition.cells.withIndex()) {
            cell(barCell.content(row.state))
            column(i) {
                width = barCell.columnWidth
                align = barCell.align
                verticalAlign = barCell.verticalAlign
            }
        }
    }

    private fun makeTable(rows: List<ProgressBarMakerRow<*>>): Widget {
        return grid {
            addPaddingWidthToFixedWidth = true
            cellBorders = Borders.NONE
            val columnCount = rows.maxOf { (d, _) -> if (d.alignColumns) d.cells.size else 0 }
            for (i in 0..<columnCount) {
                column(i) {
                    for ((j, row) in rows.withIndex()) {
                        val (d, _) = row
                        if (!d.alignColumns) continue // only aligned rows affect the column
                        val w1 = width
                        val w2 = d.cells.getOrNull(i)?.columnWidth ?: continue
                        width = when {
                            w1.isExpand || w2.isExpand -> {
                                ColumnWidth.Expand(nullMax(w1.expandWeight, w2.expandWeight) ?: 1f)
                            }
                            // If we had a MinWidth type, we'd use it here instead of Auto
                            (j > 0 && w1.isAuto) || w2.isAuto -> Auto
                            else -> ColumnWidth.Fixed(nullMax(w1.width, w2.width)!!)
                        }
                    }
                }
            }
            for (r in rows) {
                row {
                    if (r.definition.alignColumns) {
                        for ((i, c) in renderRow(r).withIndex()) {
                            cell(c.second) {
                                align = c.first.align
                                verticalAlign = c.first.verticalAlign
                                padding = Padding { left = if (i == 0) 0 else r.definition.spacing }
                            }
                        }
                    } else {
                        cell(makeHorizontalLayout(r)) {
                            columnSpan = Int.MAX_VALUE
                        }
                    }
                }
            }
        }
    }

    private fun <T> renderRow(row: ProgressBarMakerRow<T>): List<Pair<ProgressBarCell<T>, Widget>> {
        return row.definition.cells.map { cell -> cell to cell.content(row.state) }
    }
}

/**
 * Build a progress widget with the given [rows].
 */
fun ProgressBarWidgetMaker.build(vararg rows: ProgressBarMakerRow<*>): Widget {
    return build(rows.asList())
}

/**
 * Build a progress widget with the given [rows].
 */
fun <T> ProgressBarWidgetMaker.build(
    vararg rows: Pair<ProgressBarDefinition<T>, ProgressState<T>>,
): Widget {
    return build(rows.map { ProgressBarMakerRow(it.first, it.second) })
}

private fun nullMax(a: Int?, b: Int?): Int? =
    if (a == null) b else if (b == null) a else max(a, b)

private fun nullMax(a: Float?, b: Float?): Float? =
    if (a == null) b else if (b == null) a else max(a, b)
