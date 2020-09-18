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
        val tableLines = mutableListOf<MutableList<Span>>()

        for (row in rows) {
            val renderedCells = row.map { it.content.render(t, width).withStyle(it.style) }
            val maxHeight = renderedCells.maxOf { it.size }
            // TODO: borders
            val rowLines = MutableList(maxHeight + 1) { mutableListOf<Span>() }
            for ((x, cell) in row.withIndex()) {
                val colWidth = columnWidths[x]
                val lines = renderedCells[x].setSize(colWidth, maxHeight)
                rowLines[0].add(Span.word("+" + "-".repeat(colWidth)))
                for ((i, line) in lines.lines.withIndex()) {
                    rowLines[i + 1].add(Span.word("|"))
                    rowLines[i + 1].addAll(line)
                }
            }
            rowLines[0].add(Span.word("+"))
            for (i in 1..rowLines.lastIndex) {
                rowLines[i].add(Span.word("|"))
            }

            tableLines.addAll(rowLines)
        }

        tableLines.add(mutableListOf(Borders.ASCII.renderBottom(columnWidths)))
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
                cell(Panel(Text("2 2 2")))
            }
            row("4", "5")
        }
    }
    t.print(table)
}
