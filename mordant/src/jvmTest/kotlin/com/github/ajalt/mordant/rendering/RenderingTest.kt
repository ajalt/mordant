package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.internal.generateHyperlinkId
import io.kotest.matchers.shouldBe

abstract class RenderingTest(
        private val level: AnsiLevel = AnsiLevel.TRUECOLOR,
        private val theme: Theme = DEFAULT_THEME,
        private val width: Int = 79
) {
    init {
        generateHyperlinkId = { "x" }
    }

    protected fun checkRender(
            renderable: Renderable,
            expected: String,
            trimIndent: Boolean = true,
            width: Int = this.width,
            height: Int = 24,
            tabWidth: Int = 8,
            hyperlinks: Boolean = true,
            transformActual: (String) -> String = { it }
    ) {
        val t = Terminal(level, theme, width, height, hyperlinks, tabWidth)
        val actual = transformActual(t.render(renderable))
        try {
            val trimmed = if (trimIndent) expected.trimIndent() else expected
            actual shouldBe trimmed.replace("⏎", "")
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
