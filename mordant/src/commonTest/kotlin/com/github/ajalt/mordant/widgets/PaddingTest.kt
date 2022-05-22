package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import kotlin.test.Test

class PaddingTest : RenderingTest() {
    private val w = Text("x")

    @Test
    fun oneValue() = checkRender(w.withPadding(1), """
    ░░
    ░ x ░
    ░░
    """)

    @Test
    fun twoValues() = checkRender(w.withPadding(1, 2), """
    ░░
    ░  x  ░
    ░░
    """)

    @Test
    fun threeValues() = checkRender(w.withPadding(1, 2, 3), """
    ░░
    ░  x  ░
    ░░
    ░░
    ░░
    """)

    @Test
    fun fourValues() = checkRender(w.withPadding(1, 2, 3, 4), """
    ░░
    ░    x  ░
    ░░
    ░░
    ░░
    """)

    @Test
    fun vertical() = checkRender(w.withVerticalPadding(1), """
    ░░
    ░x░
    ░░
    """)

    @Test
    fun horizontal() = checkRender(w.withHorizontalPadding(1), """
    ░ x ░
    """)

    @Test
    fun padEmpty() = checkRender(Text("x\n\ny").withPadding(1, padEmptyLines = true), """
    ░░
    ░ x ░
    ░  ░
    ░ y ░
    ░░
    """)

    @Test
    fun noPadEmpty() = checkRender(Text("x\n\ny").withPadding(1, padEmptyLines = false), """
    ░░
    ░ x ░
    ░░
    ░ y ░
    ░░
    """)
}
