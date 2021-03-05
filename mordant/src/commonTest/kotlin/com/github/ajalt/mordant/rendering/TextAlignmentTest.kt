package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.white
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Text
import kotlin.js.JsName
import kotlin.test.Test


class TextAlignmentTest : RenderingTest() {
    @Test
    @JsName("align_none")
    fun `align none`() = doTest(NONE, 79, """
    |one_word
    |
    |two words
    |3 whole words
    |four words 4 4
    |5 5 5 5 5
    """)

    @Test
    @JsName("align_left")
    fun `align left`() = doTest(LEFT, 15, """
    |one_word       ⏎
    |               ⏎
    |two words      ⏎
    |3 whole words  ⏎
    |four words 4 4 ⏎
    |5 5 5 5 5      ⏎
    """)

    @Test
    @JsName("align_right")
    fun `align right`() = doTest(RIGHT, 15, """
    |       one_word⏎
    |               ⏎
    |      two words⏎
    |  3 whole words⏎
    | four words 4 4⏎
    |      5 5 5 5 5⏎
    """)

    @Test
    @JsName("align_center")
    fun `align center`() = doTest(CENTER, 15, """
    |   one_word    ⏎
    |               ⏎
    |   two words   ⏎
    | 3 whole words ⏎
    |four words 4 4 ⏎
    |   5 5 5 5 5   ⏎
    """)

    @Test
    @JsName("align_justify")
    fun `align justify`() = doTest(JUSTIFY, 15, """
    |   one_word    ⏎
    |               ⏎
    |two       words⏎
    |3  whole  words⏎
    |four words 4  4⏎
    |5  5  5   5   5⏎
    """)

    @Test
    @JsName("align_justify_wide")
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
            (blue on white)(text),
            whitespace = Whitespace.PRE_WRAP,
            align = align,
        ), ex, trimIndent = false, width = width)
    }
}
