package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.VerticalAlign.MIDDLE
import com.github.ajalt.mordant.rendering.Whitespace.*
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.ProgressBar
import com.github.ajalt.mordant.widgets.Text
import kotlin.js.JsName
import kotlin.test.Test

class LinearLayoutTest : RenderingTest() {
    @Test
    fun horizontalLayoutEmpty() = checkRender(
        horizontalLayout {}, "", width = 10
    )

    @Test
    fun horizontalLayout() = checkRender(
        horizontalLayout {
            column(1) { width = ColumnWidth.Expand() }
            val bar = ProgressBar(
                indeterminate = true,
                showPulse = false,
                indeterminateStyle = TextStyle()
            )
            cells(1, bar, 2)
        }, """
        ░1 ━━━━━━ 2░
        """, width = 10
    )

    @Test
    fun horizontalLayoutSpacing() = checkRender(
        horizontalLayout {
            spacing = 2
            column(1) { width = ColumnWidth.Expand() }
            val bar = ProgressBar(
                indeterminate = true,
                showPulse = false,
                indeterminateStyle = TextStyle()
            )
            cells(1, bar, 2)
        }, """
        ░1  ━━━━  2░
        """, width = 10
    )

    @Test
    fun verticalLayoutEmpty() = checkRender(
        verticalLayout {}, """
        ░
        """
    )

    @Test
    fun verticalLayoutSimple() = checkRender(
        verticalLayout {
            cell("1111")
            cells("2", "3")
            cellsFrom(listOf(Text("4 4")))
        }, """
        ░1111░
        ░2░
        ░3░
        ░4 4░
        """
    )

    @Test
    fun verticalLayoutCellAlign() = checkRender(
        verticalLayout {
            cell("1111")
            cells("2", "3") { align = TextAlign.RIGHT }
            cell(Text("4", align = TextAlign.LEFT))
            cellsFrom(listOf(Text("5 5")))
        }, """
        ░1111░
        ░   2░
        ░   3░
        ░4   ░
        ░5 5░
        """
    )

    @Test
    fun verticalLayoutCellAlignExpand() = checkRender(
        verticalLayout {
            width = ColumnWidth.Expand()
            align = TextAlign.RIGHT
            cell("1")
            cell("2") { align = TextAlign.RIGHT }
            cell(Text("3", align = TextAlign.LEFT))
        }, """
        ░     1░
        ░     2░
        ░3     ░
        """,
        width = 6
    )

    @Test
    fun verticalLayoutPadding() = checkRender(
        verticalLayout {
            spacing = 1
            cell("1")
            cell("2")
            cell("3")
        }, """
        ░1░
        ░░
        ░2░
        ░░
        ░3░
        """
    )

    @Test
    fun verticalLayoutLeftAlign() = checkRender(
        verticalLayout {
            align = TextAlign.LEFT
            spacing = 1
            cell("1")
            cell("2")
            cell("33")
        }, """
        ░1 ░
        ░  ░
        ░2 ░
        ░  ░
        ░33░
        """
    )

    @Test
    fun verticalLayoutFixedTruncation() = checkRender(
        verticalLayout {
            whitespace = Whitespace.NOWRAP
            cell("1111 222")
            width = ColumnWidth.Fixed(6)
        }, """
        ░1111 2░
        """
    )


    @Test
    @JsName("nesting_horizontalLayouts_in_verticalLayouts")
    fun `nesting horizontalLayouts in verticalLayouts`() = checkRender(
        verticalLayout {
            cell(horizontalLayout { cells("1", "1") })
            cell(horizontalLayout { cells("222", "2") })

        }, """
        ░1 1░
        ░222 2░
        """
    )
}
