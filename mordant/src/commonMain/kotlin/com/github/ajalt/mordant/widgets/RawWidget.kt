package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

internal class RawWidget(private val content: String) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(content.length, content.length)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(listOf(Line(listOf(Span.raw(content)))))
    }
}
