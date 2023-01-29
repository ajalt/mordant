package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Text
import kotlin.test.Test


class TextOverflowWrapTest : RenderingTest() {
    @Test
    fun normal() = doTest(
        """
        ░The weather today is
        ░21°C in
        ░Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch
        """, OverflowWrap.NORMAL
    )

    @Test
    fun break_word() = doTest(
        """
        ░The weather today is
        ░21°C in
        ░Llanfairpwllgwyngyllgog
        ░erychwyrndrobwllllantys
        ░iliogogogoch
        """, OverflowWrap.BREAK_WORD
    )

    @Test
    fun truncate() = doTest(
        """
        ░The weather today is
        ░21°C in
        ░Llanfairpwllgwyngyllgog
        """, OverflowWrap.TRUNCATE
    )

    @Test
    fun ellipses() = doTest(
        """
        ░The weather today is
        ░21°C in
        ░Llanfairpwllgwyngyllgo…
        """, OverflowWrap.ELLIPSES
    )

    @Test
    fun nowrap() = doTest(
        """
        ░The weather today is 21°C in Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch
        """, OverflowWrap.ELLIPSES, Whitespace.NOWRAP
    )

    private fun doTest(
        expected: String,
        wrap: OverflowWrap,
        whitespace: Whitespace = Whitespace.NORMAL,
        width: Int = 23,
    ) {
        val text = """
        ░The weather today is 21°C in
        ░Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch
        """.trimMargin("░")
        val widget = Text(
            text,
            whitespace = whitespace,
            align = TextAlign.NONE,
            overflowWrap = wrap
        )
        checkRender(widget, expected, width = width)
    }
}
