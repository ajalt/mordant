package com.github.ajalt.termcolors

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val ESC = (0x1b).toChar()

class TermColorsTest {
    @Test
    fun `empty string`() {
        val t = TermColors()
        val s = t.blue("")
        assertThat(s).isEqualTo("")
    }

    @Test
    fun `one level nesting`() {
        val t = TermColors()
        val s = t.blue("A${t.red("B")}C")
        assertThat(s).isEqualTo("$ESC[34mA$ESC[31mB$ESC[34mC$ESC[39m")
    }

    @Test
    fun `one level nesting end of string`() {
        val t = TermColors()
        val s = t.blue("A${t.red("B")}")
        assertThat(s).isEqualTo("$ESC[34mA$ESC[31mB$ESC[39m")
    }

    @Test
    fun `two level nesting`() {
        val t = TermColors()
        val s = t.blue("A${t.red("B${t.green("C")}D")}E")
        assertThat(s).isEqualTo("$ESC[34mA$ESC[31mB$ESC[32mC$ESC[31mD$ESC[34mE$ESC[39m")
    }

    @Test
    fun `two level nesting end of string`() {
        val t = TermColors()
        val s = t.blue("A${t.red("B${t.green("C")}")}")
        assertThat(s).isEqualTo("$ESC[34mA$ESC[31mB$ESC[32mC$ESC[39m")
    }
}
