package com.github.ajalt.mordant

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Test

class TermColorsTest {
    @Test
    fun `empty string`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.blue("")
        s shouldBe ""
    }

    @Test
    fun `one level nesting`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.blue("A${t.red("B")}C")
        s shouldBe "${CSI}34mA${CSI}31mB${CSI}34mC${CSI}39m"
    }

    @Test
    fun `one level nesting end of string`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.green("B")}")
        s shouldBe "${CSI}31mA${CSI}32mB${CSI}39m"
    }

    @Test
    fun `two level nesting`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.blue("A${t.red("B${t.green("C")}D")}E")
        s shouldBe "${CSI}34mA${CSI}31mB${CSI}32mC${CSI}31mD${CSI}34mE${CSI}39m"
    }

    @Test
    fun `two level nesting end of string`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.green("B${t.yellow("C")}")}")
        s shouldBe "${CSI}31mA${CSI}32mB${CSI}33mC${CSI}39m"
    }

    @Test
    fun `two level nesting styles`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.bold("B${t.green("C")}D")}E")
        s shouldBe "${CSI}31mA${CSI}1mB${CSI}32mC${CSI}31mD${CSI}22mE${CSI}39m"
    }

    @Test
    fun `three level nesting styles and bg`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.run { (red on red)("A${(green on green)("B${(yellow.bg + underline)("C")}D")}E") }
        s shouldBe "${CSI}31;41mA${CSI}32;42mB${CSI}43;4mC${CSI}42;24mD${CSI}31;41mE${CSI}39;49m"
    }

    @Test
    fun `cursor commands disabled`() {
        val t = TermColors(TermColors.Level.NONE)
        forall(
                row(t.cursorUp(1)),
                row(t.cursorDown(1)),
                row(t.cursorLeft(1)),
                row(t.cursorRight(1)),
                row(t.showCursor),
                row(t.hideCursor)
        ) { actual ->
            actual shouldBe ""
        }
    }

    @Test
    fun `cursor directions 0 count`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        forall(
                row(t.cursorUp(0)),
                row(t.cursorDown(0)),
                row(t.cursorRight(0)),
                row(t.cursorLeft(0))
        ) { actual ->
            actual shouldBe ""
        }
    }

    @Test
    fun `cursor commands enabled`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        forall(
                row(t.cursorUp(2), "${CSI}2A"),
                row(t.cursorDown(3), "${CSI}3B"),
                row(t.cursorRight(4), "${CSI}4C"),
                row(t.cursorLeft(5), "${CSI}5D"),
                row(t.showCursor, "$CSI?25h"),
                row(t.hideCursor, "$CSI?25l")
        ) { actual, expected ->
            actual shouldBe expected
        }
    }

    @Test
    fun `cursor commands negative count`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        forall(
                row(t.cursorUp(-2), "${CSI}2B"),
                row(t.cursorDown(-3), "${CSI}3A"),
                row(t.cursorRight(-4), "${CSI}4D"),
                row(t.cursorLeft(-5), "${CSI}5C")
        ) { actual, expected ->
            actual shouldBe expected
        }
    }
}
