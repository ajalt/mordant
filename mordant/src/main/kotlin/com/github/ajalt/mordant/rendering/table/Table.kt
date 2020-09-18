package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*

data class Cell(
        val content: Renderable,
        val rowSpan: Int = 1,
        val columnSpan: Int = 1,
        val borderLeft: Boolean = true,
        val borderTop: Boolean = true,
        val borderRight: Boolean = true,
        val borderBottom: Boolean = true,
        val style: TextStyle? = null
) {
    init {
        require(rowSpan > 0) { "rowSpan must be greater than 0" }
        require(columnSpan > 0) { "columnSpan must be greater than 0" }
    }
}

sealed class ColumnWidth {
    data class Fixed(val width: Int) : ColumnWidth()
    data class Weighted(val weight: Float) : ColumnWidth()
    object Default : ColumnWidth()
}

// TODO: make most of these classes internal
class Table(
        val rows: List<ImmutableRow>,
        val expand: Boolean = false,
        val borders: Borders? = Borders.SQUARE,
        val borderStyle: TextStyle = DEFAULT_STYLE,
        val headerRowCount: Int = 0,
        val footerRowCount: Int = 0,
        val columnWidths: Map<Int, ColumnWidth> = emptyMap()
) : Renderable {
    init {
        require(rows.isNotEmpty()) { "Table cannot be empty" }
    }

    private val columnCount = rows.maxOf { it.size }

    override fun measure(t: Terminal, width: Int): WidthRange {
        if (expand) return WidthRange(width, width)
        val borderWidth = if (borders == null) 0 else columnCount + 1
        val remainingWidth = width - borderWidth
        val ranges = List(columnCount) { measureColumn(it, t, remainingWidth) }

        return WidthRange(
                min = ranges.sumOf { it.min } + borderWidth,
                max = ranges.sumOf { it.max } + borderWidth
        )
    }

    private fun measureColumn(x: Int, t: Terminal, width: Int): WidthRange {
        val columnWidth = columnWidths[x]
        if (columnWidth is ColumnWidth.Fixed) {
            return WidthRange(columnWidth.width, columnWidth.width)
        }

        return rows.mapNotNull { it.getOrNull(x)?.content }.maxWidthRange(t, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val columnWidths = calculateColumnWidths(t, width)
        val renderedRows = rows.map { r -> r.map { it.content.render(t, width).withStyle(it.style) } }
        val rowHeights = renderedRows.map { r -> r.maxOf { it.size } }
        val tableLines: MutableList<MutableList<Span>> = MutableList(rowHeights.sum() + rowHeights.size + 1) { mutableListOf() }

        // Render in column-major order so that we can append the lines of cells with row spans
        // directly, since all the leftward cells have already been rendered.
        for (x in columnWidths.indices) {
            val colWidth = columnWidths[x]
            var tableLineY = 0
            for ((y, row) in rows.withIndex()) {
                val rowHeight = rowHeights[y]
                val cell = row.getOrNull(x)
                if (cell == null) {
                    // table is jagged on the right, this row is done
                    tableLineY += rowHeight + 1
                    continue
                }

                tableLines[tableLineY].add(Span.word("+" + "-".repeat(colWidth)))

                val lines = cell.content.render(t, width).withStyle(cell.style).setSize(colWidth, rowHeight).lines

                for ((i, line) in lines.withIndex()) {
                    tableLines[tableLineY + i + 1].add(Span.word("|"))
                    tableLines[tableLineY + i + 1].addAll(line)
                }

                if (x == row.lastIndex) {
                    tableLines[tableLineY].add(Span.word("+"))
                    for (i in lines.indices) {
                        tableLines[tableLineY + i + 1].add(Span.word("|"))
                    }
                }

                tableLineY += lines.size + 1

                if (y == rows.lastIndex) {
                    tableLines[tableLineY].add(Span.word("+" + "-".repeat(colWidth)))
                    if (x == columnWidths.lastIndex) {
                        tableLines[tableLineY].add(Span.word("+"))
                    }
                    tableLineY = 0
                }
            }
        }
        return Lines(tableLines)
    }

    private fun calculateColumnWidths(t: Terminal, width: Int): List<Int> {
        val borderWidth = if (borders == null) 0 else columnCount + 1
        val remainingWidth = width - borderWidth
        val maxWidths = List(columnCount) { measureColumn(it, t, remainingWidth).max }
        return maxWidths // TODO: shrink
    }
}


fun main() {
    val t = Terminal()
    val table = table {
        body {
            row {
                cell("1")
                cell("tall") {
                    rowSpan = 2
                }
            }
            row("4", "5")
        }
    }
    t.print(table)
}

