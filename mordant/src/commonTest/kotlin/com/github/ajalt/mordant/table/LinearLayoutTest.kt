package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
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
    @JsName("verticalLayout_justify")
    fun `verticalLayout justify`() = checkRender(
        verticalLayout {
            align = TextAlign.JUSTIFY
            spacing = 1
            cell("1")
            cell("2 2")
            cell("3333")
        }, """
        ░ 1  ░
        ░    ░
        ░2  2░
        ░    ░
        ░3333░
        """
    )

    @Test
    @JsName("verticalLayout_left_align")
    fun `verticalLayout left align`() {
        class W(val w: Int) : Widget {
            override fun measure(t: Terminal, width: Int): WidthRange {
                return WidthRange(w, w)
            }

            override fun render(t: Terminal, width: Int): Lines {
                return Lines(listOf(Line(listOf(Span.word("=".repeat(w))))))
            }
        }

        checkRender(
            verticalLayout {
                align = TextAlign.LEFT
                cell(W(2))
                cell(W(4))
            }, """
            ░==  ░
            ░====░
            """
        )
    }

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

    @Test
    @JsName("nesting_horizontalLayouts_in_verticalLayouts_with_fixed_column_width")
    fun `nesting horizontalLayouts in verticalLayouts with fixed column width`() = checkRender(
        verticalLayout {
            cell(horizontalLayout {
                spacing = 2
                align = TextAlign.RIGHT
                column(0) { width = ColumnWidth.Fixed(4) }
                cells("1", "1")
            })
            cell(horizontalLayout {
                spacing = 2
                align = TextAlign.RIGHT
                column(0) { width = ColumnWidth.Fixed(4) }
                cells("222", "2")
            })
        }, """
        ░   1  1░
        ░ 222  2░
        """
    )
}
