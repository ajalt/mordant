package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.TextAlign.NONE
import com.github.ajalt.mordant.rendering.Whitespace.*
import com.github.ajalt.mordant.components.Text
import kotlin.test.Test


class TextWhitespaceTest : RenderingTest() {
    @Test
    fun normal() = doTest(NORMAL, 18, """
        |Lorem ipsum dolor⏎
        |sit amet,⏎
        |consectetur⏎
        |adipiscing elit,
        |sed⏎
    """)

    @Test
    fun nowrap() = doTest(NOWRAP, 18, """
        |Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed⏎
    """)

    @Test
    fun pre() = doTest(PRE, 19, """
        |Lorem ipsum dolor ⏎
        |⏎
        |sit amet, consectetur ⏎
        |    adipiscing        ⏎
        |elit,   sed⏎
    """)

    @Test
    fun pre_wrap() = doTest(PRE_WRAP, 19, """
        |Lorem ipsum dolor⏎
        |⏎
        |sit amet,⏎
        |consectetur⏎
        |    adipiscing⏎
        |elit,   sed⏎
    """)

    @Test
    fun pre_line() = doTest(PRE_LINE, 19, """
        |Lorem ipsum dolor⏎
        |⏎
        |sit amet,⏎
        |consectetur⏎
        |adipiscing⏎
        |elit, sed⏎
    """)

    @Test
    fun `consecutive whitespace spans`() {
        val line1 = listOf("a", "   ", " ").map { Span.word(it) }
        val line2 = listOf(" ", "b").map { Span.word(it) }
        checkRender(
                Text(Lines(listOf(line1, line2)), whitespace = PRE_WRAP),
                """
                |a
                | b
                """.trimMargin(),
                width = 2
        )
    }

    private fun doTest(ws: Whitespace, width: Int, expected: String) {
        val text = """
        |Lorem ipsum dolor ⏎
        |⏎
        |sit amet, consectetur ⏎
        |␉adipiscing        ⏎
        |elit,   sed⏎
        """.trimMargin().replace("⏎", "").replace("␉", "\t")
        checkRender(Text(
                text,
                whitespace = ws,
                align = NONE,
                tabWidth = 4
        ), expected.trimMargin(), trimIndent = false, width = width)
    }
}
