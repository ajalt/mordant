package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.*


@DslMarker
annotation class MordantDsl

@MordantDsl
class ColumnBuilder {
    var width: ColumnWidth = ColumnWidth.Default
    var style: TextStyle = DEFAULT_STYLE
    var padding: Padding? = null
}

@MordantDsl
class TableBuilder {
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
class SectionBuilder {
    internal val rows = mutableListOf<RowBuilder>()
    internal var rowStyles = listOf<TextStyle>()

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
        rows += RowBuilder(cells.mapTo(mutableListOf()) { CellBuilder(it) }).apply(init)
    }

    fun row(init: RowBuilder.() -> Unit) {
        rows += RowBuilder(mutableListOf(), DEFAULT_STYLE, null).apply(init)
    }
}


@MordantDsl
class RowBuilder internal constructor(
        internal val cells: MutableList<CellBuilder>,
        var style: TextStyle? = null,
        var padding: Padding? = null
) {
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
}

// TODO: alignment
@MordantDsl
class CellBuilder internal constructor(
        internal val content: Renderable = Text(""),
        rowSpan: Int = 1,
        columnSpan: Int = 1,
        var borderLeft: Boolean = true,
        var borderTop: Boolean = true,
        var borderRight: Boolean = true,
        var borderBottom: Boolean = true,
        var padding: Padding? = null,
        var style: TextStyle? = null,
) {

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

    /**
     * Set all cell borders at once.
     *
     * The property is `true` if all four borders are `true`, and `false` if at least one border is `false`.
     */
    var border: Boolean
        get() = borderLeft && borderTop && borderRight && borderBottom
        set(value) {
            borderLeft = value
            borderTop = value
            borderRight = value
            borderBottom = value
        }
}

fun table(init: TableBuilder.() -> Unit): Table {
    return TableBuilderLayout(TableBuilder().apply(init)).buildTable()
}
