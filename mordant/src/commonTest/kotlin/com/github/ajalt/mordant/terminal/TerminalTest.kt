package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.Whitespace
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class TerminalTest {
    private val vt = TerminalRecorder(width = 8)
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
        t.print("1")
        t.print("2", stderr=true)
        t.print("3")
        vt.stdout() shouldBe "13"
        vt.stderr() shouldBe "2"
        vt.output() shouldBe "123"
    }

    @Test
    fun println() {
        t.println("1")
        t.println("2", stderr=true)
        t.println("3")
        vt.stdout() shouldBe "1\n3\n"
        vt.stderr() shouldBe "2\n"
        vt.output() shouldBe "1\n2\n3\n"
    }

    @Test
    fun rawPrint() {
        t.rawPrint(t.cursor.getMoves { left(1) })
        t.rawPrint(t.cursor.getMoves { up(1) }, stderr=true)
        t.rawPrint(t.cursor.getMoves { right(1) })
        vt.stdout() shouldBe t.cursor.getMoves { left(1); right(1) }
        vt.stderr() shouldBe t.cursor.getMoves { up(1) }
        vt.output() shouldBe t.cursor.getMoves { left(1); up(1); right(1) }

        vt.clearOutput()
        t.rawPrint("\t")
        vt.output() shouldBe "\t"
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
