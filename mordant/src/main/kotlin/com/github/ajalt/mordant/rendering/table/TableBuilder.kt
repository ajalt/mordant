package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.Padded
import com.github.ajalt.mordant.rendering.foldStyles

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
                expand = table.expand,
                borders = table.borders,
                borderStyle = table.borderStyle,
                headerRowCount = header.size,
                footerRowCount = footer.size,
                columnWidths = table.columns.mapValues { it.value.width }
        )
    }

    private fun buildSection(section: SectionBuilder, builderWidth: Int): MutableList<MutableRow> {
        val rows: MutableList<MutableRow> = MutableList(section.rows.size) { ArrayList(section.rows.size) }

        for ((y, row) in section.rows.withIndex()) {
            var x = 0
            for (cellBuilder in row.cells) {
                x = rows.findEmptyColumn(x, y)
                insertCell(section, table.columns[y], row, cellBuilder, builderWidth, rows, x, y)
                x += 1
            }
        }

        // Ensure the table is rectangular
        val width = rows.maxOfOrNull { it.size }
        if (width != null) {
            for (row in rows) {
                row.ensureSize(width)
            }
        }

        return rows
    }

    // The W3 standard calls tables with overlap invalid, and dictates that user agents either show
    // the visual overlap, or shift the overlapping cells.
    // We aren't bound by their laws, and instead take the gentleman's approach and throw an exception.
    private fun insertCell(
            section: SectionBuilder,
            column: ColumnBuilder?,
            rowBuilder: RowBuilder,
            cell: CellBuilder,
            builderWidth: Int,
            rows: MutableList<MutableRow>,
            startingX: Int,
            startingY: Int
    ) {
        val contentCell = buildCell(section, column, rowBuilder, cell, startingX, startingY, builderWidth, rows.size)
        val lastX = startingX + cell.columnSpan - 1
        for (x in startingX..lastX) {
            val lastY = startingY + cell.rowSpan - 1
            for (y in startingY..lastY) {
                val c = if (x == startingX && y == startingY) {
                    contentCell
                } else {
                    Cell.SpanRef(
                            contentCell,
                            borderTop = cell.borderTop && y == startingY,
                            borderRight = cell.borderRight && x == lastX,
                            borderBottom = cell.borderBottom && y == lastY
                    )
                }
                val row = rows.getRow(y)
                val existing = row.getCell(x)
                require(existing === Cell.Empty) {
                    "Invalid table: cell spans cannot overlap"
                }
                row[x] = c
            }
        }
    }

    private fun buildCell(
            section: SectionBuilder,
            column: ColumnBuilder?,
            row: RowBuilder,
            cell: CellBuilder,
            x: Int,
            y: Int,
            builderWidth: Int,
            builderHeight: Int
    ): Cell.Content {
        val padding = cell.padding ?: row.padding ?: column?.padding ?: table.padding
        val style = foldStyles(cell.style, row.style, section.rowStyles.getOrNull(y), column?.style, table.textStyle)
        val content = Padded.get(cell.content, padding)

        // The W3 standard says that spans are truncated rather than increasing the size of the table
        val columnSpan = cell.columnSpan
                .coerceAtMost(builderWidth - row.cells.size + 1)
                .coerceAtMost(builderWidth - x).coerceAtLeast(1)
        val rowSpan = cell.rowSpan.coerceAtMost(builderHeight - y)

        return Cell.Content(
                content = content,
                rowSpan = rowSpan,
                columnSpan = columnSpan,
                borderLeft = cell.borderLeft,
                borderTop = cell.borderTop,
                // The content cell of a span is in the top-left, and so doesn't have bottom or right borders
                borderRight = cell.borderRight && columnSpan == 1,
                borderBottom = cell.borderBottom && rowSpan == 1,
                style = style
        )
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
