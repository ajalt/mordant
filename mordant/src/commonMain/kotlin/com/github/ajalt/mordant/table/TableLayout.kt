package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.ColumnWidth.Companion.Auto
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.withPadding

internal typealias ImmutableRow = List<Cell>
internal typealias MutableRow = MutableList<Cell>


/**
 * Takes a table builder and computes the locations, spans, and styles of all [Cell]s.
 * Returns a [TableImpl] that will compute the row and column sizes and draw the table.
 */
internal class TableLayout(private val table: TableBuilderInstance) {
    fun buildTable(): TableImpl {
        val sections = listOf(table.headerSection, table.bodySection, table.footerSection)
        val widestExplicitRow = sections.maxOf { section ->
            section.rows.maxOfOrNull { it.cells.size } ?: 0
        }
        val occupied = sections.map { occupiedColumns(it) }
        val tableWidth = occupied.maxOf { it.maxOrNull() ?: 0 }
        val (header, body, footer) = sections.map { section ->
            // Keep the room supplied by explicit rows. A row span can establish additional
            // columns, but a continuing cell already occupies its slot in the current row.
            val explicit = IntArray(section.rows.size) { y -> widestExplicitRow - section.rows[y].cells.size }
            val baseline = requireNotNull(buildSection(section, explicit)) {
                "Invalid table: cell spans cannot overlap"
            }
            // Reserve every baseline span, including those belonging to later cells.
            // Only the remaining real columns can be spent on additional width.
            val available = IntArray(section.rows.size) { y ->
                (tableWidth - baseline[y].count { it !== Cell.Empty }).coerceAtLeast(0)
            }
            // Widening a cell in a sparse row may collide with an earlier row span.
            // Retain the narrower layout when those cells fit with the original truncation.
            val hasExtra = available.any { it > 0 }
            val rows = if (hasExtra) buildSection(section, explicit, available) else null
            rows ?: baseline
        }
        val rows = listOf(header, body, footer).flatten().ifEmpty {
            val b = table.tableBorders ?: Borders.ALL
            val cell = Cell.Content(
                content = EmptyWidget,
                rowSpan = 1,
                columnSpan = 1,
                borderLeft = b.left,
                borderTop = b.top,
                borderRight = b.right,
                borderBottom = b.bottom,
                style = null,
                textAlign = TextAlign.LEFT,
                verticalAlign = VerticalAlign.TOP,
                paddingWidth = 0,
            )
            listOf(listOf(cell))
        }
        val columnCount = rows.maxOf { it.size }

        val columnWidths = List(columnCount) { i ->
            table.columns[i]?.width ?: Auto
        }
        return TableImpl(
            rows = rows,
            borderType = table.borderType,
            borderStyle = table.borderStyle,
            headerRowCount = header.size,
            footerRowCount = footer.size,
            columnWidths = columnWidths,
            tableBorders = table.tableBorders,
            addPaddingWidthToFixedWidth = table.addPaddingWidthToFixedWidth,
        )
    }

    private fun occupiedColumns(section: SectionBuilderInstance): IntArray {
        // Each row needs room for its own cells and cells continued from earlier rows.
        val ending = IntArray(section.rows.size + 1)
        var carried = 0
        val widths = IntArray(section.rows.size)
        for ((y, row) in section.rows.withIndex()) {
            carried -= ending[y]
            widths[y] = carried + row.cells.size
            for (cell in row.cells) {
                if (cell.rowSpan > 1) {
                    carried++
                    ending[y + minOf(cell.rowSpan, section.rows.size - y)]++
                }
            }
        }
        return widths
    }

    private fun buildSection(
        section: SectionBuilderInstance,
        explicit: IntArray,
        available: IntArray? = null,
    ): MutableList<MutableRow>? {
        val rows: MutableList<MutableRow> =
            MutableList(section.rows.size) { ArrayList(section.rows.size) }

        for ((y, row) in section.rows.withIndex()) {
            var x = 0
            for (cellBuilder in row.cells) {
                x = rows.findEmptyColumn(x, y)
                if (!insertCell(cellBuilder, section, rows, x, y, explicit, available)) return null
                x += 1
            }
        }

        return rows
    }

    private fun insertCell(
        cell: CellBuilderInstance,
        section: SectionBuilderInstance,
        rows: MutableList<MutableRow>,
        startingX: Int,
        startingY: Int,
        explicit: IntArray,
        available: IntArray?,
    ): Boolean {
        val tableCol = table.columns[startingX]
        val sectionCol = section.columns[startingX]
        val row = section.rows[startingY]

        // The W3 standard says that spans are truncated rather than increasing the size of the table
        val rowSpan = cell.rowSpan.coerceAtMost(rows.size - startingY)
        val coveredRows = startingY..<startingY + rowSpan
        val baselineSpan = cell.columnSpan.coerceAtMost(1 + (coveredRows.minOfOrNull { explicit[it] } ?: 0))
        val extra = coveredRows.minOfOrNull { available?.get(it) ?: 0 } ?: 0
        val columnSpan = cell.columnSpan.coerceAtMost(baselineSpan + extra)

        fun <T : Any> getStyle(default: T, getter: (CellStyleBuilder) -> T?): T {
            return getter(cell) ?: getter(row) ?: sectionCol?.let(getter)
            ?: tableCol?.let(getter) ?: getter(section) ?: getter(table) ?: default
        }

        val borders = getStyle(Borders.ALL) { it.cellBorders }
        val padding = getStyle(Padding { horizontal = 1 }) { it.padding }
        val whitespace = getStyle(Whitespace.PRE) { it.whitespace }
        val textAlign = getStyle(TextAlign.NONE) { it.align }
        val verticalAlign = getStyle(VerticalAlign.TOP) { it.verticalAlign }
        val overflowWrap = getStyle(OverflowWrap.ELLIPSES) { it.overflowWrap }
        val stripedStyle = when {
            section.rowStyles.isNotEmpty() -> section.rowStyles[startingY % section.rowStyles.size]
            else -> null
        }
        val style = foldStyles(
            cell.style,
            row.style,
            stripedStyle,
            sectionCol?.style,
            tableCol?.style,
            section.style,
            table.style
        )
        val content = when (cell.content) {
            is CellContent.WidgetContent -> cell.content.widget
            is CellContent.TextContent -> Text(
                cell.content.text,
                whitespace,
                textAlign,
                overflowWrap
            )
        }.withPadding(padding)

        val builtCell = Cell.Content(
            content = content,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            borderLeft = borders.left,
            borderTop = borders.top,
            borderRight = borders.right.takeIf { columnSpan == 1 },
            borderBottom = borders.bottom.takeIf { rowSpan == 1 },
            style = style,
            textAlign = textAlign,
            verticalAlign = verticalAlign,
            paddingWidth = padding.left + padding.right,
        )

        val lastX = startingX + columnSpan - 1
        for (x in startingX..lastX) {
            val lastY = startingY + rowSpan - 1
            for (y in startingY..lastY) {
                val c = if (x == startingX && y == startingY) {
                    builtCell
                } else {
                    Cell.SpanRef(
                        builtCell,
                        borderLeft = borders.left.takeIf { x == startingX },
                        borderTop = borders.top.takeIf { y == startingY },
                        borderRight = borders.right.takeIf { x == lastX },
                        borderBottom = borders.bottom.takeIf { y == lastY },
                    )
                }
                val tableRow = rows.getRow(y)
                val existing = tableRow.getCell(x)

                // The W3 standard calls tables with overlap invalid, and dictates that user agents
                // either show the visual overlap, or shift the overlapping cells. We aren't bound
                // by their laws, and instead take the gentleman's approach and throw an exception.
                if (existing !== Cell.Empty) return false
                tableRow[x] = c
            }
        }
        if (available != null && columnSpan > baselineSpan) {
            for (y in coveredRows) available[y] -= columnSpan - baselineSpan
        }
        return true
    }

}

private fun MutableList<MutableRow>.findEmptyColumn(x: Int, y: Int): Int {
    val row = get(y)
    if (x > row.lastIndex) {
        row.ensureSize(x + 1)
        return x
    }

    for (i in x..row.lastIndex) {
        if (row[i] === Cell.Empty) return i
    }

    row.ensureSize(row.size + 1)
    return row.lastIndex
}

private fun MutableList<MutableRow>.getRow(y: Int): MutableRow {
    repeat(y - lastIndex) { add(mutableListOf()) }
    return get(y)
}

private fun MutableRow.getCell(x: Int): Cell {
    ensureSize(x + 1)
    return get(x)
}

private fun MutableRow.ensureSize(size: Int) {
    repeat(size - this.size) { add(Cell.Empty) }
}
