package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import kotlin.test.Test

class PaddingTest : RenderingTest() {
    private val w = Text("x")

    @Test
    fun oneValue() = checkRender(w.withPadding(1), """
    |⏎
    | x ⏎
    |⏎
    """.trimMargin())

    @Test
    fun twoValues() = checkRender(w.withPadding(1, 2), """
    |⏎
    |  x  ⏎
    |⏎
    """.trimMargin())

    @Test
    fun threeValues() = checkRender(w.withPadding(1, 2, 3), """
    |⏎
    |  x  ⏎
    |⏎
    |⏎
    |⏎
    """.trimMargin())

    @Test
    fun fourValues() = checkRender(w.withPadding(1, 2, 3, 4), """
    |⏎
    |    x  ⏎
    |⏎
    |⏎
    |⏎
    """.trimMargin())

    @Test
    fun vertical() = checkRender(w.withVerticalPadding(1), """
    |⏎
    |x⏎
    |⏎
    """.trimMargin())

    @Test
    fun horizontal() = checkRender(w.withHorizontalPadding(1), """
    | x ⏎
    """.trimMargin())

    @Test
    fun padEmpty() = checkRender(Text("x\n\ny").withPadding(1, padEmptyLines = true), """
    |⏎
    | x ⏎
    |  ⏎
    | y ⏎
    |⏎
    """.trimMargin(), trimIndent = false)

    @Test
    fun noPadEmpty() = checkRender(Text("x\n\ny").withPadding(1, padEmptyLines = false), """
    |⏎
    | x ⏎
    |⏎
    | y ⏎
    |⏎
    """.trimMargin(), trimIndent = false)
}
