package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.js.JsName
import kotlin.test.Test

class CropTest : RenderingTest(width = 20) {
    @Test
    fun crop() = forAll(
        row(null, null, "a\nbb\n"),
        row(null, 1, "a\n"),
        row(2, 1, "a \n"),
        row(1, null, "a\nb\n"),
        row(2, 2, "a \nbb\n"),
        row(3, 3, "a  \nbb \n   \n"),
        row(0, 0, ""),
    ) { w, h, ex ->
        checkRender(Crop(W, w, h), ex)
    }
}

private object W : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(2, 2)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(
            listOf(
                Line(listOf(Span.word("a"))),
                Line(listOf(Span.word("bb"))),
            )
        )
    }
}
