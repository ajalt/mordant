package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.DEFAULT_STYLE

private val DEFAULT_ROW_STYLES = listOf(DEFAULT_STYLE)
private val SPAN_PLACEHOLDER = Cell(Text(""))

data class Cell(
        val content: Renderable,
        val rowSpan: Int = 1,
        val columnSpan: Int = 1,
        val borderLeft: Boolean = true,
        val borderTop: Boolean = true,
        val borderRight: Boolean = true,
        val borderBottom: Boolean = true,
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
        val head: List<Cell>,
        val body: List<Cell>,
        val foot: List<Cell>,
        val width: ColumnWidth = ColumnWidth.Default,
        val style: TextStyle = DEFAULT_STYLE
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
        val headStyles: List<TextStyle> = DEFAULT_ROW_STYLES,
        val bodyStyles: List<TextStyle> = DEFAULT_ROW_STYLES,
        val footStyles: List<TextStyle> = DEFAULT_ROW_STYLES,
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
        TODO()
    }
}
