package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.Padded
import com.github.ajalt.mordant.rendering.Text
import com.github.ajalt.mordant.rendering.foldStyles

private typealias MutableColumn = MutableList<Cell>
private typealias ImmutableColumn = List<Cell>

private val SPAN_PLACEHOLDER = Cell(Text("")) // TODO: if we keep track of the original renderable and column span, we could distribute its measurement across its columns
private val EMPTY_CELL = Cell(Text(""), borderBottom = false, borderLeft = false, borderRight = false, borderTop = false)

internal class TableBuilderLayout(private val table: TableBuilder) {
    fun buildTable(): Table {
        val builderWidth = listOf(table.headerSection, table.bodySection, table.footerSection).maxOf {
            it.rows.maxOf { r -> r.cells.size }
        }
        val header = buildSection(table.headerSection, builderWidth)
        val body = buildSection(table.bodySection, builderWidth)
        val footer = buildSection(table.footerSection, builderWidth)

        val columns = List(maxOf(header.size, body.size, footer.size)) { i ->
            Column(
                    header.getOrNull(i) ?: emptyList(),
                    body.getOrNull(i) ?: emptyList(),
                    footer.getOrNull(i) ?: emptyList(),
                    table.columns[i]?.width ?: ColumnWidth.Default
            )
        }

        return Table(columns, table.expand, table.borders, table.borderStyle)
    }

    private fun buildSection(section: SectionBuilder, builderWidth: Int): List<ImmutableColumn> {
        val initialSize = section.rows.firstOrNull()?.cells?.size ?: 0
        val columns: MutableList<MutableColumn> = MutableList(initialSize) { ArrayList(section.rows.size) }

        var x = 0

        for (row in section.rows) {
            for ((y, cellBuilder) in row.cells.withIndex()) {
                x = columns.findEmptyColumn(x, y)

                val cell = buildCell(section, table.columns[y], row, cellBuilder, x, y, builderWidth, section.rows.size)
                insertCell(cell, columns, x, y)
            }
        }

        return columns
    }

    // The W3 standard calls tables with overlap invalid, and dictates that user agents either show
    // the visual overlap, or shift the overlapping cells.
    // We aren't bound by their laws, and instead take the gentleman's approach and throw an exception.
    private fun insertCell(cell: Cell, columns: MutableList<MutableColumn>, startingX: Int, startingY: Int) {
        for (x in startingX until startingX + cell.columnSpan) {
            for (y in startingY until startingY + cell.rowSpan) {
                val c = if (x == startingX && y == startingY) cell else SPAN_PLACEHOLDER
                val column = columns.getColumn(x)
                val existing = column.getCell(y)
                require(existing !== SPAN_PLACEHOLDER) {
                    "Invalid table: cell spans cannot overlap"
                }
                column[y] = c
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
    ): Cell {
        val padding = cell.padding ?: row.padding ?: column?.padding ?: table.padding
        val style = foldStyles(cell.style, row.style, section.rowStyles.getOrNull(y), column?.style, table.textStyle)
        val content = Padded.get(cell.content, padding)

        // The W3 standard says that spans are truncated rather than increasing the size of the table
        val columnSpan = cell.columnSpan
                .coerceAtMost(builderWidth - row.cells.size + 1)
                .coerceAtMost(builderWidth - x).coerceAtLeast(1)
        val rowSpan = cell.rowSpan.coerceAtMost(builderHeight - y)

        return Cell(
                content = content,
                rowSpan = rowSpan,
                columnSpan = columnSpan,
                borderLeft = cell.borderLeft,
                borderTop = cell.borderTop,
                borderRight = cell.borderRight,
                borderBottom = cell.borderBottom,
                style = style
        )
    }
}

private fun MutableList<MutableColumn>.findEmptyColumn(x: Int, y: Int): Int {
    var column: MutableColumn
    var i = x
    do {
        column = getColumn(i++)
    } while (column.getCell(y) === SPAN_PLACEHOLDER)
    return i
}

private fun MutableList<MutableColumn>.getColumn(x: Int): MutableColumn {
    repeat(lastIndex - x) { add(mutableListOf()) }
    return get(x)
}

private fun MutableColumn.getCell(y: Int): Cell {
    repeat(lastIndex - y) { add(EMPTY_CELL) }
    return get(y)
}
