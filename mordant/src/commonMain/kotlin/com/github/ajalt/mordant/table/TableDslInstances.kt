package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Text


private class CellStyleBuilderMixin : CellStyleBuilder {
    override var padding: Padding? = null
    override var style: TextStyle? = null
    override var cellBorders: Borders? = null
    override var whitespace: Whitespace? = null
    override var align: TextAlign? = null
    override var verticalAlign: VerticalAlign? = null
    override var overflowWrap: OverflowWrap? = null

    @Deprecated("borders has been renamed to cellBorders", replaceWith = ReplaceWith("cellBorders"))
    override var borders: Borders?
        get() = cellBorders
        set(value) {
            cellBorders = value
        }
}


internal class ColumnBuilderInstance : ColumnBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    override var width: ColumnWidth = ColumnWidth.Auto
}

@MordantDsl
internal class TableBuilderInstance : TableBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    override var borderType: BorderType = BorderType.SQUARE
    override var borderStyle: TextStyle = DEFAULT_STYLE
    override var tableBorders: Borders? = null

    val columns = mutableMapOf<Int, ColumnBuilder>()
    val headerSection = SectionBuilderInstance()
    val bodySection = SectionBuilderInstance()
    val footerSection = SectionBuilderInstance()
    var captionTop: Widget? = null
        private set
    var captionBottom: Widget? = null
        private set

    @Deprecated("`outerBorder=false` has been replaced with `tableBorders=Borders.NONE`")
    override var outerBorder: Boolean
        get() = tableBorders != Borders.ALL
        set(value) {
            tableBorders = if (value) Borders.ALL else Borders.NONE
        }

    override fun captionTop(widget: Widget) {
        captionTop = widget
    }

    override fun captionTop(text: String, align: TextAlign) {
        captionTop(Text(text, align = align))
    }

    override fun captionBottom(widget: Widget) {
        captionBottom = widget
    }

    override fun captionBottom(text: String, align: TextAlign) {
        captionBottom(Text(text, align = align))
    }

    override fun column(i: Int, init: ColumnBuilder.() -> Unit) {
        initColumn(columns, i, ColumnBuilderInstance(), init)
    }

    override fun header(init: SectionBuilder.() -> Unit) {
        headerSection.init()
    }

    override fun body(init: SectionBuilder.() -> Unit) {
        bodySection.init()
    }

    override fun footer(init: SectionBuilder.() -> Unit) {
        footerSection.init()
    }
}


@MordantDsl
internal class SectionBuilderInstance : SectionBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    val rows = mutableListOf<RowBuilderInstance>()
    val columns = mutableMapOf<Int, CellStyleBuilder>()
    var rowStyles = listOf<TextStyle>()

    override fun column(i: Int, init: CellStyleBuilder.() -> Unit) =
        initColumn(columns, i, ColumnBuilderInstance(), init)

    override fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        rowStyles = listOf(style1, style2) + styles.asList()
    }

    override fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit) {
        val cellBuilders = cells.mapTo(mutableListOf()) { CellBuilderInstance(getCellContent(it)) }
        rows += RowBuilderInstance(cellBuilders).apply(init)
    }

    override fun row(vararg cells: Any?, init: RowBuilder.() -> Unit) {
        rowFrom(cells.asList(), init)
    }

    override fun row(init: RowBuilder.() -> Unit) {
        rows += RowBuilderInstance(mutableListOf()).apply(init)
    }
}

@MordantDsl
internal class GridBuilderInstance(
    private val section: SectionBuilder,
) : GridBuilder, CellStyleBuilder by section {
    val columns = mutableMapOf<Int, ColumnBuilder>()

    override fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, ColumnBuilderInstance(), init)

    override fun rowStyles(style1: TextStyle, style2: TextStyle, vararg styles: TextStyle) {
        section.rowStyles(style1, style2, *styles)
    }

    override fun rowFrom(cells: Iterable<Any?>, init: RowBuilder.() -> Unit) {
        section.rowFrom(cells, init)
    }

    override fun row(vararg cells: Any?, init: RowBuilder.() -> Unit) {
        section.row(*cells, init = init)
    }

    override fun row(init: RowBuilder.() -> Unit) {
        section.row(init)
    }
}

@MordantDsl
internal class SingleRowBuilderInstance(
    private val section: SectionBuilder,
) : SingleRowBuilder, CellStyleBuilder by section {
    val columns = mutableMapOf<Int, ColumnBuilder>()
    val row = RowBuilderInstance(mutableListOf())

    override fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellStyleBuilder.() -> Unit) {
        row.cells(cell1, cell2, *cells, init = init)
    }

    override fun cellsFrom(cells: Iterable<Any?>, init: CellStyleBuilder.() -> Unit) {
        row.cellsFrom(cells, init)
    }

    override fun cell(content: Any?, init: CellStyleBuilder.() -> Unit) {
        row.cell(content, init)
    }

    override fun column(i: Int, init: ColumnBuilder.() -> Unit) = initColumn(columns, i, ColumnBuilderInstance(), init)
}

@MordantDsl
internal class SingleColumnBuilderInstance(
    private val section: SectionBuilder,
    private val defaultPadding: Int,
) : SingleColumnBuilder, CellStyleBuilder by section {
    override var width: ColumnWidth = ColumnWidth.Auto
    private var empty = true

    override fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellStyleBuilder.() -> Unit) {
        section.row(cell1, init = pad(init))
        section.row(cell2, init = pad(init))
        cells.forEach { section.row(it, init = pad(init)) }
    }

    override fun cellsFrom(cells: Iterable<Any?>, init: CellStyleBuilder.() -> Unit) {
        cells.forEach { section.row(it, init = pad(init)) }
    }

    override fun cell(content: Any?, init: CellStyleBuilder.() -> Unit) {
        section.row(content, init = pad(init))
    }

    private fun pad(init: CellStyleBuilder.() -> Unit): CellStyleBuilder.() -> Unit = {
        if (defaultPadding > 0) {
            if (empty) {
                empty = false
            } else {
                padding = Padding.of(top = defaultPadding)
            }
        }
        init()
    }
}

@MordantDsl
internal class RowBuilderInstance(
    val cells: MutableList<CellBuilderInstance>,
) : RowBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    override fun cells(cell1: Any?, cell2: Any?, vararg cells: Any?, init: CellBuilder.() -> Unit) {
        cell(cell1, init)
        cell(cell2, init)
        cellsFrom(cells.asList(), init)
    }

    override fun cellsFrom(cells: Iterable<Any?>, init: CellBuilder.() -> Unit) {
        cells.mapTo(this.cells) { CellBuilderInstance(getCellContent(it)).apply(init) }
    }

    override fun cell(content: Any?, init: CellBuilder.() -> Unit) {
        cells += CellBuilderInstance(getCellContent(content)).apply(init)
    }
}

internal sealed class CellContent {
    data class WidgetContent(val widget: Widget) : CellContent()
    data class TextContent(val text: String) : CellContent()
}

@MordantDsl
internal class CellBuilderInstance(
    val content: CellContent,
) : CellBuilder, CellStyleBuilder by CellStyleBuilderMixin() {

    override var columnSpan = 1
        set(value) {
            require(value > 0) { "Column span must be greater than 0" }
            field = value
        }

    override var rowSpan = 1
        set(value) {
            require(value > 0) { "Row span must be greater than 0" }
            field = value
        }
}

private fun getCellContent(content: Any?): CellContent {
    if (content is Widget) return CellContent.WidgetContent(content)
    return CellContent.TextContent(content.toString())
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
