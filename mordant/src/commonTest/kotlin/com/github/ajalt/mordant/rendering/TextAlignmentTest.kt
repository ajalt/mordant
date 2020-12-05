package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.TextColors.blue
import com.github.ajalt.mordant.terminal.TextColors.white
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.components.Text
import kotlin.test.Test


class TextAlignmentTest : RenderingTest() {
    @Test
    fun `align none`() = doTest(NONE, 79, """
    |one_word
    |
    |two words
    |3 whole words
    |four words 4 4
    |5 5 5 5 5
    """)

    @Test
    fun `align left`() = doTest(LEFT, 15, """
    |one_word       ⏎
    |               ⏎
    |two words      ⏎
    |3 whole words  ⏎
    |four words 4 4 ⏎
    |5 5 5 5 5      ⏎
    """)

    @Test
    fun `align right`() = doTest(RIGHT, 15, """
    |       one_word⏎
    |               ⏎
    |      two words⏎
    |  3 whole words⏎
    | four words 4 4⏎
    |      5 5 5 5 5⏎
    """)

    @Test
    fun `align center`() = doTest(CENTER, 15, """
    |   one_word    ⏎
    |               ⏎
    |   two words   ⏎
    | 3 whole words ⏎
    |four words 4 4 ⏎
    |   5 5 5 5 5   ⏎
    """)

    @Test
    fun `align justify`() = doTest(JUSTIFY, 15, """
    |   one_word    ⏎
    |               ⏎
    |two       words⏎
    |3  whole  words⏎
    |four words 4  4⏎
    |5  5  5   5   5⏎
    """)

    @Test
    fun `align justify wide`() = doTest(JUSTIFY, 21, """
    |      one_word       ⏎
    |                     ⏎
    |two             words⏎
    |3     whole     words⏎
    |four   words   4    4⏎
    |5    5    5    5    5⏎
    """)

    private fun doTest(align: TextAlign, width: Int, expected: String) {
        val ex = expected.trimMargin().lines().joinToString("\n") { (blue on white)(it) }
        val text = """
        |one_word
        |
        |two words
        |3 whole words
        |four words 4 4
        |5 5 5 5 5
        """.trimMargin()
        checkRender(Text(
                text,
                whitespace = Whitespace.PRE_WRAP,
                align = align,
                style = TextStyle(blue, white)
        ), ex, trimIndent = false, width = width)
    }
}
