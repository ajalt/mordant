package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import kotlin.test.Test

class PaddingTest : RenderingTest() {
    private val w = Text("x")

    @Test
    fun all() = checkRender(
        w.withPadding(1), """
    ░░
    ░ x ░
    ░░
    """
    )

    @Test
    fun fourValues() = checkRender(
        w.withPadding(1, 2, 3, 4), """
    ░░
    ░    x  ░
    ░░
    ░░
    ░░
    """
    )

    @Test
    fun vertical() = checkRender(
        w.withPadding { vertical = 1 }, """
    ░░
    ░x░
    ░░
    """
    )

    @Test
    fun horizontal() = checkRender(
        w.withPadding { horizontal = 1 }, """
    ░ x ░
    """
    )

    @Test
    fun padEmpty() = checkRender(
        Text("x\n\ny").withPadding(1, padEmptyLines = true), """
    ░░
    ░ x ░
    ░  ░
    ░ y ░
    ░░
    """
    )

    @Test
    fun noPadEmpty() = checkRender(
        Text("x\n\ny").withPadding(1, padEmptyLines = false), """
    ░░
    ░ x ░
    ░░
    ░ y ░
    ░░
    """
    )

    @Test
    fun withTopPadding() = checkRender(
        w.withPadding { top = 1 }, """
    ░░
    ░x░
    """
    )

    @Test
    fun withRightPadding() = checkRender(
        w.withPadding { right = 1 }, """
    ░x ░
    """
    )

    @Test
    fun withBottomPadding() = checkRender(
        w.withPadding { bottom = 1 }, """
    ░x░
    ░░
    """
    )

    @Test
    fun withLeftPadding() = checkRender(
        w.withPadding { left = 1 }, """
    ░ x░
    """
    )
}
