package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.normalizedOutput
import com.github.ajalt.mordant.test.visibleCrLf
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class AnimationTest {
    private val rec = TerminalRecorder(width = 24)
    private val t = Terminal(terminalInterface = rec)

    @[Test JsName("no_trailing_linebreak")]
    fun `no trailing linebreak`() {
        val a = t.textAnimation<Int>(trailingLinebreak = false) { "<$it>\n===" }
        a.update(1)
        rec.normalizedOutput() shouldBe "<1>\n==="

        // update
        rec.clearOutput()
        a.update(2)
        val moves = t.cursor.getMoves { startOfLine(); up(1) }
        rec.normalizedOutput() shouldBe "${moves}<2>\n==="
    }

    @[Test JsName("no_trailing_linebreak_single_line")]
    fun `no trailing linebreak single line`() {
        val a = t.textAnimation<Int>(trailingLinebreak = false) { "<$it>" }
        a.update(1)
        rec.normalizedOutput() shouldBe "<1>"

        // update
        rec.clearOutput()
        a.update(2)
        val moves = t.cursor.getMoves { startOfLine() }
        rec.normalizedOutput() shouldBe "${moves}<2>"
    }

    @[Test JsName("animation_size_change")]
    fun `animation size change`() {
        val a = t.textAnimation<String> { it }
        a.update("1")
        rec.normalizedOutput() shouldBe "1"

        // update
        rec.clearOutput()
        a.update("2\n3")
        val moves = t.cursor.getMoves { startOfLine() }
        rec.normalizedOutput() shouldBe "${moves}2\n3"
    }

    @Test
    fun crClearsLine() {
        val rec = TerminalRecorder(width = 24, supportsAnsiCursor = true)
        val t = Terminal(terminalInterface = rec)
        val a = t.textAnimation<Int> { "$it" }
        a.update(1)
        rec.normalizedOutput() shouldBe "1"
        a.update(2)
        rec.normalizedOutput() shouldBe "1\r2"
        a.stop()
        rec.normalizedOutput() shouldBe "1\r2\n"
    }

    @[Test JsName("print_during_animation")]
    fun `print during animation`() {
        val a = t.textAnimation<Int> { "<$it>\n===" }
        a.update(1)
        rec.normalizedOutput() shouldBe "<1>\n==="

        // update
        rec.clearOutput()
        a.update(2)
        var moves = t.cursor.getMoves { startOfLine(); up(1) }
        rec.normalizedOutput() shouldBe "${moves}<2>\n==="

        // print while active
        rec.clearOutput()
        t.println("X")
        moves = t.cursor.getMoves { startOfLine(); up(1); clearScreenAfterCursor() }
        rec.normalizedOutput() shouldBe "${moves}X\n<2>\n==="

        // clear
        rec.clearOutput()
        a.clear()
        moves = t.cursor.getMoves { startOfLine(); up(1); clearScreenAfterCursor() }
        rec.normalizedOutput() shouldBe moves

        // repeat clear
        rec.clearOutput()
        a.clear()
        rec.normalizedOutput() shouldBe ""

        // update after clear
        rec.clearOutput()
        a.update(3)
        rec.normalizedOutput() shouldBe "<3>\n==="

        // stop
        rec.clearOutput()
        a.stop()
        rec.normalizedOutput() shouldBe "\n"

        // print after stop
        rec.clearOutput()
        t.println("X")
        rec.normalizedOutput() shouldBe "X\n"

        // update after stop
        rec.clearOutput()
        a.update(4)
        rec.normalizedOutput() shouldBe "<4>\n==="
    }

    @[Test JsName("two_animations")]
    fun `two animations`() {
        val a = t.textAnimation<Int> { "<a$it>" }
        val b = t.textAnimation<Int> { "<b$it>" }

        a.update(1)
        rec.normalizedOutput().visibleCrLf() shouldBe "<a1>"
        rec.clearOutput()

        b.update(2)
        var moves = t.cursor.getMoves { startOfLine() }
        rec.normalizedOutput().visibleCrLf() shouldBe "${moves}<a1>\n<b2>".visibleCrLf()
        rec.clearOutput()

        b.update(3)
        moves = t.cursor.getMoves {
            startOfLine(); up(1); clearScreenAfterCursor(); startOfLine()
        }
        rec.normalizedOutput().visibleCrLf() shouldBe "${moves}<a1>\n<b3>".visibleCrLf()

        rec.clearOutput()
        a.stop()
        rec.normalizedOutput().visibleCrLf() shouldBe "\r<b3>".visibleCrLf()

        rec.clearOutput()
        b.stop()
        rec.normalizedOutput().visibleCrLf() shouldBe "\n".visibleCrLf()
    }
}
