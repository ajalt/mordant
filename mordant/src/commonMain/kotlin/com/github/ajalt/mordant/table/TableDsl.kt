package com.github.ajalt.mordant.table

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.ColumnWidth.Companion.Auto
import com.github.ajalt.mordant.table.ColumnWidth.Companion.Expand
import com.github.ajalt.mordant.table.ColumnWidth.Companion.Fixed
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.withPadding

interface CellStyleBuilderBase {
    var style: TextStyle?
    var whitespace: Whitespace?
    var align: TextAlign?
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
        style = TextStyle(
            color,
            bgColor,
            bold,
            italic,
            underline,
            dim,
            inverse,
            strikethrough,
            hyperlink
        )
    }
}

interface CellStyleBuilder : CellStyleBuilderBase {
    /**
     * Borders around each individual cell.
     *
     * For cells on the edges of the table, the outside borders can be overridden by
     * [tableBorders][TableBuilder.tableBorders]
     */
    var cellBorders: Borders?

    /** Vertical alignment of cell contents */
    var verticalAlign: VerticalAlign?

    /**
     * The padding around each cell
     *
     * If you want to set the padding around the entire layout, use [withPadding].
     */
    var padding: Padding?

    /**
     * Set the padding around each cell
     *
     * If you want to set the padding around the entire layout, use [withPadding].
     */
    fun padding(all: Int) {
        padding = Padding(all)
    }

    fun padding(block: Padding.Builder.() -> Unit) {
        padding = Padding(block)
    }
}

/**
 * Configuration for how a column should be sized in a table.
 */
data class ColumnWidth(
    /**
     * The priority of the column when allocating available width.
     *
     * Available width is allocated to columns in decreasing order of priority.
     *
     * - [Fixed] columns have a priority of 3.
     * - [Auto] columns have a priority of 2.
     * - [Expand] columns have a priority of 1.
     */
    val priority: Int,
    /**
     * The fixed width of the column, or `null` if the width should be computed automatically
     */
    val width: Int? = null,
    /**
     * The weight of the column when expanding, or `null` if the column should not expand.
     *
     * If there are multiple expanding columns with the same [priority], the available width
     * will be divided among them proportional to their weights.
     */
    val expandWeight: Float? = null,
) {
    init {
        require(width == null || width > 0) { "width must be greater than zero" }
        require(expandWeight == null || expandWeight > 0f) {
            "expandWeight must be greater than zero"
        }
        require(width == null || expandWeight == null) {
            "Cannot set both width and expandWeight"
        }
    }

    override fun toString(): String {
        return when {
            isAuto -> "Auto"
            isExpand -> "Expand($expandWeight)"
            isFixed -> "Fixed($width)"
            else -> "Custom(width=$width, expandWeight=$expandWeight, priority=$priority)"
        }
    }

    @Suppress("FunctionName")
    companion object {

        /** The column will fit to the size of its content */
        val Auto = ColumnWidth(2)

        /**
         * The column will have a fixed [width].
         *
         * The width includes padding, so increasing horizontal padding of a fixed column will decrease content width rather
         * than expand the column.
         */
        fun Fixed(width: Int): ColumnWidth {
            require(width > 0) { "width must be greater than zero" }
            return ColumnWidth(3, width)
        }

        /**
         * The column will expand to fill the available terminal width.
         *
         * If there are multiple expanding columns, the available width will be divided among them
         * proportional to their [weight]s.
         */
        fun Expand(weight: Number = 1f): ColumnWidth {
            val w = weight.toFloat()
            require(w > 0) { "weight must be greater than zero" }
            return ColumnWidth(1, null, w)
        }
    }
}

@DslMarker
annotation class MordantDsl

@MordantDsl
interface ColumnBuilder : CellStyleBuilder {
    /** Set the width for this column */
    var width: ColumnWidth
}

interface ColumnHolderBuilder {
    /**
     * If false, (the default) [padding][CellStyleBuilder.padding] in
     * [fixed width][ColumnWidth.Fixed] columns will reduce the content width so
     * that the total width is always exactly the specified width.
     *
     * If true, padding will be added to the specified width so padding never reduces the content
     * width.
     */
    var addPaddingWidthToFixedWidth: Boolean

    /** Configure a single column, which the first column at index 0. */
    fun column(i: Int, init: ColumnBuilder.() -> Unit)
}

@MordantDsl
interface TableBuilder : CellStyleBuilder, ColumnHolderBuilder {
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
interface GridBuilder : CellStyleBuilder, RowHolderBuilder, ColumnHolderBuilder

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
interface LinearLayoutBuilder : CellStyleBuilderBase {
    /** Add multiple cells to this builder */
    fun cells(
        cell1: Any?,
        cell2: Any?,
        vararg cells: Any?,
        init: CellStyleBuilderBase.() -> Unit = {},
    )

    /** Add all [cells] from an iterable to this builder */
    fun cellsFrom(cells: Iterable<Any?>, init: CellStyleBuilderBase.() -> Unit = {})

    /** Add a cell to this builder.
     *
     * The [content] can be a [Widget] to render, or any other type of object which will be
     * converted to a string.
     */
    fun cell(content: Any?, init: CellStyleBuilderBase.() -> Unit = {})

    /** The amount of padding between each cell */
    var spacing: Int
}

@MordantDsl
interface HorizontalLayoutBuilder : LinearLayoutBuilder, ColumnHolderBuilder {
    /** Vertical alignment of cell contents */
    var verticalAlign: VerticalAlign?
}

@MordantDsl
interface VerticalLayoutBuilder : LinearLayoutBuilder {
    /** Set the width for this layout. By default, the width is `Auto` and lines are not padded to equal width. */
    var width: ColumnWidth
}

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
 * [padding][SectionBuilder.padding], or remove it by setting [cellBorders][SectionBuilder.cellBorders] to
 * [NONE][Borders.NONE].
 */
fun grid(init: GridBuilder.() -> Unit): Widget {
    return GridBuilderInstance().apply(init).build()
}

/**
 * Lay out widget in a horizontal row.
 *
 * You can set the [spacing][HorizontalLayoutBuilder.spacing] between each widget in the layout.
 */
fun horizontalLayout(init: HorizontalLayoutBuilder.() -> Unit): Widget {
    return HorizontalLayoutBuilderInstance().apply(init).build()
}

/**
 * Lay out widgets in a vertical column.
 *
 * You can set the [spacing][VerticalLayoutBuilder.spacing] between each widget in the layout.
 *
 * By default, the rendered layout won't include any trailing whitespace. You can change this behavior by setting
 * [align][VerticalLayoutBuilder.align] to a value other than [NONE][TextAlign.NONE]
 */
fun verticalLayout(init: VerticalLayoutBuilder.() -> Unit): Widget {
    return VerticalLayoutBuilderInstance().apply(init).build()
}

internal val ColumnWidth.isAuto: Boolean
    get() = this.width == null && this.expandWeight == null

internal val ColumnWidth.isExpand: Boolean
    get() = this.expandWeight != null

internal val ColumnWidth.isFixed: Boolean
    get() = this.width != null
