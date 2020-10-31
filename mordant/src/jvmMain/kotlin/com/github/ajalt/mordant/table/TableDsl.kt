package com.github.ajalt.mordant.table

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.components.Caption
import com.github.ajalt.mordant.components.Padding
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*

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
            hyperlink: String? = null
    ) {
        style = TextStyle(color, bgColor, bold, italic, underline, dim, inverse, strikethrough, hyperlink)
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
    object Auto : ColumnWidth()
    class Fixed(val width: Int) : ColumnWidth() {
        init {
            require(width > 0) { "width must be greater than zero" }
        }
    }

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
    var width: ColumnWidth = ColumnWidth.Auto
}

@MordantDsl
class TableBuilder internal constructor() {
    var borderStyle: BorderStyle = BorderStyle.SQUARE
    var borderTextStyle: TextStyle = DEFAULT_STYLE

    var padding: Padding = Padding.horizontal(1)
    var style: TextStyle = DEFAULT_STYLE
    var borders: Borders = Borders.ALL
    var align: TextAlign = TextAlign.LEFT
    var verticalAlign: VerticalAlign = VerticalAlign.TOP
    var overflowWrap: OverflowWrap = OverflowWrap.ELLIPSES
    var outerBorder: Boolean = true

    internal val columns = mutableMapOf<Int, ColumnBuilder>()
    internal val headerSection = SectionBuilder()
    internal val bodySection = SectionBuilder()
    internal val footerSection = SectionBuilder()
    internal var captionTop: Renderable? = null
    internal var captionBottom: Renderable? = null

    fun captionTop(renderable: Renderable) {
        captionTop = renderable
    }

    fun captionTop(text: String, style: TextStyle = DEFAULT_STYLE, align: TextAlign = TextAlign.CENTER) {
        captionTop(Text(text, style, align = align))
    }

    fun captionBottom(renderable: Renderable) {
        captionBottom = renderable
    }

    fun captionBottom(text: String, style: TextStyle = DEFAULT_STYLE, align: TextAlign = TextAlign.CENTER) {
        captionBottom(Text(text, style, align = align))
    }

    fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, init)

    fun header(init: SectionBuilder.() -> Unit) {
        headerSection.init()
    }

    fun body(init: SectionBuilder.() -> Unit) {
        bodySection.init()
    }

    fun footer(init: SectionBuilder.() -> Unit) {
        footerSection.init()
    }
}


@MordantDsl
class SectionBuilder internal constructor() : CellStyleBuilder by CellStyleBuilderMixin() {
    internal val rows = mutableListOf<RowBuilder>()
    internal val columns = mutableMapOf<Int, ColumnBuilder>()
    internal var rowStyles = listOf<TextStyle>()

    fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, init)

    fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        rowStyles = listOf(style1, style2) + styles.asList()
    }

    fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit = {}) {
        val cellBuilders = cells.mapTo(mutableListOf()) { CellBuilder(getRenderable(it)) }
        rows += RowBuilder(cellBuilders).apply(init)
    }

    fun row(vararg cells: Any?, init: RowBuilder.() -> Unit = {}) {
        rowFrom(cells.asList(), init)
    }

    fun row(init: RowBuilder.() -> Unit) {
        rows += RowBuilder(mutableListOf()).apply(init)
    }
}


@MordantDsl
class RowBuilder internal constructor(
        internal val cells: MutableList<CellBuilder>
) : CellStyleBuilder by CellStyleBuilderMixin() {
    fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellBuilder.() -> Unit = {}) {
        cell(cell1, init)
        cell(cell2, init)
        cells(cells.asList(), init)
    }

    fun cells(cells: Iterable<Any?>, init: CellBuilder.() -> Unit = {}) {
        cells.mapTo(this.cells) { CellBuilder(getRenderable(it)).apply(init) }
    }

    fun cell(content: Any?, init: CellBuilder.() -> Unit = {}) {
        cells += CellBuilder(getRenderable(content)).apply(init)
    }
}

@MordantDsl
class CellBuilder internal constructor(
        internal val content: Renderable = Text(""),
        rowSpan: Int = 1,
        columnSpan: Int = 1,
) : CellStyleBuilder by CellStyleBuilderMixin() {

    var columnSpan = columnSpan
        set(value) {
            require(value > 0) { "Column span must be greater than 0" }
            field = value
        }

    var rowSpan = rowSpan
        set(value) {
            require(value > 0) { "Row span must be greater than 0" }
            field = value
        }
}

fun table(init: TableBuilder.() -> Unit): Renderable {
    val tableBuilder = TableBuilder().apply(init)
    val table = TableBuilderLayout(tableBuilder).buildTable()
    return when {
        tableBuilder.captionTop != null || tableBuilder.captionBottom != null -> {
            Caption(table, tableBuilder.captionTop, tableBuilder.captionBottom)
        }
        else -> table
    }
}

fun grid(init: SectionBuilder.() -> Unit): Renderable {
    return table {
        borders = Borders.LEFT_RIGHT
        outerBorder = false
        borderStyle = BorderStyle.BLANK
        padding = Padding.none()
        body(init)
    }
}

private fun getRenderable(content: Any?): Renderable {
    if (content is Renderable) return content
    return when (val string = content.toString()) {
        "" -> EmptyRenderable
        else -> Text(string)
    }
}

private fun initColumn(columns: MutableMap<Int, ColumnBuilder>, i: Int, init: ColumnBuilder.() -> Unit) {
    require(i >= 0) { "column index cannot be negative" }
    var v = columns[i]
    if (v == null) {
        v = ColumnBuilder()
        columns[i] = v
    }
    v.init()
}
