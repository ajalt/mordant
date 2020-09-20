package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.*

interface CellStyleBuilder {
    var padding: Padding?
    var style: TextStyle?
    var borderLeft: Boolean?
    var borderTop: Boolean?
    var borderRight: Boolean?
    var borderBottom: Boolean?

    /**
     * Set all cell borders at once.
     *
     * The property is `true` if all four borders are `true`.
     */
    var border: Boolean
        get() = borderLeft == true && borderTop == true && borderRight == true && borderBottom == true
        set(value) {
            borderLeft = value
            borderTop = value
            borderRight = value
            borderBottom = value
        }
}

private class CellStyleBuilderMixin : CellStyleBuilder {
    override var padding: Padding? = null
    override var style: TextStyle? = null
    override var borderLeft: Boolean? = null
    override var borderTop: Boolean? = null
    override var borderRight: Boolean? = null
    override var borderBottom: Boolean? = null
}

@DslMarker
annotation class MordantDsl

@MordantDsl
class ColumnBuilder internal constructor() : CellStyleBuilder by CellStyleBuilderMixin() {
    var width: ColumnWidth = ColumnWidth.Default
}

@MordantDsl
class TableBuilder internal constructor() {
    var expand: Boolean = false
    var borders: Borders? = Borders.SQUARE
    var borderStyle: TextStyle = DEFAULT_STYLE
    var padding: Padding = Padding.horizontal(1)
    var textStyle: TextStyle? = null

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

    fun row(cells: Iterable<String>, init: RowBuilder.() -> Unit = {}) {
        row(cells.map { Text(it) }, init)
    }

    // TODO: allow `Any?`
    fun row(vararg cells: String, init: RowBuilder.() -> Unit = {}) {
        row(cells.asList(), init)
    }

    fun row(vararg cells: Renderable, init: RowBuilder.() -> Unit = {}) {
        row(cells.asList(), init)
    }

    @JvmName("renderableRow")
    fun row(cells: Iterable<Renderable>, init: RowBuilder.() -> Unit = {}) {
        rows += RowBuilder(cells.mapTo(mutableListOf()) { CellBuilder(it) }).apply(init).validate()
    }

    fun row(init: RowBuilder.() -> Unit) {
        rows += RowBuilder(mutableListOf()).apply(init).validate()
    }
}


@MordantDsl
class RowBuilder internal constructor(
        internal val cells: MutableList<CellBuilder>
) : CellStyleBuilder by CellStyleBuilderMixin() {
    fun cells(cells: Iterable<String>, init: CellBuilder.() -> Unit = {}) {
        cells(cells.map { Text(it) }, init)
    }

    fun cells(cell1: String, cell2: String, vararg cells: String, init: CellBuilder.() -> Unit = {}) {
        this.cells += CellBuilder(Text(cell1)).apply(init)
        this.cells += CellBuilder(Text(cell2)).apply(init)
        cells(cells.asList(), init)
    }

    @JvmName("renderableCells")
    fun cells(cells: Iterable<Renderable>, init: CellBuilder.() -> Unit = {}) {
        cells.mapTo(this.cells) { CellBuilder(it).apply(init) }
    }

    fun cell(content: Renderable, init: CellBuilder.() -> Unit = {}) {
        cells += CellBuilder(content).apply(init)
    }

    fun cell(content: String, init: CellBuilder.() -> Unit = {}) = cell(Text(content), init)

    internal fun validate(): RowBuilder {
        require(cells.isNotEmpty()) { "A row cannot be empty" }
        return this
    }
}

// TODO: alignment / gravity
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

fun table(init: TableBuilder.() -> Unit): Table {
    return TableBuilderLayout(TableBuilder().apply(init)).buildTable()
}
