package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiLevel
import com.github.ajalt.mordant.Terminal
import io.kotest.matchers.shouldBe

abstract class RenderingTest(
        level: AnsiLevel = AnsiLevel.TRUECOLOR,
        theme: Theme = DEFAULT_THEME,
        width: Int = 79
) {
    protected var t = Terminal(level, theme, width)

    protected fun checkRender(renderable: Renderable, expected: String, trimIndent: Boolean = true) {
        val actual = t.render(renderable)
        try {
            actual shouldBe if (trimIndent) expected.trimIndent() else expected
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
