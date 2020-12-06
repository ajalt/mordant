package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.junit.Test

private fun c(b: CursorMovements.() -> Unit): String {
    val vt = VirtualTerminalInterface()
    val t = Terminal(terminalInterface = vt)
    t.cursor.move(b)
    return vt.buffer()
}

class TerminalCursorTest {
    @Test
    fun `disabled commands`() {
        val vt = VirtualTerminalInterface(AnsiLevel.NONE)
        val t = Terminal(terminalInterface = vt)
        val c = t.cursor
        forAll(
            row(c.move { up(1) }),
            row(c.move { down(1) }),
            row(c.move { right(1) }),
            row(c.move { left(1) }),
            row(c.move { startOfLine() }),
            row(c.move { setPosition(1, 1) }),
            row(c.show()),
            row(c.hide(showOnExit = false)),
            row(c.move { clearScreen() }),
            row(c.move { clearScreenAfterCursor() }),
        ) {
            vt.buffer() shouldBe ""
            vt.clearBuffer()
        }
    }

    @Test
    fun `cursor directions 0 count`() = forAll(
            row(c { up(0) }),
            row(c { down(0) }),
            row(c { left(0) }),
            row(c { right(0) }),
    ) { actual ->
        actual shouldBe ""
    }

    @Test
    fun `cursor show and hide`() {
        val vt = VirtualTerminalInterface()
        val t = Terminal(terminalInterface = vt)
        t.cursor.hide(showOnExit = false)
        vt.buffer() shouldBe "$CSI?25l"
        vt.clearBuffer()
        t.cursor.show()
        vt.buffer() shouldBe "$CSI?25h"
    }

    @Test
    fun `disabled cursor show and hide`() {
        val vt = VirtualTerminalInterface(AnsiLevel.NONE)
        val t = Terminal(terminalInterface = vt)
        t.cursor.hide(showOnExit = false)
        vt.buffer() shouldBe ""
        t.cursor.show()
        vt.buffer() shouldBe ""
    }

    @Test
    fun `cursor commands`() = forAll(
            row(c { up(2) }, "${CSI}2A"),
            row(c { down(3) }, "${CSI}3B"),
            row(c { right(4) }, "${CSI}4C"),
            row(c { left(5) }, "${CSI}5D"),
            row(c { startOfLine() }, "\r"),
            row(c { setPosition(6, 7) }, "${CSI}8;7H"),
            row(c { clearScreen() }, "${CSI}2J"),
            row(c { clearScreenBeforeCursor() }, "${CSI}1J"),
            row(c { clearScreenAfterCursor() }, "${CSI}0J"),
            row(c { clearLine() }, "${CSI}2K"),
            row(c { clearLineBeforeCursor() }, "${CSI}1K"),
            row(c { clearLineAfterCursor() }, "${CSI}0K"),
            row(c { savePosition() }, "${CSI}s"),
            row(c { restorePosition() }, "${CSI}u"),
    ) { actual, expected ->
        actual shouldBe expected
    }

    @Test
    fun `cursor commands negative count`() {
        forAll(
                row(c { up(-1) }, c { down(1) }),
                row(c { down(-2) }, c { up(2) }),
                row(c { left(-3) }, c { right(3) }),
                row(c { right(-4) }, c { left(4) }),
        ) { actual, expected ->
            actual shouldBe expected
        }
    }
}
