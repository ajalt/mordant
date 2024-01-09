package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.test.Test

class ViewportTest : RenderingTest(width = 20) {
    @Test
    fun crop() = forAll(
        row(null, null, "a  ␊b c"),
        row(null, 1, "a  "),
        row(1, 1, "a"),
        row(2, 1, "a "),
        row(1, null, "a␊b"),
        row(2, null, "a ␊b "),
        row(3, null, "a  ␊b c"),
        row(2, 2, "a ␊b "),
        row(4, 3, "a   ␊b c ␊    "),
        row(0, 0, ""),
    ) { w, h, ex ->
        doTest(w, h, 0, 0, ex, "a\nb c")
    }

    @Test
    fun scroll() = forAll(
        row(0, 0, "a  ␊b c"),
        row(1, 0, "   ␊ c "),
        row(2, 0, "   ␊c  "),
        row(3, 0, "   ␊   "),
        row(4, 0, "   ␊   "),
        row(0, 1, "b c␊   "),
        row(0, 2, "   ␊   "),
        row(1, 1, " c ␊   "),
        row(9, 9, "   ␊   "),
        row(-1, 0, " a ␊ b "),
        row(-2, 0, "  a␊  b"),
        row(-3, 0, "   ␊   "),
        row(0, -1, "   ␊a  "),
        row(0, -2, "   ␊   "),
        row(-9, -9, "   ␊   "),
    ) { x, y, ex ->
        doTest(null, null, x, y, ex, "a\nb c")
    }
    // TODO: test scrolling to the middle of a span with a different style than the one before
    private fun doTest(w: Int?, h: Int?, x: Int, y: Int, ex: String, txt: String) {
        checkRender(Viewport(Text(txt), w, h, x, y), ex, trimMargin = false) {
            it.replace('\n', '␊')
        }
    }
}

