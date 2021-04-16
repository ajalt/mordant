package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Text
import kotlin.test.Test


class TextOverflowWrapTest : RenderingTest() {
    @Test
    fun normal() = doTest(OverflowWrap.NORMAL, """
        |The weather today is
        |21°C in
        |Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch
    """)

    @Test
    fun break_word() = doTest(OverflowWrap.BREAK_WORD, """
        |The weather today is
        |21°C in
        |Llanfairpwllgwyngyllgog
        |erychwyrndrobwllllantys
        |iliogogogoch
    """)

    @Test
    fun truncate() = doTest(OverflowWrap.TRUNCATE, """
        |The weather today is
        |21°C in
        |Llanfairpwllgwyngyllgog
    """)

    @Test
    fun ellipses() = doTest(OverflowWrap.ELLIPSES, """
        |The weather today is
        |21°C in
        |Llanfairpwllgwyngyllgo…
    """)

    private fun doTest(wrap: OverflowWrap, expected: String) {
        val text = """
        |The weather today is 21°C in
        |Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch
        """.trimMargin()
        checkRender(Text(
            text,
            whitespace = Whitespace.NORMAL,
            align = TextAlign.NONE,
            overflowWrap = wrap
        ), expected.trimMargin(), trimIndent = false, width = 23)
    }
}
