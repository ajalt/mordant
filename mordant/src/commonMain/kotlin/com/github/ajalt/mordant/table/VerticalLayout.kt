package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

private class VerticalLayoutCell(
    val content: Widget,
    val style: TextStyle?,
    val textAlign: TextAlign,
)

internal class VerticalLayout private constructor(
    private val cells: List<VerticalLayoutCell>,
    private val spacing: Int,
    private val columnWidth: ColumnWidth,
    private val textAlign: TextAlign,
    private val hasAlignedCells: Boolean,
) : Widget {
    companion object {
        fun fromTableBuilder(
            builder: TableBuilderInstance,
            spacing: Int,
            columnWidth: ColumnWidth,
        ): VerticalLayout {
            builder.padding(0)
            builder.cellBorders = Borders.NONE
            builder.tableBorders = Borders.NONE
            var aligned = false
            val cells = TableLayout(builder).buildTable().rows.map {
                check(it.size == 1)
                val cell = it[0] as Cell.Content
                aligned = aligned || (cell.textAlign !in listOf(null, TextAlign.NONE))
                VerticalLayoutCell(cell.content, cell.style, cell.textAlign)
            }
            return VerticalLayout(
                cells,
                spacing,
                columnWidth,
                builder.align ?: TextAlign.NONE,
                aligned,
            )
        }
    }

    init {
        require(spacing >= 0) { "layout spacing cannot be negative" }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        return cells.maxWidthRange { it.content.measure(t, width) }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val renderWidth = when {
            columnWidth is ColumnWidth.Expand -> width
            hasAlignedCells -> measure(t, width).max
            else -> width
        }
        val lines = mutableListOf<Line>()
        val spacingLine = when (textAlign) {
            TextAlign.NONE -> EMPTY_LINE
            else -> Line(listOf(Span.space(renderWidth)), DEFAULT_STYLE)
        }
        for ((i, cell) in cells.withIndex()) {
            if (i > 0) repeat(spacing) { lines += spacingLine }
            var rendered = cell.content.render(t, renderWidth).withStyle(cell.style)

            val w = columnWidth.toCustom()
            rendered = when {
                w.expandWeight != null -> rendered.setSize(width, textAlign = cell.textAlign)
                w.width != null -> rendered.setSize(w.width, textAlign = cell.textAlign)
                else -> rendered
            }
            // Cells always take up a line, even if empty
            lines += rendered.lines.ifEmpty { listOf(EMPTY_LINE) }
        }
        return Lines(lines)
    }
}
