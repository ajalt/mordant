package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TextColors.brightBlue
import com.github.ajalt.mordant.terminal.TextColors.gray
import io.kotest.matchers.shouldBe
import org.junit.Test

class ProgressBarTest {
    @Test
    fun `0 percent complete`() = doPercentTest(0, "     ")

    @Test
    fun `10 percent complete`() = doPercentTest(10, "     ")

    @Test
    fun `20 percent complete`() = doPercentTest(20, "#>   ")

    @Test
    fun `30 percent complete`() = doPercentTest(30, "#>   ")

    @Test
    fun `40 percent complete`() = doPercentTest(40, "##>  ")

    @Test
    fun `60 percent complete`() = doPercentTest(60, "###> ")

    @Test
    fun `80 percent complete`() = doPercentTest(80, "####>")

    @Test
    fun `99 percent complete`() = doPercentTest(99, "####>")

    @Test
    fun `100 percent complete`() = doPercentTest(100, "#####")

    @Test
    fun `default theme`() {
        Terminal(AnsiLevel.TRUECOLOR, width = 5).render(
            ProgressBar(
                completed = 40,
            )
        ) shouldBe "${brightBlue("━━")} ${gray("━━")}"
    }

    @Test
    fun update() {
        val t = Terminal(theme = Theme.PlainAscii, width = 5)
        val p = ProgressBar()
        t.render(p) shouldBe "     "
        p.update(20)
        t.render(p) shouldBe "#>   "
        p.update(1, 2)
        t.render(p) shouldBe "##>  "
        p.percentComplete shouldBe 0.5f
    }

    private fun doPercentTest(completed: Int, expected: String) {
        val t = Terminal(theme = Theme.PlainAscii, width = 5)
        t.render(
            ProgressBar(completed = completed)
        ) shouldBe expected
    }
}
