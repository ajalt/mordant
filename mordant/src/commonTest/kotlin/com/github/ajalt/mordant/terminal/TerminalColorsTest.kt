package com.github.ajalt.mordant.terminal

import com.github.ajalt.colormath.*
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import io.kotest.assertions.assertSoftly
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TerminalColorsTest {
    private val c = Terminal(AnsiLevel.TRUECOLOR).colors

    @Test
    fun `empty string`() = doTest(
        c.blue(""),
        ""
    )

    @Test
    fun `one level nesting`() = doTest(
        c.blue("A${c.red("B")}C"),
        "<34>A<31>B<34>C<39>"
    )

    @Test
    fun `one level nesting start of string`() = doTest(
        (c.blue on c.green)("${c.red("A")}B"),
        "<31;42>A<34>B<39;49>"
    )

    @Test
    fun `one level nesting end of string`() = doTest(
        c.red("A${c.green("B")}"),
        "<31>A<32>B<39>"
    )

    @Test
    fun `two level nesting`() = doTest(
        c.blue("A${c.red("B${c.green("C")}D")}E"),
        "<34>A<31>B<32>C<31>D<34>E<39>"
    )

    @Test
    fun `two level nesting end of string`() = doTest(
        c.red("A${c.green("B${c.yellow("C")}")}"),
        "<31>A<32>B<33>C<39>"
    )

    @Test
    fun `two level nesting styles`() = doTest(
        c.red("A${c.bold("B${c.green("C")}D")}E"),
        "<31>A<1>B<32>C<31>D<22>E<39>"
    )

    @Test
    fun `three level nesting styles and bg`() = doTest(
        c.run { (red on red)("A${(green on green)("B${(yellow.bg + underline)("C")}D")}E") },
        "<31;41>A<32;42>B<43;4>C<42;24>D<31;41>E<39;49>"
    )

    @Test
    fun `all ansi16 colors`() = forAll(
        row(c.black, 30),
        row(c.red, 31),
        row(c.green, 32),
        row(c.yellow, 33),
        row(c.blue, 34),
        row(c.magenta, 35),
        row(c.cyan, 36),
        row(c.white, 37),
        row(c.gray, 90),
        row(c.brightRed, 91),
        row(c.brightGreen, 92),
        row(c.brightYellow, 93),
        row(c.brightBlue, 94),
        row(c.brightMagenta, 95),
        row(c.brightCyan, 96),
        row(c.brightWhite, 97),
    ) { color, code ->
        color.color!!.toAnsi16().code shouldBe code
    }

    @Test
    fun `all 24bit colors`() = forAll(
        row(c.rgb("#ff00ff"), RGB("#ff00ff")),
        row(c.rgb(11, 22, 33), RGB(11, 22, 33)),
        row(c.hsl(11, 22, 33), HSL(11, 22, 33)),
        row(c.hsv(11, 22, 33), HSV(11, 22, 33)),
        row(c.cmyk(11, 22, 33, 44), CMYK(11, 22, 33, 44)),
        row(c.gray(0.5), RGB(128, 128, 128)),
        row(c.xyz(11.0, 22.0, 33.0), XYZ(11.0, 22.0, 33.0)),
        row(c.lab(11.0, 22.0, 33.0), LAB(11.0, 22.0, 33.0)),
    ) { color, expected ->
        color.color shouldBe expected
    }

    @Test
    fun `all styles`() {
        assertSoftly {
            c.bold.bold shouldBe true
            c.dim.dim shouldBe true
            c.italic.italic shouldBe true
            c.underline.underline shouldBe true
            c.inverse.inverse shouldBe true
            c.strikethrough.strikethrough shouldBe true
            c.plain shouldBe DEFAULT_STYLE
        }
    }

    @Test
    fun `all colors and styles downsampled`() {
        val c = Terminal(ansiLevel = AnsiLevel.NONE).colors
        forAll(
            row(c.black),
            row(c.red),
            row(c.green),
            row(c.yellow),
            row(c.blue),
            row(c.magenta),
            row(c.cyan),
            row(c.white),
            row(c.gray),
            row(c.brightRed),
            row(c.brightGreen),
            row(c.brightYellow),
            row(c.brightBlue),
            row(c.brightMagenta),
            row(c.brightCyan),
            row(c.brightWhite),
            row(c.bold),
            row(c.dim),
            row(c.italic),
            row(c.underline),
            row(c.inverse),
            row(c.strikethrough),
            row(c.plain),
        ) { style ->
            style shouldBe DEFAULT_STYLE
        }
    }

    private fun doTest(actual: String, expected: String) {
        try {
            actual.replace(CSI, "<").replace("m", ">") shouldBe expected
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
