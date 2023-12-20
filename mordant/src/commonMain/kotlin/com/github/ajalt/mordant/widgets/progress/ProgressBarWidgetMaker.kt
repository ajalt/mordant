package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Padding

interface ProgressBarWidgetMaker {
    fun <T> build(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget
}

object BaseProgressBarWidgetMaker : ProgressBarWidgetMaker {
    override fun <T> build(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget {
        return when {
            states.isEmpty() -> EmptyWidget
            definition.alignColumns -> makeTable(definition, states)
            else -> makeLinearLayout(definition, states)
        }
    }

    private fun <T> makeLinearLayout(
        d: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget {
        return verticalLayout {
            for (state in states) {
                cell(horizontalLayout {
                    this.spacing = d.spacing
                    d.cells.forEachIndexed { i, cell ->
                        column(i) {
                            align = cell.align
                            width = cell.columnWidth
                        }
                    }
                    cellsFrom(makeWidgets(d.cells, state))
                })
            }
        }
    }

    private fun <T> makeTable(d: ProgressBarDefinition<T>, states: List<ProgressState<T>>): Table {
        return table {
            //TODO  verticalAlign = verticalAlign
            cellBorders = Borders.NONE
            padding = Padding { left = d.spacing }
            column(0) { padding = Padding(0) }
            d.cells.forEachIndexed { i, cell ->
                column(i) {
                    align = cell.align
                    val w = cell.columnWidth
                    width = when {
                        // The fixed width cells don't include padding, so add it here
                        i > 0 && w is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + d.spacing)
                        else -> w
                    }
                }
            }
            body {
                for (state in states) {
                    rowFrom(makeWidgets(d.cells, state))
                }
            }
        }
    }

    private fun <T> makeWidgets(
        cells: List<ProgressBarCell<T>>,
        state: ProgressState<T>,
    ): List<Widget> {
        return cells.map { it.content(state) }
    }
}
