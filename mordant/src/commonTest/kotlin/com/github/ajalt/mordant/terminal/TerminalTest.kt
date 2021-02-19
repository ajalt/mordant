package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.Whitespace
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class TerminalTest {
    private val vt = VirtualTerminalInterface(width = 8)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun success() {
        t.success("success")
        vt.buffer() shouldBe t.theme.success("success") + "\n"
        t.renderSuccess("success") shouldBe t.theme.success("success")
    }

    @Test
    fun danger() {
        t.danger("danger")
        vt.buffer() shouldBe t.theme.danger("danger") + "\n"
        t.renderDanger("danger") shouldBe t.theme.danger("danger")
    }

    @Test
    fun warning() {
        t.warning("warning")
        vt.buffer() shouldBe t.theme.warning("warning") + "\n"
        t.renderWarning("warning") shouldBe t.theme.warning("warning")
    }

    @Test
    fun info() {
        t.info("info")
        vt.buffer() shouldBe t.theme.info("info") + "\n"
        t.renderInfo("info") shouldBe t.theme.info("info")
    }

    @Test
    fun muted() {
        t.muted("muted")
        vt.buffer() shouldBe t.theme.muted("muted") + "\n"
        t.renderMuted("muted") shouldBe t.theme.muted("muted")
    }

    @Test
    fun print() {
        t.print("print")
        vt.buffer() shouldBe "print"
    }

    @Test
    fun println() {
        t.println("println")
        vt.buffer() shouldBe "println\n"
    }

    @Test
    fun `print customized`() {
        t.print("print with a wrap", style = cyan, whitespace = Whitespace.NORMAL, align = TextAlign.RIGHT)
        vt.buffer() shouldBe """
        |${cyan("  print")}
        |${cyan(" with a")}
        |${cyan("    wrap")}
        """.trimMargin()
    }
}
