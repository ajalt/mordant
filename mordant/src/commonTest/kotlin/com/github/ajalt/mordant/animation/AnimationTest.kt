package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class AnimationTest {
    private val rec = TerminalRecorder(width = 24)
    private val t = Terminal(terminalInterface = rec)

    @Test
    @JsName("print_during_animation")
    fun `print during animation`() {
        val a = t.textAnimation<Int> { "<$it>\n===" }
        a.update(1)
        rec.output() shouldBe "<1>\n===\n"

        // update
        rec.clearOutput()
        a.update(2)
        var moves = t.cursor.getMoves { startOfLine(); up(2) }
        rec.output() shouldBe "${moves}<2>\n===\n"

        // print while active
        rec.clearOutput()
        t.println("X")
        moves = t.cursor.getMoves { startOfLine(); up(2); clearScreenAfterCursor() }
        rec.output() shouldBe "${moves}X\n<2>\n===\n"

        // clear
        rec.clearOutput()
        a.clear()
        moves = t.cursor.getMoves { startOfLine(); up(2); clearScreenAfterCursor() }
        rec.output() shouldBe moves

        // repeat clear
        rec.clearOutput()
        a.clear()
        rec.output() shouldBe ""

        // update after clear
        rec.clearOutput()
        a.update(3)
        rec.output() shouldBe "<3>\n===\n"

        // stop
        rec.clearOutput()
        a.stop()
        rec.output() shouldBe ""

        // print after stop
        rec.clearOutput()
        t.println("X")
        rec.output() shouldBe "X\n"

        // update after stop
        rec.clearOutput()
        a.update(4)
        rec.output() shouldBe "<4>\n===\n"
    }
}
