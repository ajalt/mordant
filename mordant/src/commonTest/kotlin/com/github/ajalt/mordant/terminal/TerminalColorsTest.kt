package com.github.ajalt.mordant.terminal

import com.github.ajalt.colormath.model.*
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.*
import io.kotest.assertions.assertSoftly
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Ignore
import kotlin.test.Test

class TerminalColorsTest {
    private val c = Terminal(AnsiLevel.TRUECOLOR).colors

    @Test
    @JsName("empty_string")
    fun `empty string`() = doTest(
        c.blue(""),
        ""
    )

    @Test
    @JsName("one_level_nesting")
    fun `one level nesting`() = doTest(
        c.blue("A${c.red("B")}C"),
        "<34>A<31>B<34>C<39>"
    )

    @Test
    @JsName("one_level_nesting_start_of_string")
    fun `one level nesting start of string`() = doTest(
        (c.blue on c.green)("${c.red("A")}B"),
        "<34;42>AB<39;49>"
    )

    @Test
    @JsName("one_level_nesting_end_of_string")
    fun `one level nesting end of string`() = doTest(
        c.red("A${c.green("B")}"),
        "<31>A<32>B<39>"
    )

    @Test
    @JsName("two_level_nesting")
    fun `two level nesting`() = doTest(
        c.blue("A${c.red("B${c.green("C")}D")}E"),
        "<34>A<31>B<32>C<31>D<34>E<39>"
    )

    @Test
    @JsName("two_level_nesting_end_of_string")
    fun `two level nesting end of string`() = doTest(
        c.red("A${c.green("B${c.yellow("C")}")}"),
        "<31>A<32>B<33>C<39>"
    )

    @Test
    @JsName("two_level_nesting_styles")
    fun `two level nesting styles`() = doTest(
        c.red("A${c.bold("B${c.green("C")}D")}E"),
        "<31>A<1>B<32>C<31>D<22>E<39>"
    )

    @Test
    @JsName("three_level_nesting_styles_and_bg")
    fun `three level nesting styles and bg`() = doTest(
        c.run { (red on red)("A${(green on green)("B${(yellow.bg + underline)("C")}D")}E") },
        "<31;41>A<32;42>B<43;4>C<42;24>D<31;41>E<39;49>"
    )

    @Test
    @JsName("all_ansi16_colors")
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

    // Disabled due to codegen bug on JS/IR
    @Ignore
    @Test
    @JsName("all_24bit_colors")
    fun `all 24bit colors`() = forAll(
        row(c.rgb("#ff00ff"), RGB("#ff00ff")),
        row(c.rgb(.11, .22, .33), RGB(.11, .22, .33)),
        row(c.hsl(.11, .22, .33), HSL(.11, .22, .33)),
        row(c.hsv(11, .22, .33), HSV(11, .22, .33)),
        row(c.cmyk(11, 22, 33, 44), CMYK(11, 22, 33, 44)),
        row(c.gray(0.5), RGB(.5, .5, .5)),
        row(c.xyz(.11, .22, .33), XYZ(.11, .22, .33)),
        row(c.lab(11, 22, 33), LAB(11, 22, 33)),
    ) { color, expected ->
        color.color shouldBe expected
    }

    @Test
    @JsName("all_styles")
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
    @JsName("all_colors_and_styles_downsampled")
    fun `all colors and styles downsampled`() {
        val colorNone = Terminal(ansiLevel = AnsiLevel.NONE).colors
        val colorRgb = Terminal(ansiLevel = AnsiLevel.TRUECOLOR).colors
        forAll(
            row({ it.black }, TextColors.black),
            row({ it.red }, TextColors.red),
            row({ it.green }, TextColors.green),
            row({ it.yellow }, TextColors.yellow),
            row({ it.blue }, TextColors.blue),
            row({ it.magenta }, TextColors.magenta),
            row({ it.cyan }, TextColors.cyan),
            row({ it.white }, TextColors.white),
            row({ it.gray }, TextColors.gray),
            row({ it.brightRed }, TextColors.brightRed),
            row({ it.brightGreen }, TextColors.brightGreen),
            row({ it.brightYellow }, TextColors.brightYellow),
            row({ it.brightBlue }, TextColors.brightBlue),
            row({ it.brightMagenta }, TextColors.brightMagenta),
            row({ it.brightCyan }, TextColors.brightCyan),
            row({ it.brightWhite }, TextColors.brightWhite),
            row({ it.bold }, TextStyles.bold.style),
            row({ it.dim }, TextStyles.dim.style),
            row({ it.italic }, TextStyles.italic.style),
            row({ it.underline }, TextStyles.underline.style),
            row({ it.inverse }, TextStyles.inverse.style),
            row({ it.strikethrough }, TextStyles.strikethrough.style),
            row({ it.plain }, DEFAULT_STYLE),
            row({ it.success }, Theme.Default.success),
            row({ it.danger }, Theme.Default.danger),
            row({ it.warning }, Theme.Default.warning),
            row({ it.info }, Theme.Default.info),
            row({ it.muted }, Theme.Default.muted),
        ) { block: (TerminalColors) -> TextStyle, style: TextStyle ->
            block(colorNone) shouldBe DEFAULT_STYLE
            block(colorRgb) shouldBe style
        }
    }

    private fun doTest(actual: String, expected: String) {
        try {
            actual.replace(CSI, "<").replace("m", ">") shouldBe expected
        } catch (e: Throwable) {
            println("Expected:")
            println(expected.replace("<", CSI).replace(">", "m"))
            println("Actual:")
            println(actual)
            throw e
        }
    }
}
