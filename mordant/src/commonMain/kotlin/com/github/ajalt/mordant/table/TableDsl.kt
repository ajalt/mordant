package com.github.ajalt.mordant.table

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.Padding

interface CellStyleBuilder {
    var padding: Padding?
    var style: TextStyle?
    var borders: Borders?
    var whitespace: Whitespace?
    var align: TextAlign?
    var verticalAlign: VerticalAlign?
    var overflowWrap: OverflowWrap?

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

    fun padding(all: Int) {
        padding = Padding(all)
    }

    fun padding(vertical: Int, horizontal: Int) {
        padding = Padding(vertical, horizontal, vertical, horizontal)
    }

    fun padding(top: Int, horizontal: Int, bottom: Int) {
        padding = Padding(top, horizontal, bottom, horizontal)
    }

    fun padding(top: Int, right: Int, bottom: Int, left: Int) {
        padding = Padding(top, right, bottom, left)
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
    var borderType: BorderType
    var borderStyle: TextStyle
    var outerBorder: Boolean

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
    /** Add multiple cells to this row */
    fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellStyleBuilder.() -> Unit = {})

    /** Add all [cells] from an iterable to this row */
    fun cellsFrom(cells: Iterable<Any?>, init: CellStyleBuilder.() -> Unit = {})

    /** Add a cell to this row.
     *
     * The [content] can be a [Widget] to render, or any other type of object which will be
     * converted to a string.
     */
    fun cell(content: Any?, init: CellStyleBuilder.() -> Unit = {})
}

@MordantDsl
interface SingleRowBuilder : SingleLineBuilder {
    /** Configure a single column, with the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit)
}

@MordantDsl
interface SingleColumnBuilder : ColumnBuilder, SingleLineBuilder

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
 * [padding][SectionBuilder.padding], or remove it by setting [borders][SectionBuilder.borders] to
 * [NONE][Borders.NONE].
 */
fun grid(init: GridBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        borders = Borders.LEFT_RIGHT
        outerBorder = false
        borderType = BorderType.BLANK
        padding = Padding.none()
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
fun row(padding: Int = 1, init: SingleRowBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        borders = Borders.NONE
        this.padding = Padding.of(left = padding)
        val b = SingleRowBuilderInstance(bodySection)
        b.init()
        bodySection.rows += b.row
        columns.putAll(b.columns)
        column(0) { this.padding = this.padding ?: Padding.none() }
    }

    return TableLayout(tableBuilder).buildTable()
}


/**
 * Build a column widget that can be used to lay out text and other widgets.
 *
 * This builder functions like a single column in a [grid]. Cells have optional [padding], which sets the default top
 * padding of all cells after the first.
 */
fun column(padding: Int = 0, init: SingleColumnBuilder.() -> Unit): Widget {
    val tableBuilder = TableBuilderInstance().apply {
        borders = Borders.NONE
        this.padding = Padding.none()
        val b = SingleColumnBuilderInstance(bodySection, padding)
        b.init()
        column(0) { width = b.width }
    }

    return TableLayout(tableBuilder).buildTable()
}
