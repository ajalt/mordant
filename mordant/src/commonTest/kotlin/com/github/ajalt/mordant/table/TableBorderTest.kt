package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.test.Test

@Suppress("TestFunctionName")
class TableBorderTest : RenderingTest() {
    @Test
    fun NONE() = doTest(NONE,
        """
        ░ × 
        """
    )

    @Test
    fun BOTTOM() = doTest(BOTTOM,
        """
        ░ × 
        ░───
        """
    )

    @Test
    fun RIGHT() = doTest(RIGHT,
        """
        ░ × │
        """
    )

    @Test
    fun BOTTOM_RIGHT() = doTest(BOTTOM_RIGHT,
        """
        ░ × │
        ░───┘
        """
    )

    @Test
    fun TOP() = doTest(TOP,
        """
        ░───
        ░ × 
        """
    )

    @Test
    fun TOM_BOTTOM() = doTest(TOM_BOTTOM,
        """
        ░───
        ░ × 
        ░───
        """
    )

    @Test
    fun TOP_RIGHT() = doTest(TOP_RIGHT,
        """
        ░───┐
        ░ × │
        """
    )

    @Test
    fun TOP_RIGHT_BOTTOM() = doTest(TOP_RIGHT_BOTTOM,
        """
        ░───┐
        ░ × │
        ░───┘
        """
    )

    @Test
    fun LEFT() = doTest(LEFT,
        """
        ░│ × 
        """
    )

    @Test
    fun LEFT_BOTTOM() = doTest(LEFT_BOTTOM,
        """
        ░│ × 
        ░└───
        """
    )

    @Test
    fun LEFT_RIGHT() = doTest(LEFT_RIGHT,
        """
        ░│ × │
        """
    )

    @Test
    fun LEFT_RIGHT_BOTTOM() = doTest(LEFT_RIGHT_BOTTOM,
        """
        ░│ × │
        ░└───┘
        """
    )

    @Test
    fun LEFT_TOP() = doTest(LEFT_TOP,
        """
        ░┌───
        ░│ × 
        """
    )

    @Test
    fun LEFT_TOP_BOTTOM() = doTest(LEFT_TOP_BOTTOM,
        """
        ░┌───
        ░│ × 
        ░└───
        """
    )

    @Test
    fun LEFT_TOP_RIGHT() = doTest(LEFT_TOP_RIGHT,
        """
        ░┌───┐
        ░│ × │
        """
    )

    @Test
    fun ALL() = doTest(ALL,
        """
        ░┌───┐
        ░│ × │
        ░└───┘
        """
    )

    private fun doTest(borders: Borders, expected: String) = checkRender(table {
        this.cellBorders = borders
        body {
            row("×")
        }
    }, expected)
}
