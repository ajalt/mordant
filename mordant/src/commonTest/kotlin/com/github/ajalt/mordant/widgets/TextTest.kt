package com.github.ajalt.mordant.widgets

import com.github.ajalt.colormath.model.Ansi256
import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.OSC
import com.github.ajalt.mordant.internal.ST
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.OverflowWrap.BREAK_WORD
import com.github.ajalt.mordant.rendering.OverflowWrap.NORMAL
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.test.normalizeHyperlinks
import com.github.ajalt.mordant.test.visibleCrLf
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.js.JsName
import kotlin.test.Test


class TextTest : RenderingTest() {
    @[Test JsName("trailing_line_break")]
    fun `trailing line break`() {
        checkRender(Text("x\n"), "x\n", trimMargin = false)
    }

    @[Test JsName("override_width")]
    fun `override width`() = checkRender(
        Text(
            """
    Lorem ipsum dolor
    sit amet
    """, whitespace = Whitespace.NORMAL, width = 12
        ), """
    ░Lorem ipsum
    ░dolor sit
    ░amet
    """, width = 79
    )

    @[Test JsName("hard_line_breaks")]
    fun `hard line breaks`() = checkRender(
        Text(
            """
    Lorem${NEL}ipsum dolor $LS
    sit $LS  amet
    """, whitespace = Whitespace.NORMAL
        ), """
    ░Lorem
    ░ipsum dolor
    ░sit
    ░amet
    """
    )

    @Test
    fun tabs() = forAll(
        row("\t.",/*   */ "    ."),
        row(".\t.",/*  */ ".   ."),
        row("..\t.",/*  */"..  ."),
        row("...\t.",/* */"... ."),
        row("....\t.",/**/"....    ."),
    ) { text, expected ->
        checkRender(Text(text, whitespace = PRE), expected, tabWidth = 4)
    }

    @[Test JsName("ansi_parsing")]
    fun `ansi parsing`() = checkRender(
        Text(
            """
    ${(red on white)("red ${blue("blue ${gray.bg("gray.bg")}")} red")}
    ${CSI}255munknown
    ${CSI}6ndevice status report
    """.trimIndent(), whitespace = PRE
        ), """
    ░${CSI}31;47mred ${CSI}34mblue ${CSI}100mgray.bg${CSI}31;47m red${CSI}39;49m
    ░unknown
    ░device status report
    """, width = 79
    )

    @[Test JsName("ansi_parsing_256")]
    fun `ansi parsing 256`() = checkRender(
        Text(
            (TextColors.color(Ansi256(111)) on TextColors.color(Ansi256(222)))("red")
        ),
        "${CSI}38;5;111;48;5;222mred${CSI}39;49m"
    )

    @[Test JsName("ansi_parsing_truecolor")]
    fun `ansi parsing truecolor`() = checkRender(
        Text(
            (TextColors.rgb("#ff0000") on TextColors.rgb("#00ff00"))("red")
        ),
        "${CSI}38;2;255;0;0;48;2;0;255;0mred${CSI}39;49m"
    )

    @[Test JsName("ansi_parsing_with_styles")]
    fun `ansi parsing with styles`() = checkRender(
        Text(
            """
    ${
                TextStyle(
                    RGB(1, 0, 0),
                    white
                )("red ${TextStyle(blue)("blue ${TextStyle(bgColor = gray)("gray.bg")}")} red")
            }
    ${TextStyle(hyperlink = "foo.com")("foo.${TextStyle(hyperlink = "bar.com")("bar")}.com")}/baz
    """.trimIndent(), whitespace = PRE
        ), """
    ░${CSI}38;2;255;0;0;47mred ${CSI}34mblue ${CSI}100mgray.bg${CSI}38;2;255;0;0;47m red${CSI}39;49m
    ░${OSC}8;id=1;foo.com${ST}foo.${OSC}8;id=2;bar.com${ST}bar${OSC}8;id=1;foo.com$ST.com${OSC}8;;$ST/baz
    """, width = 79
    ) { it.normalizeHyperlinks() }

    @[Test JsName("replacing_whole_string_color")]
    fun `replacing whole string color`() = checkRender(
        Text((green on gray)((red on blue)("text"))),
        (green on gray)("text")
    )


    @[Test JsName("ansi_bold_and_dim")]
    fun `ansi bold and dim`() = checkRender(
        Text(" ${dim("dim${bold("bold")}dim")} ".visibleCrLf()),
        " ␛2mdim␛1mbold␛22;2mdim␛22m "
        // [1;2mbold[0m[2m not [0m[2;31mbold[0m
    )


    @Test
    fun resets() = forAll(
        row(TextStyles.resetForeground, blue.bg),
        row(TextStyles.resetBackground, red),
        row(TextStyles.reset, DEFAULT_STYLE),
    ) { style, expected ->
        checkRender(Text(style((red on blue)("text"))), expected("text"))
    }

    @[Test JsName("hyperlink_one_line")]
    fun `hyperlink one line`() = doHyperlinkTest(
        "This is a link",
        "${OSC}8;id=1;https://example.com${ST}This is a link${OSC}8;;$ST"
    )

    @[Test JsName("hyperlink_word_wrap")]
    fun `hyperlink word wrap`() = doHyperlinkTest(
        "This is a link",
        """
        ${OSC}8;id=1;https://example.com${ST}This is${OSC}8;;$ST
        ${OSC}8;id=1;https://example.com${ST}a link${OSC}8;;$ST
        """,
        width = 8
    )

    @[Test JsName("hyperlink_break_word")]
    fun `hyperlink break word`() = doHyperlinkTest(
        "This_is_a_link",
        """
        ${OSC}8;id=1;https://example.com${ST}This_is_${OSC}8;;$ST
        ${OSC}8;id=1;https://example.com${ST}a_link${OSC}8;;$ST
        """,
        overflowWrap = BREAK_WORD,
        width = 8
    )

    private fun doHyperlinkTest(
        text: String,
        expected: String,
        overflowWrap: OverflowWrap = NORMAL,
        width: Int = 79,
    ) = checkRender(
        Text(
            TextStyles.hyperlink("https://example.com")(text),
            whitespace = Whitespace.NORMAL,
            overflowWrap = overflowWrap
        ),
        expected.trimIndent(),
        width = width,
        trimMargin = false,
    ) { it.normalizeHyperlinks() }
}
