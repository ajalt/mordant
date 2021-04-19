package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Text


private class CellStyleBuilderMixin : CellStyleBuilder {
    override var padding: Padding? = null
    override var style: TextStyle? = null
    override var borders: Borders? = null
    override var whitespace: Whitespace? = null
    override var align: TextAlign? = null
    override var verticalAlign: VerticalAlign? = null
    override var overflowWrap: OverflowWrap? = null
}


internal class ColumnBuilderInstance : ColumnBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    override var width: ColumnWidth = ColumnWidth.Auto
}

@MordantDsl
internal class TableBuilderInstance : TableBuilder, CellStyleBuilder by CellStyleBuilderMixin() {
    override var borderStyle: BorderStyle = BorderStyle.SQUARE
    override var borderTextStyle: TextStyle = DEFAULT_STYLE
    override var outerBorder: Boolean = true

    val columns = mutableMapOf<Int, ColumnBuilder>()
    val headerSection = SectionBuilderInstance()
    val bodySection = SectionBuilderInstance()
    val footerSection = SectionBuilderInstance()
    var captionTop: Widget? = null
        private set
    var captionBottom: Widget? = null
        private set

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
