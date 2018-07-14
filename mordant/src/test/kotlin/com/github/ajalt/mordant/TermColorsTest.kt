package com.github.ajalt.mordant

import io.kotlintest.shouldBe
import org.junit.Test

private const val ESC = '\u001b'

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
        s shouldBe "$ESC[34mA$ESC[31mB$ESC[34mC$ESC[39m"
    }

    @Test
    fun `one level nesting end of string`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.green("B")}")
        s shouldBe "$ESC[31mA$ESC[32mB$ESC[39m"
    }

    @Test
    fun `two level nesting`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.blue("A${t.red("B${t.green("C")}D")}E")
        s shouldBe "$ESC[34mA$ESC[31mB$ESC[32mC$ESC[31mD$ESC[34mE$ESC[39m"
    }

    @Test
    fun `two level nesting end of string`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.green("B${t.yellow("C")}")}")
        s shouldBe "$ESC[31mA$ESC[32mB$ESC[33mC$ESC[39m"
    }

    @Test
    fun `two level nesting styles`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.red("A${t.bold("B${t.green("C")}D")}E")
        s shouldBe "$ESC[31mA$ESC[1mB$ESC[32mC$ESC[31mD$ESC[22mE$ESC[39m"
    }

    @Test
    fun `three level nesting styles and bg`() {
        val t = TermColors(TermColors.Level.TRUECOLOR)
        val s = t.run { (red on red)("A${(green on green)("B${(yellow.bg + underline)("C")}D")}E") }
        s shouldBe "$ESC[31;41mA$ESC[32;42mB$ESC[43;4mC$ESC[42;24mD$ESC[31;41mE$ESC[39;49m"
    }
}
