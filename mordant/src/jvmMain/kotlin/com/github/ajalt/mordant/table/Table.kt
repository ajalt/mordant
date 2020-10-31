package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.*

internal sealed class Cell {
    object Empty : Cell() {
        override val rowSpan: Int get() = 1
        override val columnSpan: Int get() = 1
        override val borderLeft: Boolean get() = false
        override val borderTop: Boolean get() = false
        override val borderRight: Boolean get() = false
        override val borderBottom: Boolean get() = false
    }

    data class SpanRef(
            val cell: Content,
            override val borderLeft: Boolean?,
            override val borderTop: Boolean?,
            override val borderRight: Boolean?,
            override val borderBottom: Boolean?,
    ) : Cell() {
        override val rowSpan: Int get() = cell.rowSpan
        override val columnSpan: Int get() = cell.columnSpan
    }

    data class Content(
            val content: Renderable,
            override val rowSpan: Int,
            override val columnSpan: Int,
            override val borderLeft: Boolean?,
            override val borderTop: Boolean?,
            override val borderRight: Boolean?,
            override val borderBottom: Boolean?,
            val style: TextStyle?,
            val textAlign: TextAlign,
            val verticalAlign: VerticalAlign,
    ) : Cell() {
        init {
            require(rowSpan > 0) { "rowSpan must be greater than 0" }
            require(columnSpan > 0) { "columnSpan must be greater than 0" }
        }
    }

    abstract val rowSpan: Int
    abstract val columnSpan: Int

    // True borders will draw a line, false will draw a space, null will not draw anything
    abstract val borderLeft: Boolean?
    abstract val borderTop: Boolean?
    abstract val borderRight: Boolean?
    abstract val borderBottom: Boolean?
}

internal class Table(
        val rows: List<ImmutableRow>,
        val borderStyle: BorderStyle,
        val borderTextStyle: TextStyle,
        val headerRowCount: Int,
        val footerRowCount: Int,
        val columnStyles: Map<Int, ColumnWidth>,
        val outerBorder: Boolean
) : Renderable {
    init {
        require(rows.isNotEmpty()) { "Table cannot be empty" }
    }

    private val expand = columnStyles.values.any { it is ColumnWidth.Expand }
    private val columnCount = rows.maxOf { it.size }
    private val rowBorders = List(rows.size + 1) { y ->
        (outerBorder || y in 1 until rows.size) && (0 until columnCount).any { x ->
            getCell(x, y)?.borderTop == true || getCell(x, y - 1)?.borderBottom == true
        }
    }
    private val columnBorders = List(columnCount + 1) { x ->
        (outerBorder || x in 1 until columnCount) && rows.indices.any { y ->
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
                borderStyle = borderStyle,
                borderTextStyle = borderTextStyle,
                headerRowCount = headerRowCount,
                footerRowCount = footerRowCount,
                columnCount = columnCount,
                columnWidths = calculateColumnWidths(t, width),
                columnBorders = columnBorders,
                rowBorders = rowBorders,
                t = t
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

    private fun calculateColumnWidths(t: Terminal, terminalWidth: Int): List<Int> {
        val availableWidth = terminalWidth - borderWidth
        if (availableWidth <= 0) return List(columnCount) { 0 }

        val measurements = List(columnCount) { measureColumn(it, t, availableWidth) }
        val widths = measurements.mapTo(mutableListOf()) { it.max }

        val fixedIdxs = columnStyles.mapNotNull { if (it.value is ColumnWidth.Fixed) it.key else null }
        val expandIdxs = columnStyles.mapNotNull { if (it.value is ColumnWidth.Expand) it.key else null }
        val autoIdxs = (0 until columnCount).mapNotNull { i ->
            if (columnStyles[i]?.let { it !is ColumnWidth.Auto } == true) null else i
        }

        val maxAutoWidth = autoIdxs.sumOf { measurements[it].max }
        val minAutoWidth = autoIdxs.sumOf { measurements[it].min }
        val maxFixedWidth = fixedIdxs.sumOf { measurements[it].max }
        val minExpandWidth = expandIdxs.sumOf { measurements[it].min }

        // Only shrink fixed columns if they can't fit
        val allocatedFixedWidth = minOf(maxFixedWidth, availableWidth)
        // Only shrink auto columns if shrinking is required to allow the flex columns to fit their
        // min. Never shrink auto columns below their min unless they wouldn't fit.
        val allocatedAutoWidth = (availableWidth - allocatedFixedWidth - minExpandWidth)
                .coerceIn(minAutoWidth, maxAutoWidth)
                .coerceAtMost(availableWidth - allocatedFixedWidth)
        // Expanding columns get whatever is left
        val allocatedExpandWidth = availableWidth - allocatedFixedWidth - allocatedAutoWidth

        fun setWeights(idxs: List<Int>, weights: List<Float>, allocatedWidth: Int, maxWidth: Int = -1) {
            if (weights.isEmpty() || allocatedWidth == maxWidth) return
            val distributedWidths = distributeWidths(weights, allocatedWidth)
            for ((i, w) in idxs.zip(distributedWidths)) {
                widths[i] = w
            }
        }

        setWeights(fixedIdxs, fixedIdxs.map { 1f }, allocatedFixedWidth, maxFixedWidth)
        setWeights(expandIdxs, expandIdxs.map { (columnStyles[it] as ColumnWidth.Expand).weight }, allocatedExpandWidth)
        // Setting a column's weight to its max width allows us to shrink the columns while
        // maintaining their relative widths
        setWeights(autoIdxs, autoIdxs.map { widths[it].toFloat() }, allocatedAutoWidth, maxAutoWidth)

        return widths
    }

    private fun distributeWidths(weights: List<Float>, totalWidth: Int): List<Int> {
        if (totalWidth == 0) return weights.map { 0 }

        val totalWeight = weights.sumOf { it.toDouble() }
        val widths = weights.mapTo(mutableListOf()) { weight ->
            (weight / totalWeight * totalWidth).toInt()
        }

        // distribute remainder left over from rounding down
        repeat(totalWidth - widths.sum()) { i ->
            widths[i] += 1
        }

        return widths
    }


    private fun getCell(x: Int, y: Int): Cell? {
        return rows.getOrNull(y)?.getOrNull(x)
    }
}

private class TableRenderer(
        val rows: List<ImmutableRow>,
        val borderStyle: BorderStyle,
        val borderTextStyle: TextStyle,
        val headerRowCount: Int,
        val footerRowCount: Int,
        val columnCount: Int,
        val columnWidths: List<Int>,
        val columnBorders: List<Boolean>,
        val rowBorders: List<Boolean>,
        val t: Terminal
) {
    private val rowCount get() = rows.size
    private val renderedRows = rows.map { r ->
        r.mapIndexed { x, it ->
            if (it is Cell.Content) {
                val w = (x until x + it.columnSpan).sumOf { columnWidths[it] }
                if (w == 0) EMPTY_LINES else it.content.render(t, w).withStyle(it.style)
            } else {
                EMPTY_LINES
            }
        }
    }
    private val rowHeights = renderedRows.mapIndexed { y, r ->
        r.withIndex().maxOfOrNull { (x, it) ->
            it.height / (cellAt(x, y)?.rowSpan ?: 1)
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

                tableLineY += drawTopBorderForCell(tableLineY, x, y, colWidth, cell.borderTop)
                drawCellContent(tableLineY, cell, x, y)

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
                getTopLeftCorner(x, rowCount)?.let { line.add(it) }
            }
            drawTopBorderForCell(tableLines.lastIndex, x, rowCount, columnWidths[x], borderTop = false)
        }

        // Bottom-right corner
        if (columnBorders[columnCount]) {
            getTopLeftCorner(columnCount, rowCount)?.let { line.add(it) }
        }
    }

    private fun drawCellContent(tableLineY: Int, cell: Cell, x: Int, y: Int) {
        val lines = renderCell(cell, x, y)
        for ((i, line) in lines.withIndex()) {
            tableLines[tableLineY + i].addAll(line)
        }
    }

    /** Return 1 if any cell in row [y] has a top border, or 0 if they don't */
    private fun drawTopBorderForCell(tableLineY: Int, x: Int, y: Int, colWidth: Int, borderTop: Boolean?): Int {
        if (!rowBorders[y]) return 0
        if (colWidth == 0 || borderTop == null) return 1

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

            if (rowBorders[y]) {
                getTopLeftCorner(x, y)?.let { tableLines[tableLineY].add(it) }
            }

            val borderLeft = cell.borderLeft
            val topBorderHeight = if (rowBorders[y]) 1 else 0

            if (borderLeft != null) {
                val border = when {
                    borderLeft || cellAt(x - 1, y)?.borderRight == true -> {
                        Span.word(sectionOfRow(y, allowBottom = false).ns, borderTextStyle)
                    }
                    else -> SINGLE_SPACE
                }
                for (i in 0 until rowHeight) {
                    tableLines[tableLineY + i + topBorderHeight].add(border)
                }
            }
            tableLineY += rowHeight + topBorderHeight
        }
    }

    private fun renderCell(cell: Cell, x: Int, y: Int): List<Line> {
        return when (cell) {
            is Cell.SpanRef -> {
                emptyList()
            }
            is Cell.Empty -> {
                val space = listOf(Span.space(columnWidths[x]))
                List(rowHeights[y]) { space }
            }
            is Cell.Content -> {
                val cellWidth = (x until x + cell.columnSpan).sumOf { columnWidths[it] } +
                        ((x + 1) until (x + cell.columnSpan)).count { columnBorders[it + 1] }
                val cellHeight = (y until y + cell.rowSpan).sumOf { rowHeights[it] } +
                        ((y + 1) until (y + cell.rowSpan)).count { rowBorders[it + 1] }
                cell.content.render(t, cellWidth)
                        .withStyle(cell.style)
                        .setSize(cellWidth, cellHeight, cell.verticalAlign, cell.textAlign)
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

        // If all the cells around this corner have null borders, we're in the middle of a span, so
        // don't draw anything.
        if ((tl != null || tr != null || bl != null || br != null) &&
                tl?.borderRight == null && tr?.borderLeft == null &&
                tr?.borderBottom == null && br?.borderTop == null &&
                bl?.borderRight == null && br?.borderLeft == null &&
                tl?.borderBottom == null && bl?.borderTop == null
        ) {
            return null
        }
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
