package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.Padded
import com.github.ajalt.mordant.rendering.foldStyles
import com.github.ajalt.mordant.rendering.withAlign

internal typealias ImmutableRow = List<Cell>
internal typealias MutableRow = MutableList<Cell>

internal class TableBuilderLayout(private val table: TableBuilder) {
    fun buildTable(): Table {
        val builderWidth = listOf(table.headerSection, table.bodySection, table.footerSection).maxOf {
            it.rows.maxOfOrNull { r -> r.cells.size } ?: 0
        }
        val header = buildSection(table.headerSection, builderWidth)
        val body = buildSection(table.bodySection, builderWidth)
        val footer = buildSection(table.footerSection, builderWidth)

        return Table(
                rows = listOf(header, body, footer).flatten(),
                borderStyle = table.borderStyle,
                borderTextStyle = table.borderTextStyle,
                headerRowCount = header.size,
                footerRowCount = footer.size,
                columnStyles = table.columns.mapValues { it.value.width }
        )
    }

    private fun buildSection(section: SectionBuilder, builderWidth: Int): MutableList<MutableRow> {
        val rows: MutableList<MutableRow> = MutableList(section.rows.size) { ArrayList(section.rows.size) }

        for ((y, row) in section.rows.withIndex()) {
            var x = 0
            for (cellBuilder in row.cells) {
                x = rows.findEmptyColumn(x, y)
                insertCell(cellBuilder, section, rows, x, y, builderWidth)
                x += 1
            }
        }

        return rows
    }

    // The W3 standard calls tables with overlap invalid, and dictates that user agents either show
    // the visual overlap, or shift the overlapping cells.
    // We aren't bound by their laws, and instead take the gentleman's approach and throw an exception.
    private fun insertCell(
            cell: CellBuilder,
            section: SectionBuilder,
            rows: MutableList<MutableRow>,
            startingX: Int,
            startingY: Int,
            builderWidth: Int
    ) {
        val column = table.columns[startingX]
        val row = section.rows[startingY]

        // The W3 standard says that spans are truncated rather than increasing the size of the table
        val maxRowSize = (startingY until startingY + cell.rowSpan)
                .maxOfOrNull { section.rows.getOrNull(it)?.cells?.size ?: 0 } ?: 0
        val columnSpan = cell.columnSpan.coerceAtMost(builderWidth - maxRowSize + 1)
        val rowSpan = cell.rowSpan.coerceAtMost(rows.size - startingY)

        fun <T : Any> getVal(default: T, getter: (CellStyleBuilder) -> T?): T {
            return getter(cell) ?: getter(row) ?: column?.let(getter) ?: getter(section) ?: default
        }

        val borders = getVal(table.borders) { it.borders }
        val padding = getVal(table.padding) { it.padding }
        val textAlign = getVal(table.align) { it.align }
        val verticalAlign = getVal(table.verticalAlign) { it.verticalAlign }
        val overflowWrap = getVal(table.overflowWrap) { it.overflowWrap }
        val style = foldStyles(cell.style, row.style, section.rowStyles.getOrNull(startingY), column?.style, table.style)
        val content = Padded.get(cell.content.withAlign(textAlign, overflowWrap), padding)

        val builtCell = Cell.Content(
                content = content,
                rowSpan = rowSpan,
                columnSpan = columnSpan,
                borderLeft = borders.left,
                borderTop = borders.top,
                borderRight = borders.right && columnSpan == 1,
                borderBottom = borders.bottom && rowSpan == 1,
                style = style,
                textAlign = textAlign,
                verticalAlign = verticalAlign
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
                            borderLeft = borders.left && x == startingX,
                            borderTop = borders.top && y == startingY,
                            borderRight = borders.right && x == lastX,
                            borderBottom = borders.bottom && y == lastY
                    )
                }
                val tableRow = rows.getRow(y)
                val existing = tableRow.getCell(x)
                require(existing === Cell.Empty) {
                    "Invalid table: cell spans cannot overlap"
                }
                tableRow[x] = c
            }
        }
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
