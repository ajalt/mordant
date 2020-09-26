package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.*

interface CellStyleBuilder {
    var padding: Padding?
    var style: TextStyle?
    var borders: Borders?
    var align: TextAlign?
    var verticalAlign: VerticalAlign?
}

private class CellStyleBuilderMixin : CellStyleBuilder {
    override var padding: Padding? = null
    override var style: TextStyle? = null
    override var borders: Borders? = null
    override var align: TextAlign? = null
    override var verticalAlign: VerticalAlign? = null
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
        constructor(weight: Int): this(weight.toFloat())
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

    internal val columns = mutableMapOf<Int, ColumnBuilder>()
    internal val headerSection = SectionBuilder()
    internal val bodySection = SectionBuilder()
    internal val footerSection = SectionBuilder()

    fun column(i: Int, init: ColumnBuilder.() -> Unit) {
        var v = columns[i]
        if (v == null) {
            v = ColumnBuilder()
            columns[i] = v
        }
        v.init()
    }

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
    internal var rowStyles = listOf<TextStyle>()

    // TODO: test
    fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        rowStyles = listOf(style1 + style2) + styles.asList()
    }

    fun row(cells: Iterable<Any?>, init: RowBuilder.() -> Unit = {}) {
        val cellBuilders = cells.mapTo(mutableListOf()) { CellBuilder(getRenderable(it)) }
        rows += RowBuilder(cellBuilders).apply(init)
    }

    fun row(vararg cells: Any?, init: RowBuilder.() -> Unit = {}) {
        row(cells.asList(), init)
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

    // TODO: rename
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
    return TableBuilderLayout(TableBuilder().apply(init)).buildTable()
}

private fun getRenderable(content: Any?): Renderable {
    if (content is Renderable) return content
    return when (val string = content.toString()) {
        "" -> EmptyRenderable
        else -> Text(string)
    }
}
