package com.github.ajalt.mordant.table

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Text

interface CellStyleBuilder {
    var padding: Padding?
    var style: TextStyle?
    var borders: Borders?
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

private class CellStyleBuilderMixin : CellStyleBuilder {
    override var padding: Padding? = null
    override var style: TextStyle? = null
    override var borders: Borders? = null
    override var align: TextAlign? = null
    override var verticalAlign: VerticalAlign? = null
    override var overflowWrap: OverflowWrap? = null
}

sealed class ColumnWidth {
    /** The column will fit to the size of its content */
    object Auto : ColumnWidth()

    /** The column will have a fixed [width] */
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

        constructor(weight: Int) : this(weight.toFloat())
    }
}

@DslMarker
annotation class MordantDsl

@MordantDsl
class ColumnBuilder internal constructor() : CellStyleBuilder by CellStyleBuilderMixin() {
    /** Set the width for this column */
    var width: ColumnWidth = ColumnWidth.Auto
}

@MordantDsl
class TableBuilder internal constructor() : CellStyleBuilder by CellStyleBuilderMixin() {
    var borderStyle: BorderStyle = BorderStyle.SQUARE
    var borderTextStyle: TextStyle = DEFAULT_STYLE
    var outerBorder: Boolean = true

    internal val columns = mutableMapOf<Int, ColumnBuilder>()
    internal val headerSection = SectionBuilder()
    internal val bodySection = SectionBuilder()
    internal val footerSection = SectionBuilder()
    internal var captionTop: Widget? = null
    internal var captionBottom: Widget? = null

    /** Add a [widget] as a caption oto the top of this table. */
    fun captionTop(widget: Widget) {
        captionTop = widget
    }

    /** Add [text] as a caption to the top of this table. */
    fun captionTop(text: String, style: TextStyle = DEFAULT_STYLE, align: TextAlign = TextAlign.CENTER) {
        captionTop(Text(text, style, align = align))
    }

    /** Add a [widget] as a caption to the bottom of this table. */
    fun captionBottom(widget: Widget) {
        captionBottom = widget
    }

    /** Add [text] as a caption to the bottom of this table. */
    fun captionBottom(text: String, style: TextStyle = DEFAULT_STYLE, align: TextAlign = TextAlign.CENTER) {
        captionBottom(Text(text, style, align = align))
    }

    /** Configure a single column, which the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, ColumnBuilder(), init)

    /** Configure the header section. */
    fun header(init: SectionBuilder.() -> Unit) {
        headerSection.init()
    }

    /** Configure the body section. */
    fun body(init: SectionBuilder.() -> Unit) {
        bodySection.init()
    }

    /** Configure the footer section. */
    fun footer(init: SectionBuilder.() -> Unit) {
        footerSection.init()
    }
}


@MordantDsl
class SectionBuilder internal constructor() : CellStyleBuilder by CellStyleBuilderMixin() {
    internal val rows = mutableListOf<RowBuilder>()
    internal val columns = mutableMapOf<Int, CellStyleBuilder>()
    internal var rowStyles = listOf<TextStyle>()

    /** Configure a single column, which the first column at index 0. */
    fun column(i: Int, init: CellStyleBuilder.() -> Unit) = initColumn(columns, i, ColumnBuilder(), init)

    /** Add styles to alternating rows. If there are more rows than styles, the styles will loop. */
    fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        rowStyles = listOf(style1, style2) + styles.asList()
    }

    /**
     * Add all [cells] from an iterable.
     */
    fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit = {}) {
        val cellBuilders = cells.mapTo(mutableListOf()) { CellBuilder(getWidget(it)) }
        rows += RowBuilder(cellBuilders).apply(init)
    }

    /**
     * Add a row with one or more cells.
     */
    fun row(vararg cells: Any?, init: RowBuilder.() -> Unit = {}) {
        rowFrom(cells.asList(), init)
    }

    /**
     * Add a row.
     */
    fun row(init: RowBuilder.() -> Unit) {
        rows += RowBuilder(mutableListOf()).apply(init)
    }
}

@MordantDsl
class GridBuilder internal constructor(private val section: SectionBuilder) : CellStyleBuilder by section {
    internal val columns = mutableMapOf<Int, ColumnBuilder>()

    /** Configure a single column, with the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, ColumnBuilder(), init)

    /** Add styles to alternating rows. If there are more rows than styles, the styles will loop. */
    fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        section.rowStyles(style1, style2, *styles)
    }

    /**
     * Add all [cells] from an iterable.
     */
    fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit = {}) {
        section.rowFrom(cells, init)
    }

    /**
     * Add a row with one or more cells.
     */
    fun row(vararg cells: Any?, init: RowBuilder.() -> Unit = {}) {
        section.row(*cells, init = init)
    }

    /**
     * Add a row.
     */
    fun row(init: RowBuilder.() -> Unit) {
        section.row(init)
    }
}

@MordantDsl
class RowBuilder internal constructor(
    internal val cells: MutableList<CellBuilder>,
) : CellStyleBuilder by CellStyleBuilderMixin() {
    /** Add multiple cells to this row */
    fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellBuilder.() -> Unit = {}) {
        cell(cell1, init)
        cell(cell2, init)
        cellsFrom(cells.asList(), init)
    }

    /** Add all [cells] from an iterable to this row */
    fun cellsFrom(cells: Iterable<Any?>, init: CellBuilder.() -> Unit = {}) {
        cells.mapTo(this.cells) { CellBuilder(getWidget(it)).apply(init) }
    }

    /** Add a cell to this row.
     *
     * The [content] can be a [Widget] to render, or any other type of object which will be
     * converted to a string.
     */
    fun cell(content: Any?, init: CellBuilder.() -> Unit = {}) {
        cells += CellBuilder(getWidget(content)).apply(init)
    }
}

@MordantDsl
class CellBuilder internal constructor(
    internal val content: Widget = Text(""),
) : CellStyleBuilder by CellStyleBuilderMixin() {

    /**
     * Set the number of columns that this cell should span. The value will be truncated to the width of the table.
     */
    var columnSpan = 1
        set(value) {
            require(value > 0) { "Column span must be greater than 0" }
            field = value
        }

    /**
     * Set the number of rows that this cell should span.
     */
    var rowSpan = 1
        set(value) {
            require(value > 0) { "Row span must be greater than 0" }
            field = value
        }
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
 * 1. Table, applies to every cell
 * 2. Section, applies to all cells in the header, body, or footer
 * 3. Table Column, applies to all cells in a column
 * 4. Section Column, applies to all cells in a column for a single section
 * 5. Row, applies to all cells in a row
 * 6. Cell, applies to a single cell
 */
fun table(init: TableBuilder.() -> Unit): Table {
    val tableBuilder = TableBuilder().apply(init)
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
 * [padding][TableBuilder.padding], or remove it by setting [borders][TableBuilder.borders] to
 * [NONE][Borders.NONE].
 */
fun grid(init: GridBuilder.() -> Unit): Widget {
    return table {
        borders = Borders.LEFT_RIGHT
        outerBorder = false
        borderStyle = BorderStyle.BLANK
        padding = Padding.none()

        val gb = GridBuilder(bodySection)
        gb.init()
        columns.putAll(gb.columns)
    }
}

private fun getWidget(content: Any?): Widget {
    if (content is Widget) return content
    return when (val string = content.toString()) {
        "" -> EmptyWidget
        else -> Text(string)
    }
}

private fun <T : CellStyleBuilder> initColumn(columns: MutableMap<Int, T>, i: Int, def: T, init: T.() -> Unit) {
    require(i >= 0) { "column index cannot be negative" }
    var v = columns[i]
    if (v == null) {
        v = def
        columns[i] = v
    }
    v.init()
}
