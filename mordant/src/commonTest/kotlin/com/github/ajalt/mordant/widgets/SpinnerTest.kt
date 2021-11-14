package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SpinnerTest : RenderingTest() {
    @Test
    fun dots() = doTest(Spinner.Dots(), "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")

    @Test
    fun line() = doTest(Spinner.Lines(), "|", "/", "-", "\\")

    @Test
    fun custom() = doTest(Spinner("ab"), "a", "b", "a", "b")

    @Test
    fun duration() = doTest(Spinner("12", duration = 2), "1", "1", "2", "2", "1", "1", "2", "2")

    @Test
    fun lineStyle() = doTest(Spinner.Lines(red), red("|"), red("/"), red("-"), red("\\"))

    @Test
    fun initialTick() = doTest(Spinner.Lines(initial = 2), "-", "\\", "|", "/")

    private fun doTest(
        spinner: Spinner,
        vararg expected: String,
    ) {
        for (ex in expected) {
            checkRender(spinner, ex)
            val t = spinner.tick
            spinner.advanceTick() shouldBe (t + 1)
        }
    }
}
