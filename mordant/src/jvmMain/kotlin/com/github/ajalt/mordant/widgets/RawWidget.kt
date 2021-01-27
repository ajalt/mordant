package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.terminal.Terminal

internal class RawWidget(private val content: String) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(content.length, content.length)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(listOf(listOf(Span.raw(content))))
    }
}
