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
    private val rowBorders = List(rows.size + 1) { y ->
        (0 until columnCount).any { x ->
            getCell(x, y)?.borderTop == true || getCell(x, y - 1)?.borderBottom == true
        }
    }
    private val columnBorders = List(columnCount + 1) { x ->
        rows.indices.any { y ->
            getCell(x, y)?.borderLeft == true || getCell(x - 1, y)?.borderRight == true
        }
    }
    private val borderWidth = columnBorders.count { it }


    override fun measure(t: Terminal, width: Int): WidthRange {
        if (expand) return WidthRange(width, width)
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
                columnBorders = columnBorders,
                rowBorders = rowBorders,
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
        val remainingWidth = width - borderWidth
        return List(columnCount) { measureColumn(it, t, remainingWidth).max } // TODO: shrink
    }

    private fun getCell(x: Int, y: Int): Cell? {
        return rows.getOrNull(y)?.getOrNull(x)
    }
}


private class TableRenderer(
        val rows: List<ImmutableRow>,
        val expand: Boolean,
        val borderStyle: BorderStyle,
        val borderTextStyle: TextStyle,
        val headerRowCount: Int,
        val footerRowCount: Int,
        val columnCount: Int,
        val columnWidths: List<Int>,
        val columnBorders: List<Boolean>,
        val rowBorders: List<Boolean>,
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
        r.withIndex().maxOfOrNull { (x, it) ->
            it.size / (cellAt(x, y)?.rowSpan ?: 1)
        }?.coerceAtLeast(1) ?: 1
    }

    private val tableLines: MutableList<MutableList<Span>> =
            MutableList(rowHeights.sum() + rowBorders.count { it }) { mutableListOf() }

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

                tableLineY +=  drawTopBorderForCell(tableLineY, x, y, cell.borderTop, colWidth)
                drawCellContent(tableLineY, cell, rowHeight, colWidth)

                tableLineY += rowHeight
            }
        }

        // Draw the right border
        drawLeftBorderForColumn(columnCount)

        drawBottomBorder()
        return Lines(tableLines)
    }

    private fun drawBottomBorder() {
        if (!rowBorders[rowCount]) return

        val line = tableLines[tableLines.lastIndex]
        for (x in columnWidths.indices) {
            if (columnBorders[x]) {
                line.add(getTopLeftCorner(x, rowCount))
            }
            drawTopBorderForCell(tableLines.lastIndex, x, rowCount, false, columnWidths[x])
        }

        // Bottom-right corner
        if (columnBorders[columnCount]) {
            line.add(getTopLeftCorner(columnCount, rowCount))
        }
    }

    private fun drawCellContent(tableLineY: Int, cell: Cell, rowHeight: Int, colWidth: Int) {
        val lines = renderCell(cell, rowHeight, colWidth)
        for ((i, line) in lines.withIndex()) {
            tableLines[tableLineY + i].addAll(line)
        }
    }

    private fun drawTopBorderForCell(tableLineY: Int, x: Int, y: Int, borderTop: Boolean, colWidth: Int): Int {
        if (!rowBorders[y]) return 0

        val char = if (borderTop || cellAt(x, y - 1)?.borderBottom == true) sectionOfRow(y).ew else " "
        tableLines[tableLineY].add(Span.word(char.repeat(colWidth), borderTextStyle))
        return 1
    }

    private fun drawLeftBorderForColumn(x: Int) {
        if (!columnBorders[x]) return

        var tableLineY = 0
        for ((y, row) in rows.withIndex()) {
            val rowHeight = rowHeights[y]
            val cell = row.getOrNull(x) ?: Cell.Empty

            val borderHeight = when {
                rowBorders[y] -> {
                    tableLines[tableLineY].add(getTopLeftCorner(x, y))
                    1
                }
                else -> 0
            }

            val border = when {
                cell.borderLeft || cellAt(x - 1, y)?.borderRight == true -> {
                    Span.word(sectionOfRow(y, allowBottom = false).ns, borderTextStyle)
                }
                else -> SINGLE_SPACE
            }
            for (i in 0 until rowHeight) {
                tableLines[tableLineY + i + borderHeight].add(border)
            }
            tableLineY += rowHeight + borderHeight
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

    private fun getTopLeftCorner(x: Int, y: Int): Span {
        val tl = cellAt(x - 1, y - 1)
        val tr = cellAt(x, y - 1)
        val bl = cellAt(x - 1, y)
        val br = cellAt(x, y)
        return sectionOfRow(y).getCorner(
                tl?.borderRight == true || tr?.borderLeft == true,
                tr?.borderBottom == true || br?.borderTop == true,
                bl?.borderRight == true || br?.borderLeft == true,
                tl?.borderBottom == true || bl?.borderTop == true,
                borderTextStyle
        )
    }

    private fun sectionOfRow(y: Int, allowBottom: Boolean = true): BorderStyleSection {
        return when {
            y < headerRowCount -> borderStyle.head
            allowBottom && headerRowCount > 0 && y == headerRowCount -> borderStyle.headBottom
            allowBottom && footerRowCount > 0 && y == rowCount - footerRowCount -> borderStyle.bodyBottom
            footerRowCount == 0 || y < rowCount - footerRowCount -> borderStyle.body
            else -> borderStyle.foot
        }
    }
}
