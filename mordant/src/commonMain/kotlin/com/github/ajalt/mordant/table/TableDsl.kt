package com.github.ajalt.mordant.table

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.withPadding

interface CellStyleBuilder {
    /**
     * The padding around each cell
     *
     * If you want to set the padding on around the entire widget, use [withPadding].
     */
    var padding: Padding?
    var style: TextStyle?
    var cellBorders: Borders?
    var whitespace: Whitespace?
    var align: TextAlign?
    var verticalAlign: VerticalAlign?
    var overflowWrap: OverflowWrap?

    @Deprecated("borders has been renamed to cellBorders", replaceWith = ReplaceWith("cellBorders"))
    var borders: Borders?

    fun style(
        color: Color? = null,
        bgColor: Color? = null,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        dim: Boolean = false,
        inverse: Boolean = false,
        strikethrough: Boolean = false,
        hyperlink: String? = null,
    ) {
        style = TextStyle(color, bgColor, bold, italic, underline, dim, inverse, strikethrough, hyperlink)
    }

    /**
     * The padding around each cell
     *
     * If you want to set the padding on around the entire widget, use [withPadding].
     */
    fun padding(all: Int) {
        padding = Padding(all)
    }

    fun padding(block: Padding.Builder.() -> Unit) {
        padding = Padding(block)
    }

}

sealed class ColumnWidth {
    /** The column will fit to the size of its content */
    object Auto : ColumnWidth()

    /**
     * The column will have a fixed [width].
     *
     * The width includes padding, so increasing horizontal padding of a fixed column will decrease content width rather
     * than expand the column.
     */
    class Fixed(val width: Int) : ColumnWidth() {
        init {
            require(width > 0) { "width must be greater than zero" }
        }
    }

    /**
     * The column will expand to fill the available terminal width.
     *
     * If there are multiple expanding columns, the available width will be divided among them
     * proportional to their [weight]s.
     */
    class Expand(val weight: Float = 1f) : ColumnWidth() {
        init {
            require(weight > 0) { "weight must be greater than zero" }
        }

        constructor(weight: Number) : this(weight.toFloat())
    }
}

@DslMarker
annotation class MordantDsl

@MordantDsl
interface ColumnBuilder : CellStyleBuilder {
    /** Set the width for this column */
    var width: ColumnWidth
}

@MordantDsl
interface TableBuilder : CellStyleBuilder {
    /** The characters to use to draw cell edges */
    var borderType: BorderType

    /** A text style to apply to the cell border characters */
    var borderStyle: TextStyle

    /**
     * Which borders to draw on the outer edge of the table.
     *
     * This overrides [cellBorders] for borders on the outer edges of the table. The default values of `null` will not
     * override [cellBorders].
     */
    var tableBorders: Borders?

    /** Add a [widget] as a caption oto the top of this table. */
    fun captionTop(widget: Widget)

    /** Add [text] as a caption to the top of this table. */
    fun captionTop(text: String, align: TextAlign = TextAlign.CENTER)

    /** Add a [widget] as a caption to the bottom of this table. */
    fun captionBottom(widget: Widget)

    /** Add [text] as a caption to the bottom of this table. */
    fun captionBottom(text: String, align: TextAlign = TextAlign.CENTER)

    /** Configure a single column, which the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit)

    /** Configure the header section. */
    fun header(init: SectionBuilder.() -> Unit)

    /** Configure the body section. */
    fun body(init: SectionBuilder.() -> Unit)

    /** Configure the footer section. */
    fun footer(init: SectionBuilder.() -> Unit)

    @Deprecated("`outerBorder=false` has been replaced with `tableBorders=Borders.NONE`")
    var outerBorder: Boolean
}

@MordantDsl
interface RowHolderBuilder {
    /** Add styles to alternating rows. If there are more rows than styles, the styles will loop. */
    fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle)

    /** Add all [cells] from an iterable. */
    fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit = {})

    /** Add a row with one or more cells. */

    fun row(vararg cells: Any?, init: RowBuilder.() -> Unit = {})

    /** Add a row. */
    fun row(init: RowBuilder.() -> Unit)
}

@MordantDsl
interface SectionBuilder : CellStyleBuilder, RowHolderBuilder {
    /** Configure a single column, which the first column at index 0. */
    fun column(i: Int, init: CellStyleBuilder.() -> Unit)
}

@MordantDsl
interface GridBuilder : CellStyleBuilder, RowHolderBuilder {
    /** Configure a single column, with the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit)
}

@MordantDsl
interface RowBuilder : CellStyleBuilder {
    /** Add multiple cells to this row */
    fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellBuilder.() -> Unit = {})

    /** Add all [cells] from an iterable to this row */
    fun cellsFrom(cells: Iterable<Any?>, init: CellBuilder.() -> Unit = {})

    /** Add a cell to this row.
     *
     * The [content] can be a [Widget] to render, or any other type of object which will be
     * converted to a string.
     */
    fun cell(content: Any?, init: CellBuilder.() -> Unit = {})
}

@MordantDsl
interface SingleLineBuilder : CellStyleBuilder {
    /** Add multiple cells to this builder */
    fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellStyleBuilder.() -> Unit = {})

    /** Add all [cells] from an iterable to this builder */
    fun cellsFrom(cells: Iterable<Any?>, init: CellStyleBuilder.() -> Unit = {})

    /** Add a cell to this builder.
     *
     * The [content] can be a [Widget] to render, or any other type of object which will be
     * converted to a string.
     */
    fun cell(content: Any?, init: CellStyleBuilder.() -> Unit = {})
}

@MordantDsl
interface HorizontalLayoutBuilder : SingleLineBuilder {
    /** Configure a single column, with the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit)
}

@MordantDsl
interface VerticalLayoutBuilder : ColumnBuilder, SingleLineBuilder

@MordantDsl
interface CellBuilder : CellStyleBuilder {
    /**
     * Set the number of columns that this cell should span. The value will be truncated to the width of the table.
     */
    var columnSpan: Int


    /**
     * Set the number of rows that this cell should span.
     */
    var rowSpan: Int
}

/**
 * Build a table widget.
 *
 * Tables have three optional sections: the [header][TableBuilder.header], the
 * [body][TableBuilder.body], and the [footer][TableBuilder.footer].
 *
 * Within each section, you can add entire [rows][SectionBuilder.row] of cells at once, or one at a
 * time with the [cell][RowBuilder.cell] builder.
 *
 * You can customize the table's styles at a number of levels, with more specific styles overriding
 * less specific styles. The places you can customize are, from least-specific to most-specific:
 *
 * 1. Table: applies to every cell
 * 2. Section: applies to all cells in the header, body, or footer
 * 3. Table Column: applies to all cells in a column
 * 4. Section Column: applies to all cells in a column for a single section
 * 5. Row: applies to all cells in a row
 * 6. Cell: applies to a single cell
 */
fun table(init: TableBuilder.() -> Unit): Table {
    val tableBuilder = TableBuilderInstance().apply(init)
    val table = TableLayout(tableBuilder).buildTable()
    return when {
        tableBuilder.captionTop != null || tableBuilder.captionBottom != null -> {
            TableWithCaption(
                table,
                Caption(table, tableBuilder.captionTop, tableBuilder.captionBottom),
            )
        }

        else -> table
    }
}

/**
 * Build a grid widget that can be used to lay out text and other widgets.
 *
 * This builder functions like a [table] builder, but has no sections and no borders.
 *
 * By default, there is one space between cells in a row. You can increase this by adding
 * [padding][SectionBuilder.padding], or remove it by setting [borders][SectionBuilder.cellBorders] to
 * [NONE][Borders.NONE].
 */
fun grid(init: GridBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        cellBorders = Borders.LEFT_RIGHT
        tableBorders = Borders.NONE
        borderType = BorderType.BLANK
        padding = Padding(0)
        val gb = GridBuilderInstance(bodySection)
        gb.init()
        columns.putAll(gb.columns)
    }

    return TableLayout(tableBuilder).buildTable()
}

/**
 * Build a row widget that can be used to lay out text and other widgets.
 *
 * This builder functions like a single row in a [grid]. Cells have optional [padding], which sets the default left
 * padding of all cells after the first.
 */
fun horizontalLayout(padding: Int = 1, init: HorizontalLayoutBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        cellBorders = Borders.NONE
        this.padding = Padding { left = padding }
        val b = HorizontalLayoutBuilderInstance(bodySection)
        b.init()
        bodySection.rows += b.row
        columns.putAll(b.columns)
        column(0) { this.padding = this.padding ?: Padding(0) }
    }

    return TableLayout(tableBuilder).buildTable()
}


/**
 * Build a column widget that can be used to lay out text and other widgets.
 *
 * This builder functions like a single column in a [grid]. Cells have optional [padding], which sets the default top
 * padding of all cells after the first.
 */
fun verticalLayout(padding: Int = 0, init: VerticalLayoutBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        cellBorders = Borders.NONE
        this.padding = Padding(0)
        val b = VerticalLayoutBuilderInstance(bodySection, padding)
        b.init()
        column(0) { width = b.width }
    }

    return TableLayout(tableBuilder).buildTable()
}
