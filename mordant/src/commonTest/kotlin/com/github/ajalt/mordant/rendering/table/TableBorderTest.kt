package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.test.Test

@Suppress("TestFunctionName")
class TableBorderTest : RenderingTest() {
    @Test
    fun NONE() = doTest(NONE,
        """
        | × 
        """.trimMargin()
    )

    @Test
    fun BOTTOM() = doTest(BOTTOM,
        """
        | × 
        |───
        """.trimMargin()
    )

    @Test
    fun RIGHT() = doTest(RIGHT,
        """
        | × │
        """.trimMargin()
    )

    @Test
    fun BOTTOM_RIGHT() = doTest(BOTTOM_RIGHT,
        """
        | × │
        |───┘
        """.trimMargin()
    )

    @Test
    fun TOP() = doTest(TOP,
        """
        |───
        | × 
        """.trimMargin()
    )

    @Test
    fun TOM_BOTTOM() = doTest(TOM_BOTTOM,
        """
        |───
        | × 
        |───
        """.trimMargin()
    )

    @Test
    fun TOP_RIGHT() = doTest(TOP_RIGHT,
        """
        |───┐
        | × │
        """.trimMargin()
    )

    @Test
    fun TOP_RIGHT_BOTTOM() = doTest(TOP_RIGHT_BOTTOM,
        """
        |───┐
        | × │
        |───┘
        """.trimMargin()
    )

    @Test
    fun LEFT() = doTest(LEFT,
        """
        |│ × 
        """.trimMargin()
    )

    @Test
    fun LEFT_BOTTOM() = doTest(LEFT_BOTTOM,
        """
        |│ × 
        |└───
        """.trimMargin()
    )

    @Test
    fun LEFT_RIGHT() = doTest(LEFT_RIGHT,
        """
        |│ × │
        """.trimMargin()
    )

    @Test
    fun LEFT_RIGHT_BOTTOM() = doTest(LEFT_RIGHT_BOTTOM,
        """
        |│ × │
        |└───┘
        """.trimMargin()
    )

    @Test
    fun LEFT_TOP() = doTest(LEFT_TOP,
        """
        |┌───
        |│ × 
        """.trimMargin()
    )

    @Test
    fun LEFT_TOP_BOTTOM() = doTest(LEFT_TOP_BOTTOM,
        """
        |┌───
        |│ × 
        |└───
        """.trimMargin()
    )

    @Test
    fun LEFT_TOP_RIGHT() = doTest(LEFT_TOP_RIGHT,
        """
        |┌───┐
        |│ × │
        """.trimMargin()
    )

    @Test
    fun ALL() = doTest(ALL,
        """
        |┌───┐
        |│ × │
        |└───┘
        """.trimMargin()
    )

    private fun doTest(borders: Borders, expected: String) = checkRender(table {
        this.borders = borders
        body {
            row("×")
        }
    }, expected, trimIndent = false)
}
