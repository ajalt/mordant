package com.github.ajalt.mordant.test

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import io.kotest.matchers.shouldBe
import kotlin.test.assertEquals

abstract class RenderingTest(
    private val width: Int = 79,
) {
    protected fun checkRender(
        widget: Widget,
        expected: String,
        trimIndent: Boolean = true,
        width: Int = this.width,
        height: Int = 24,
        tabWidth: Int = 8,
        hyperlinks: Boolean = true,
        theme: Theme = Theme.Default,
        transformActual: (String) -> String = { it },
    ) = threadedTest {
        val t = Terminal(AnsiLevel.TRUECOLOR, theme, width, height, hyperlinks, tabWidth)
        val actual = transformActual(t.render(widget))
        try {
            val trimmed = if (trimIndent) expected.trimIndent() else expected
            actual shouldBe trimmed.replace("⏎", "")
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
