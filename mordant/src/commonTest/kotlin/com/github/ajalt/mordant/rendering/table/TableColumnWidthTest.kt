package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.OverflowWrap
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.Borders.ALL
import com.github.ajalt.mordant.table.Borders.LEFT_RIGHT
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.widgets.Padding
import kotlin.js.JsName
import kotlin.test.Test

class TableColumnWidthTest : RenderingTest() {
    @Test
    @JsName("exact_min")
    fun `exact min`() = doTest(18,
        "|11 |22 foo|33|44|"
    )

    @Test
    @JsName("expand_flex")
    fun `expand flex`() = doTest(26,
        "|11 |22 foo|33  |44      |"
    )

    @Test
    @JsName("expand_flex_partial_remainder")
    fun `expand flex partial remainder`() = doTest(27,
        "|11 |22 foo|33   |44      |"
    )

    @Test
    @JsName("expand_flex_equal_remainder")
    fun `expand flex equal remainder`() = doTest(28,
        "|11 |22 foo|33   |44       |"
    )

    @Test
    @JsName("shrink_auto_partial")
    fun `shrink auto partial`() = doTest(16,
        """
        |11 |22  |33|44|
        |   |foo |  |  |
        """
    )

    @Test
    @JsName("shrink_auto_max")
    fun `shrink auto max`() = doTest(15,
        """
        |11 |22 |33|44|
        |   |foo|  |  |
        """
    )

    @Test
    @JsName("shrink_flex_past_min")
    fun `shrink flex past min`() = doTest(13,
        """
        |11 |22 |3|4|
        |   |foo| | |
        """
    )

    @Test
    @JsName("shrink_flex_completely")
    fun `shrink flex completely`() = doTest(11,
        """
        |11 |22 |||
        |   |foo|||
        """
    )

    @Test
    @JsName("shrink_flex_and_partial_auto")
    fun `shrink flex and partial auto`() = doTest(9,
        """
        |11 |2|||
        |   |f|||
        """
    )

    @Test
    @JsName("shrink_auto_completely")
    fun `shrink auto completely`() = doTest(8,
        """
        |11 ||||
        """
    )

    @Test
    @JsName("shrink_fixed_partial")
    fun `shrink fixed partial`() = doTest(6,
        """
        |1||||
        """
    )

    @Test
    @JsName("shrink_fixed_completely")
    fun `shrink fixed completely`() = doTest(0,
        "|||||"
    )

    @Test
    @JsName("shrink_fixed_completely_with_row_borders")
    fun `shrink fixed completely with row borders`() = doTest(0,
        """
        +++++
        |||||
        +++++
        """, borders = ALL
    )

    private fun doTest(tableWidth: Int, expected: String, borders: Borders = LEFT_RIGHT) {
        checkRender(table {
            borderStyle = BorderStyle.ASCII
            padding = Padding.none()
            overflowWrap = OverflowWrap.TRUNCATE
            column(0) { width = ColumnWidth.Fixed(3) }
            column(2) { width = ColumnWidth.Expand() }
            column(3) { width = ColumnWidth.Expand(2) }
            body {
                this.borders = borders
                row {
                    cells(11, "22 foo", 33, 44)
                }
            }
        }, expected.trimIndent(), width = tableWidth)
    }
}
