package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.TextAlign.*
import org.junit.Test


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
        t = Terminal(width = width)
        checkRender(Text("""
        |one_word
        |
        |two words
        |3 whole words
        |four words 4 4
        |5 5 5 5 5
        """.trimMargin(), whitespace = Whitespace.PRE_WRAP, align = align), expected.trimMargin(), trimIndent = false)
    }
}
