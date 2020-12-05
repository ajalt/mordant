package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test

private fun c(b: TerminalCursor.() -> Unit): String {
    val t = VirtualTerminal()
    t.cursor.b()
    return t.buffer()
}

class TerminalCursorTest {
    @Test
    fun `disabled commands`() {
        val t = VirtualTerminal(AnsiLevel.NONE)
        val c = t.cursor
        forAll(
                row(c.up(1)),
                row(c.down(1)),
                row(c.right(1)),
                row(c.left(1)),
                row(c.startOfLine()),
                row(c.setPosition(1, 1)),
                row(c.show()),
                row(c.hide(showOnExit = false)),
                row(c.clearScreen()),
                row(c.clearScreenAfterCursor()),
        ) {
            t.buffer() shouldBe ""
            t.clearBuffer()
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
    fun `cursor commands`() = forAll(
            row(c { up(2) }, "${CSI}2A"),
            row(c { down(3) }, "${CSI}3B"),
            row(c { right(4) }, "${CSI}4C"),
            row(c { left(5) }, "${CSI}5D"),
            row(c { setPosition(6, 7) }, "${CSI}8;7H"),
            row(c { show() }, "$CSI?25h"),
            row(c { hide(showOnExit = false) }, "$CSI?25l"),
            row(c { clearScreen() }, "${CSI}2J"),
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
