package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.BorderStyle
import com.github.ajalt.mordant.rendering.Padding
import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.table.Borders.LEFT_RIGHT
import org.junit.Test

class TableColumnWidthTest : RenderingTest() {
    @Test
    fun `exact min`() = doTest(18,
            "|11 |22 foo|33|44|"
    )

    @Test
    fun `expand flex`() = doTest(26,
            "|11 |22 foo|33  |44      |"
    )

    @Test
    fun `expand flex partial remainder`() = doTest(27,
            "|11 |22 foo|33   |44      |"
    )

    @Test
    fun `expand flex equal remainder`() = doTest(28,
            "|11 |22 foo|33   |44       |"
    )

    @Test
    fun `shrink auto partial`() = doTest(16,
            """
            |11 |22  |33|44|
            |   |foo |  |  |
            """
    )

    @Test
    fun `shrink auto max`() = doTest(15,
            """
            |11 |22 |33|44|
            |   |foo|  |  |
            """
    )

    @Test
    fun `shrink flex past min`() = doTest(13,
            """
            |11 |22 |3|4|
            |   |foo| | |
            """
    )

    @Test
    fun `shrink flex completely`() = doTest(11,
            """
            |11 |22 |||
            |   |foo|||
            """
    )

    @Test
    fun `shrink flex and partial auto`() = doTest(9,
            """
            |11 |2|||
            |   |f|||
            """
    )

    @Test
    fun `shrink auto completely`() = doTest(8,
            """
            |11 ||||
            """
    )

    @Test
    fun `shrink fixed partial`() = doTest(6,
            """
            |1||||
            """
    )

    @Test
    fun `shrink fixed completely`() = doTest(0,
            "|||||"
    )

    private fun doTest(tableWidth: Int, expected: String) {
        t = Terminal(width = tableWidth)
        checkRender(table {
            borderStyle = BorderStyle.ASCII
            padding = Padding.none()
            column(0) { width = ColumnWidth.Fixed(3) }
            column(2) { width = ColumnWidth.Expand() }
            column(3) { width = ColumnWidth.Expand(2) }
            body {
                row {
                    borders = LEFT_RIGHT
                    cells(11, "22 foo", 33, 44)
                }
            }
        }, expected)
    }
}
