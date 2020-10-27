package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.internal.CSI
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.junit.Test

private val NO_OUTPUT = "NO_OUTPUT"

private class TestTerminalCursor() : PrintTerminalCursor() {
    var output: String = NO_OUTPUT
    override fun print(text: String) {
        output = text
    }
}

private fun c(b: TerminalCursor.() -> Unit): String {
    val t = TestTerminalCursor()
    t.b()
    return t.output
}

class TerminalCursorTest {
    @Test
    fun `cursor directions 0 count`() = forAll(
            row(c { up(0) }),
            row(c { down(0) }),
            row(c { left(0) }),
            row(c { right(0) }),
    ) { actual ->
        actual shouldBe NO_OUTPUT
    }

    @Test
    fun `cursor commands`() = forAll(
            row(c { up(2) }, "${CSI}2A"),
            row(c { down(3) }, "${CSI}3B"),
            row(c { right(4) }, "${CSI}4C"),
            row(c { left(5) }, "${CSI}5D"),
            row(c { setPosition(6, 7) }, "${CSI}8;7H"),
            row(c { setVisible(true) }, "${CSI}?25h"),
            row(c { setVisible(false) }, "${CSI}?25l"),
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
