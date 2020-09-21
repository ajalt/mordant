package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*

sealed class Cell {
    object Empty : Cell() {
        override val borderLeft: Boolean get() = false
        override val borderTop: Boolean get() = false
        override val borderRight: Boolean get() = false
        override val borderBottom: Boolean get() = false
        override val rowSpan: Int get() = 1
        override val columnSpan: Int get() = 1
    }

    data class SpanRef(
            val cell: Content,
            override val borderLeft: Boolean,
            override val borderTop: Boolean,
            override val borderRight: Boolean,
            override val borderBottom: Boolean
    ) : Cell() {
        override val rowSpan: Int get() = cell.rowSpan
        override val columnSpan: Int get() = cell.columnSpan
    }

    data class Content(
            val content: Renderable,
            override val rowSpan: Int = 1,
            override val columnSpan: Int = 1,
            override val borderLeft: Boolean = true,
            override val borderTop: Boolean = true,
            override val borderRight: Boolean = true,
            override val borderBottom: Boolean = true,
            val style: TextStyle? = null
    ) : Cell() {
        init {
            require(rowSpan > 0) { "rowSpan must be greater than 0" }
            require(columnSpan > 0) { "columnSpan must be greater than 0" }
        }
    }

    abstract val rowSpan: Int
    abstract val columnSpan: Int
    abstract val borderLeft: Boolean
    abstract val borderTop: Boolean
    abstract val borderRight: Boolean
    abstract val borderBottom: Boolean
}

sealed class ColumnWidth {
    data class Fixed(val width: Int) : ColumnWidth()
    data class Weighted(val weight: Float) : ColumnWidth()
    object Default : ColumnWidth()
}

// TODO: make most of these classes internal
class Table(
        val rows: List<ImmutableRow>,
        val expand: Boolean,
        val borderStyle: BorderStyle,
        val borderTextStyle: TextStyle,
        val headerRowCount: Int,
        val footerRowCount: Int,
        val columnStyles: Map<Int, ColumnWidth>,
) : Renderable {
    init {
        require(rows.isNotEmpty()) { "Table cannot be empty" }
    }

    private val columnCount = rows.maxOf { it.size }

    override fun measure(t: Terminal, width: Int): WidthRange {
        if (expand) return WidthRange(width, width)
        val borderWidth = if (borderStyle == null) 0 else columnCount + 1
        val remainingWidth = width - borderWidth
        val ranges = List(columnCount) { measureColumn(it, t, remainingWidth) }

        return WidthRange(
                min = ranges.sumOf { it.min } + borderWidth,
                max = ranges.sumOf { it.max } + borderWidth
        )
    }

    override fun render(t: Terminal, width: Int): Lines {
        return TableRenderer(
                rows = rows,
                expand = expand,
                borderStyle = borderStyle,
                borderTextStyle = borderTextStyle,
                headerRowCount = headerRowCount,
                footerRowCount = footerRowCount,
                columnCount = columnCount,
                columnWidths = calculateColumnWidths(t, width),
                t = t,
                renderWidth = width
        ).render()
    }

    private fun measureColumn(x: Int, t: Terminal, width: Int): WidthRange {
        val columnWidth = columnStyles[x]
        if (columnWidth is ColumnWidth.Fixed) {
            return WidthRange(columnWidth.width, columnWidth.width)
        }

        return rows.maxWidthRange { row ->
            when (val cell = row.getOrNull(x)) {
                null -> null
                is Cell.Empty -> WidthRange(0, 0)
                is Cell.Content -> cell.content.measure(t, width) / cell.columnSpan
                is Cell.SpanRef -> cell.cell.content.measure(t, width) / cell.columnSpan
            }
        }
    }

    private fun calculateColumnWidths(t: Terminal, width: Int): List<Int> {
        val borderWidth = if (borderStyle == null) 0 else columnCount + 1
        val remainingWidth = width - borderWidth
        val maxWidths = List(columnCount) { measureColumn(it, t, remainingWidth).max }
        return maxWidths // TODO: shrink
    }
}


private class TableRenderer(
        val rows: List<ImmutableRow>,
        val expand: Boolean,
        val borderStyle: BorderStyle?,
        val borderTextStyle: TextStyle,
        val headerRowCount: Int,
        val footerRowCount: Int,
        val columnCount: Int,
        val columnWidths: List<Int>,
        val t: Terminal,
        val renderWidth: Int
) {
    private val rowCount get() = rows.size
    private val renderedRows = rows.map { r ->
        r.map {
            if (it is Cell.Content) {
                it.content.render(t, renderWidth * it.columnSpan + it.columnSpan).withStyle(it.style)
            } else {
                EMPTY_LINES
            }
        }
    }
    private val rowHeights = renderedRows.mapIndexed { y, r ->
        r.withIndex().maxOf { (x, it) ->
            it.size / (cellAt(x, y)?.rowSpan ?: 1)
        }.coerceAtLeast(1)
    }

    private val tableLines: MutableList<MutableList<Span>> =
            MutableList(rowHeights.sum() + rowCount + 1) { mutableListOf() }

    fun render(): Lines {
        // Render in column-major order so that we can append the lines of cells with row spans
        // directly, since all the leftward cells will have already been rendered.
        for (x in columnWidths.indices) {
            // Draw the left border for the entire column so that we don't have to worry about
            // corners in cells with row spans.
            drawLeftBorderForColumn(x)

            val colWidth = columnWidths[x]
            var tableLineY = 0
            for ((y, row) in rows.withIndex()) {
                val rowHeight = rowHeights[y]
                val cell = row.getOrNull(x) ?: Cell.Empty

                drawTopBorderForCell(tableLineY, cell.borderTop, x, y, colWidth)
                drawCellContent(tableLineY, cell, rowHeight, colWidth)

                tableLineY += rowHeight + 1
            }
        }

        // Draw the right border
        drawLeftBorderForColumn(columnCount)

        drawBottomBorder()
        return Lines(tableLines)
    }

    private fun drawBottomBorder() {
        val line = tableLines[tableLines.lastIndex]
        for (x in columnWidths.indices) {
            getTopLeftCorner(x, rowCount)?.let { line.add(it) }
            drawTopBorderForCell(tableLines.lastIndex, false, x, rowCount, columnWidths[x])
        }

        // Bottom-right corner
        getTopLeftCorner(columnCount, rowCount)?.let { line.add(it) }
    }

    private fun drawCellContent(tableLineY: Int, cell: Cell, rowHeight: Int, colWidth: Int) {
        val lines = renderCell(cell, rowHeight, colWidth)
        for ((i, line) in lines.withIndex()) {
            tableLines[tableLineY + i + 1].addAll(line)
        }
    }

    private fun drawTopBorderForCell(tableLineY: Int, borderTop: Boolean, x: Int, y: Int, colWidth: Int) {
        if (borderTop || cellAt(x, y - 1)?.borderBottom == true) {
            tableLines[tableLineY].add(Span.word("─".repeat(colWidth), borderTextStyle))
        }
    }

    private fun drawLeftBorderForColumn(x: Int) {
        var tableLineY = 0
        for ((y, row) in rows.withIndex()) {
            val rowHeight = rowHeights[y]
            val cell = row.getOrNull(x) ?: Cell.Empty
            getTopLeftCorner(x, y)?.let { tableLines[tableLineY].add(it) }
            if (cell.borderLeft || cellAt(x - 1, y)?.borderRight == true) {
                for (i in 0 until rowHeight) {
                    tableLines[tableLineY + i + 1].add(Span.word("│", borderTextStyle))
                }
            }
            tableLineY += rowHeight + 1
        }
    }

    private fun renderCell(cell: Cell, rowHeight: Int, colWidth: Int): List<List<Span>> {
        return when (cell) {
            is Cell.SpanRef -> {
                emptyList()
            }
            is Cell.Empty -> {
                List(rowHeight) { listOf(Span.space(colWidth)) }
            }
            is Cell.Content -> {
                val cellHeight = colWidth * cell.columnSpan + cell.columnSpan - 1
                val cellWidth = rowHeight * cell.rowSpan + cell.rowSpan - 1
                cell.content.render(t, renderWidth)
                        .withStyle(cell.style)
                        .setSize(cellHeight, cellWidth)
                        .lines
            }
        }
    }

    private fun cellAt(x: Int, y: Int): Cell? = rows.getOrNull(y)?.getOrNull(x)

    private fun getTopLeftCorner(x: Int, y: Int): Span? {
        val tl = cellAt(x - 1, y - 1)
        val tr = cellAt(x, y - 1)
        val bl = cellAt(x - 1, y)
        val br = cellAt(x, y)
        return getCorner(
                tl?.borderRight == true || tr?.borderLeft == true,
                tr?.borderBottom == true || br?.borderTop == true,
                bl?.borderRight == true || br?.borderLeft == true,
                tl?.borderBottom == true || bl?.borderTop == true,
        )
    }

    private fun getCorner(n: Boolean, e: Boolean, s: Boolean, w: Boolean): Span? {
        val char = when {
            !n && e && s && !w -> "┌"
            !n && e && s && w -> "┬"
            !n && !e && s && w -> "┐"
            n && e && s && !w -> "├"
            n && e && s && w -> "┼"
            n && !e && s && w -> "┤"
            n && e && !s && !w -> "└"
            n && e && !s && w -> "┴"
            n && !e && !s && w -> "┘"
            !n && e && !s && w -> "─"
            n && !e && s && !w -> "│"
            n && !e && !s && !w -> "╷"
            !n && e && !s && !w -> "╴"
            !n && !e && s && !w -> "╵"
            !n && !e && !s && w -> "╶"
            !n && !e && !s && !w -> return null
            else -> error("impossible corner: n=$n $e=e s=$s w=$w")
        }

        return Span.word(char, borderTextStyle)
    }
}


fun main() {
    val t = Terminal()
    val table = table {
        body {
            row {
                cell("2x2") {
                    rowSpan = 2
                    columnSpan = 2
                }

            }
            row {
                cell(3) {
                    rowSpan = 2
                    columnSpan = 2
                }
            }
            row {
                cells(5, 6, 7) {
                    rowSpan = 2
                    columnSpan = 2
                }
            }
        }
    }
    t.print(table)
}

