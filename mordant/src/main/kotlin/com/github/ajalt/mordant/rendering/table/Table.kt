package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*

sealed class Cell {
    object Empty : Cell() {
        override val borderLeft: Boolean get() = false
        override val borderTop: Boolean get() = false
        override val borderRight: Boolean get() = false
        override val borderBottom: Boolean get() = false
    }

    data class SpanRef(
            val cell: Content,
            override val borderTop: Boolean,
            override val borderRight: Boolean,
            override val borderBottom: Boolean
    ) : Cell() {
        val rowSpan: Int get() = cell.rowSpan
        val columnSpan: Int get() = cell.columnSpan
        override val borderLeft: Boolean get() = false // always drawn by [cell]
    }

    data class Content(
            val content: Renderable,
            val rowSpan: Int = 1,
            val columnSpan: Int = 1,
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

        return rows.maxWidthRange { row ->
            when (val cell = row.getOrNull(x)) {
                null -> null
                is Cell.Empty -> WidthRange(0, 0)
                is Cell.Content -> cell.content.measure(t, width) / cell.columnSpan
                is Cell.SpanRef -> cell.cell.content.measure(t, width) / cell.columnSpan
            }
        }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val columnWidths = calculateColumnWidths(t, width)
        val renderedRows = rows.map { r ->
            r.map {
                if (it is Cell.Content) {
                    it.content.render(t, width * it.columnSpan + it.columnSpan).withStyle(it.style)
                } else {
                    EMPTY_LINES
                }
            }
        }
        val rowHeights = renderedRows.map { r -> r.maxOf { it.size } }
        val tableLines: MutableList<MutableList<Span>> = MutableList(rowHeights.sum() + rows.size + 1) { mutableListOf() }


        // Render in column-major order so that we can append the lines of cells with row spans
        // directly, since all the leftward cells have already been rendered.
        for (x in columnWidths.indices) {
            val colWidth = columnWidths[x]
            var tableLineY = 0
            for ((y, row) in rows.withIndex()) {
                val rowHeight = rowHeights[y]
                val cell = row.getOrNull(x) ?: Cell.Empty


                // Top border
                if (cell !is Cell.SpanRef && (cell.borderTop || cellAt(x, y - 1)?.borderBottom != false)) {
                    tableLines[tableLineY].add(getTopLeftCorner(x, y))
                    tableLines[tableLineY].add(Span.word("─".repeat(colWidth)))
                }

                val lines = renderCell(cell, rowHeight, colWidth, t, width)
                for ((i, line) in lines.withIndex()) {
                    // Left border
                    if (cell.borderLeft || cellAt(x - 1, y)?.borderRight != false) {
                        tableLines[tableLineY + i + 1].add(Span.word("|"))
                    }
                    // Content
                    tableLines[tableLineY + i + 1].addAll(line)
                }

                // Right border, if this is the last cell in the row
                if (x == row.lastIndex) {
                    tableLines[tableLineY].add(getTopLeftCorner(x + 1, y))
                    if (cell.borderRight) {
                        for (i in 0 until rowHeight) {
                            tableLines[tableLineY + i + 1].add(Span.word("|"))
                        }
                    }
                }

                tableLineY += rowHeight + 1
            }
        }

        // Bottom borders
        val line = mutableListOf<Span>()
        for ((x, colWidth) in columnWidths.withIndex()) {
            line.add(getTopLeftCorner(x, rows.size))
            val border = when (cellAt(x, rows.lastIndex)?.borderBottom) {
                true -> {
                    "─"
                }
                else -> " "
            }
            line.add(Span.word(border.repeat(colWidth)))

        }
        tableLines[tableLines.lastIndex] = line
        return Lines(tableLines)
    }

    private fun renderCell(cell: Cell, rowHeight: Int, colWidth: Int, t: Terminal, width: Int): List<List<Span>> {
        return when (cell) {
            is Cell.SpanRef -> {
                emptyList()
            }
            is Cell.Empty -> {
                List(rowHeight) { listOf(Span.space(colWidth)) }
            }
            is Cell.Content -> {
                cell.content.render(t, width)
                        .withStyle(cell.style)
                        .setSize(colWidth * cell.columnSpan + cell.columnSpan - 1, rowHeight * cell.rowSpan + cell.rowSpan - 1)
                        .lines
            }
        }
    }

    private fun cellAt(x: Int, y: Int): Cell? = rows.getOrNull(y)?.getOrNull(x)

    private fun calculateColumnWidths(t: Terminal, width: Int): List<Int> {
        val borderWidth = if (borders == null) 0 else columnCount + 1
        val remainingWidth = width - borderWidth
        val maxWidths = List(columnCount) { measureColumn(it, t, remainingWidth).max }
        return maxWidths // TODO: shrink
    }

    private fun getTopLeftCorner(x: Int, y: Int): Span {
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

    private fun getCorner(n: Boolean, e: Boolean, s: Boolean, w: Boolean): Span {
        val char = when {
            !n && e && s && !w -> "┌"
            !n && e && s && w -> "┬"
            !n && !e && s && w -> "┐"
            n && e && s && !w -> "├"
            n && e && s && w -> "┼"
            n && !e && s && w -> {
                "┤"
            }
            n && e && !s && !w -> "└"
            n && e && !s && w -> "┴"
            n && !e && !s && w -> "┘"
            !n && e && !s && w -> "─"
            n && !e && s && !w -> "│"
            !n && !e && !s && !w -> " "
            else -> error("impossible corner: n=$n $e=e s=$s w=$w")
        }

        return Span.word(char, borderStyle)
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
            row("4")
        }
    }
    t.print(table)
}

