package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiColor
import com.github.ajalt.mordant.AnsiColor.*
import com.github.ajalt.mordant.CSI
import com.github.ajalt.mordant.rendering.Whitespace.PRE
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
    ${(red on white)("red ")}${(blue on white)("blue ")}${((blue on gray)("gray.bg"))}${(red on white)(" red")}
    unknown
    device status report
    """, width = 79)
}
