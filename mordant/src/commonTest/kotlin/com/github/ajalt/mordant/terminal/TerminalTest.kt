package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.Whitespace
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class TerminalTest {
    private val vt = VirtualTerminalInterface(width = 8)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun success() {
        t.success("success")
        vt.output() shouldBe t.theme.success("success") + "\n"
    }

    @Test
    fun danger() {
        t.danger("danger")
        vt.output() shouldBe t.theme.danger("danger") + "\n"
    }

    @Test
    fun warning() {
        t.warning("warning")
        vt.output() shouldBe t.theme.warning("warning") + "\n"
    }

    @Test
    fun info() {
        t.info("info")
        vt.output() shouldBe t.theme.info("info") + "\n"
    }

    @Test
    fun muted() {
        t.muted("muted")
        vt.output() shouldBe t.theme.muted("muted") + "\n"
    }

    @Test
    fun print() {
        t.print("print")
        vt.output() shouldBe "print"
    }

    @Test
    fun println() {
        t.println("println")
        vt.output() shouldBe "println\n"
    }

    @Test
    @JsName("print_customized")
    fun `print customized`() {
        t.print(cyan("print with a wrap"), whitespace = Whitespace.NORMAL, align = TextAlign.RIGHT)
        vt.output() shouldBe """
        |${cyan("  print")}
        |${cyan(" with a")}
        |${cyan("    wrap")}
        """.trimMargin()
    }
}
