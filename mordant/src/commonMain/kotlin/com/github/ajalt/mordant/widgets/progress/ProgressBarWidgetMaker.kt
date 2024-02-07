package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Padding
import kotlin.math.max

interface ProgressBarWidgetMaker {
    /**
     * Build a progress widget with the given [rows].
     */
    fun <T> build(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): Widget

    /**
     * Build the widgets for each cell in the progress bar.
     *
     * This can be used if you want to manually include the individual cells in a layout like
     * a table.
     *
     * @return A list of rows, where each row is a list of widgets for the cells in that row.
     */
    fun <T> buildCells(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): List<List<Widget>>
}

object MultiProgressBarWidgetMaker : ProgressBarWidgetMaker {
    override fun <T> build(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): Widget {
        return when {
            rows.isEmpty() -> EmptyWidget
            rows.count { (d, _) -> d.alignColumns } > 1 -> makeTable(rows)
            else -> makeVerticalLayout(rows)
        }
    }

    override fun <T> buildCells(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): List<List<Widget>> {
        return rows.map { (definition, state) ->
            definition.cells.map { cell -> cell.content(state) }
        }
    }

    private fun <T> makeVerticalLayout(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): Widget {
        return when {
            rows.size == 1 -> makeHorizontalLayout(rows[0].first, rows[0].second)
            else -> verticalLayout {
                for ((d, state) in rows) {
                    cell(makeHorizontalLayout(d, state))
                }
            }
        }
    }

    private fun <T> makeHorizontalLayout(
        d: ProgressBarDefinition<T>, state: ProgressState<T>,
    ): Widget = horizontalLayout {
        spacing = d.spacing
        for ((i, barCell) in d.cells.withIndex()) {
            cell(barCell.content(state))
            column(i) {
                width = barCell.columnWidth
                align = barCell.align
                verticalAlign = barCell.verticalAlign
            }
        }
    }

    private fun <T> makeTable(
        rows: List<Pair<ProgressBarDefinition<T>, ProgressState<T>>>,
    ): Table {
        return table {
            addPaddingWidthToFixedWidth = true
            cellBorders = Borders.NONE
            val columnCount = rows.maxOf { (d, _) -> if (d.alignColumns) d.cells.size else 0 }
            for (i in 0..<columnCount) {
                column(i) {
                    for ((j, row) in rows.withIndex()) {
                        val (d, _) = row
                        if (!d.alignColumns) continue // only aligned rows affect the column
                        val w1 = width.toCustom()
                        val w2 = d.cells.getOrNull(i)?.columnWidth?.toCustom() ?: continue
                        width = when {
                            w1.isExpand || w2.isExpand -> {
                                ColumnWidth.Expand(nullMax(w1.expandWeight, w2.expandWeight) ?: 1f)
                            }
                            // If we had a MinWidth type, we'd use it here instead of Auto
                            (j > 0 && w1.isAuto) || w2.isAuto -> ColumnWidth.Auto
                            else -> ColumnWidth.Fixed(nullMax(w1.width, w2.width)!!)
                        }
                    }
                }
            }
            body {
                for ((d, state) in rows) {
                    row {
                        if (d.alignColumns) {
                            for ((i, c) in d.cells.withIndex()) {
                                cell(c.content(state)) {
                                    align = c.align
                                    verticalAlign = c.verticalAlign
                                    padding = Padding { left = if (i == 0) 0 else d.spacing }
                                }
                            }
                        } else {
                            cell(makeHorizontalLayout(d, state)) {
                                columnSpan = Int.MAX_VALUE
                            }
                        }
                    }
                }
            }
        }
    }
}

fun <T> ProgressBarWidgetMaker.build(
    vararg rows: Pair<ProgressBarDefinition<T>, ProgressState<T>>,
): Widget {
    return build(rows.asList())
}

fun <T> ProgressBarWidgetMaker.buildCells(
    vararg rows: Pair<ProgressBarDefinition<T>, ProgressState<T>>,
): List<List<Widget>> {
    return buildCells(rows.asList())
}

private fun nullMax(a: Int?, b: Int?): Int? =
    if (a == null) b else if (b == null) a else max(a, b)

private fun nullMax(a: Float?, b: Float?): Float? =
    if (a == null) b else if (b == null) a else max(a, b)
