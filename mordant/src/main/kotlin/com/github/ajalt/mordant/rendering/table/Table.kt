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

class Column(
        val cells: List<Cell>,
        val head: List<Cell>,
        val body: List<Cell>,
        val foot: List<Cell>,
        val width: ColumnWidth = ColumnWidth.Default
) {
    init {
        require(head.isNotEmpty() || body.isNotEmpty() || foot.isNotEmpty()) {
            "Column must not be empty"
        }
    }
}

// TODO: make most of these classes internal
class Table(
        val columns: List<Column>,
        val expand: Boolean = false,
        val borders: Borders? = Borders.SQUARE,
        val borderStyle: TextStyle = DEFAULT_STYLE
) : Renderable {
    init {
        require(columns.isNotEmpty()) { "Table cannot be empty" }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        if (expand) return WidthRange(width, width)
        val borderWidth = if (borders == null) 0 else columns.size + 1
        val remainingWidth = width - borderWidth
        val ranges = columns.map { measureColumn(it, t, remainingWidth) }

        return WidthRange(
                min = ranges.sumOf { it.min } + borderWidth,
                max = ranges.sumOf { it.max } + borderWidth
        )
    }

    private fun measureColumn(column: Column, t: Terminal, width: Int): WidthRange {
        if (column.width is ColumnWidth.Fixed) {
            return WidthRange(column.width.width, column.width.width)
        }

        return sequenceOf(column.head, column.body, column.foot)
                .flatten().map { it.content }.asIterable()
                .maxWidthRange(t, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val columnWidths = calculateColumnWidths(t, width)
        val renderedColumns = columns.map { column ->
            RenderedColumn(
                    renderCells(column.head, t, width),
                    renderCells(column.body, t, width),
                    renderCells(column.foot, t, width)
            )
        }
        val headerHeights = rowHeights(renderedColumns.map { it.header })
        val bodyHeights = rowHeights(renderedColumns.map { it.body })
        val footerHeights = rowHeights(renderedColumns.map { it.footer })


    }

    private fun renderCells(cells: List<Cell>, t: Terminal, width: Int): List<RenderedCell> {
        return cells.map {
            RenderedCell(
                    it.content.render(t, width).withStyle(it.style),
                    it.borderLeft,
                    it.borderTop,
                    it.borderRight,
                    it.borderBottom
            )
        }
    }

    private fun rowHeights(columns: List<List<RenderedCell>>): List<Int> {
        if (columns.isEmpty()) return emptyList()
        val heights = MutableList(columns[0].size) { 0 }
        for (x in columns.indices) {
            for (y in columns[0].indices) {
                heights[y] = maxOf(heights[y], columns[x][y].lines.size)
            }
        }
        return heights
    }

    private fun calculateColumnWidths(t: Terminal, width: Int): List<Int> {
        val borderWidth = if (borders == null) 0 else columns.size + 1
        val remainingWidth = width - borderWidth
        val maxWidths = columns.map { measureColumn(it, t, remainingWidth).max }
        return maxWidths // TODO: shrink
    }
}

private typealias MutableRenderedColumn = MutableList<Lines>

private class RenderedCell(
        val lines: Lines,
        val borderLeft: Boolean = true,
        val borderTop: Boolean = true,
        val borderRight: Boolean = true,
        val borderBottom: Boolean = true
)

private class RenderedColumn(
        val header: List<RenderedCell>,
        val body: List<RenderedCell>,
        val footer: List<RenderedCell>
)
