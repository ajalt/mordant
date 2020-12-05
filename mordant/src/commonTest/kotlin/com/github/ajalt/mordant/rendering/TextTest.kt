package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.RGB
import com.github.ajalt.mordant.terminal.TextColors
import com.github.ajalt.mordant.terminal.TextColors.*
import com.github.ajalt.mordant.terminal.TextStyles
import com.github.ajalt.mordant.rendering.OverflowWrap.BREAK_WORD
import com.github.ajalt.mordant.rendering.OverflowWrap.NORMAL
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.components.LS
import com.github.ajalt.mordant.components.NEL
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.OSC
import com.github.ajalt.mordant.internal.ST
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.test.Test


class TextTest : RenderingTest() {
    @Test
    fun `override width`() = checkRender(Text("""
    Lorem ipsum dolor
    sit amet
    """, width = 12), """
    Lorem ipsum
    dolor sit
    amet
    """, width = 79)

    @Test
    fun `hard line breaks`() = checkRender(Text("""
    Lorem${NEL}ipsum dolor $LS
    sit $LS  amet
    """), """
    Lorem
    ipsum dolor
    sit
    amet
    """)

    @Test
    fun tabs() = forAll(
            row("\t.",/*   */ "    ."),
            row(".\t.",/*  */ ".   ."),
            row("..\t.",/*  */"..  ."),
            row("...\t.",/* */"... ."),
            row("....\t.",/**/"....    ."),
    ) { text, expected ->
        checkRender(Text(text, whitespace = PRE), expected, tabWidth = 4, trimIndent = false)
    }

    @Test
    fun `ansi parsing`() = checkRender(Text("""
    ${(red on white)("red ${blue("blue ${gray.bg("gray.bg")}")} red")}
    ${CSI}255munknown
    ${CSI}6ndevice status report
    """.trimIndent(), whitespace = PRE), """
    ${CSI}31;47mred ${CSI}34mblue ${CSI}100mgray.bg${CSI}31;47m red${CSI}39;49m
    unknown
    device status report
    """, width = 79)

    @Test
    fun `ansi parsing 256`() = checkRender(Text(
            (TextColors.color(Ansi256(111)) on TextColors.color(Ansi256(222)))("red")),
            "${CSI}38;5;111;48;5;222mred${CSI}39;49m"
    )

    @Test
    fun `ansi parsing truecolor`() = checkRender(Text(
            (TextColors.rgb("#ff0000") on TextColors.rgb("#00ff00"))("red")),
            "${CSI}38;2;255;0;0;48;2;0;255;0mred${CSI}39;49m"
    )

    @Test
    fun `ansi parsing with styles`() = checkRender(Text("""
    ${TextStyle(RGB(255, 0, 0), white)("red ${TextStyle(blue)("blue ${TextStyle(bgColor = gray)("gray.bg")}")} red")}
    ${TextStyle(hyperlink = "foo.com")("foo.${TextStyle(hyperlink = "bar.com")("bar")}.com")}/baz
    """.trimIndent(), whitespace = PRE), """
    ${CSI}38;2;255;0;0;47mred ${CSI}34mblue ${CSI}100mgray.bg${CSI}38;2;255;0;0;47m red${CSI}39;49m
    ${OSC}8;id=x;foo.com${ST}foo.${OSC}8;id=x;bar.com${ST}bar${OSC}8;id=x;foo.com$ST.com${OSC}8;;$ST/baz
    """, width = 79)

    @Test
    fun `hyperlink one line`() = doHyperlinkTest("This is a link",
            "${OSC}8;id=x;http://example.com${ST}This is a link${OSC}8;;$ST"
    )

    @Test
    fun `hyperlink word wrap`() = doHyperlinkTest("This is a link",
            """
            ${OSC}8;id=x;http://example.com${ST}This is${OSC}8;;$ST
            ${OSC}8;id=x;http://example.com${ST}a link${OSC}8;;$ST
            """,
            width = 8
    )

    @Test
    fun `hyperlink break word`() = doHyperlinkTest("This_is_a_link",
            """
            ${OSC}8;id=x;http://example.com${ST}This_is_${OSC}8;;$ST
            ${OSC}8;id=x;http://example.com${ST}a_link${OSC}8;;$ST
            """,
            overflowWrap = BREAK_WORD,
            width = 8
    )

    private fun doHyperlinkTest(text: String, expected: String, overflowWrap: OverflowWrap = NORMAL, width: Int = 79) = checkRender(
            Text(text, TextStyles.hyperlink("http://example.com"), overflowWrap = overflowWrap),
            expected,
            width = width
    )
}
