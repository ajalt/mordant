package com.github.ajalt.mordant.test

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import io.kotest.matchers.shouldBe

abstract class RenderingTest(
    private val width: Int = 79,
) {
    protected fun checkRender(
        widget: Widget,
        expected: String,
        trimMargin: Boolean = true,
        width: Int = this.width,
        height: Int = 24,
        tabWidth: Int = 8,
        hyperlinks: Boolean = true,
        theme: Theme = Theme.Default,
        transformActual: (String) -> String = { it },
    ) {
        val t = Terminal(AnsiLevel.TRUECOLOR, theme, width, height, hyperlinks, tabWidth)
        val actual = transformActual(t.render(widget))
        actual.shouldMatchRender(expected, trimMargin)
    }
}
