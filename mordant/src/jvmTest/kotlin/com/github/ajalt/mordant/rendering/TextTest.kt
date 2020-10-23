package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.TextColors.*
import com.github.ajalt.mordant.TextStyles
import com.github.ajalt.mordant.rendering.OverflowWrap.BREAK_WORD
import com.github.ajalt.mordant.rendering.OverflowWrap.NORMAL
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.rendering.internal.CSI
import com.github.ajalt.mordant.rendering.internal.OSC
import com.github.ajalt.mordant.rendering.internal.ST
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import org.junit.Test


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
    ${(red on white)("red ${blue("blue ${gray.bg("gray.bg")}")} red")}
    unknown
    device status report
    """, width = 79)


    @Test
    fun `ansi parsing with styles`() = checkRender(Text("""
    ${TextStyle(red, white)("red ${TextStyle(blue)("blue ${TextStyle(bgColor = gray)("gray.bg")}")} red")}
    ${CSI}255munknown
    ${CSI}6ndevice status report
    """.trimIndent(), whitespace = PRE), """
    ${(red on white)("red ${blue("blue ${gray.bg("gray.bg")}")} red")}
    unknown
    device status report
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
    ) { it.replace(Regex("id=\\d+"), "id=x") }
}
