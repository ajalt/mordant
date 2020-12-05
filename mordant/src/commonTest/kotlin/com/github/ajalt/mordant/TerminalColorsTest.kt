package com.github.ajalt.mordant

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.AnsiLevel
import com.github.ajalt.mordant.terminal.TerminalColors
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TerminalColorsTest {
    private val t = TerminalColors(AnsiLevel.TRUECOLOR)

    @Test
    fun `empty string`() = doTest(
            t.blue(""),
            ""
    )

    @Test
    fun `one level nesting`() = doTest(
            t.blue("A${t.red("B")}C"),
            "<34>A<31>B<34>C<39>"
    )

    @Test
    fun `one level nesting start of string`() = doTest(
            (t.blue on t.green)("${t.red("A")}B"),
            "<31;42>A<34>B<39;49>"
    )

    @Test
    fun `one level nesting end of string`() = doTest(
            t.red("A${t.green("B")}"),
            "<31>A<32>B<39>"
    )

    @Test
    fun `two level nesting`() = doTest(
            t.blue("A${t.red("B${t.green("C")}D")}E"),
            "<34>A<31>B<32>C<31>D<34>E<39>"
    )

    @Test
    fun `two level nesting end of string`() = doTest(
            t.red("A${t.green("B${t.yellow("C")}")}"),
            "<31>A<32>B<33>C<39>"
    )

    @Test
    fun `two level nesting styles`() = doTest(
            t.red("A${t.bold("B${t.green("C")}D")}E"),
            "<31>A<1>B<32>C<31>D<22>E<39>"
    )

    @Test
    fun `three level nesting styles and bg`() = doTest(
            t.run { (red on red)("A${(green on green)("B${(yellow.bg + underline)("C")}D")}E") },
            "<31;41>A<32;42>B<43;4>C<42;24>D<31;41>E<39;49>"
    )

    private fun doTest(actual: String, expected: String) {
        try {
            actual.replace(CSI, "<").replace("m", ">") shouldBe expected
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
