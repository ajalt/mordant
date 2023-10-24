package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

/** A widget that displays blank space the same size as an [inner] widget */
internal class BlankWidgetWrapper(private val inner: Widget) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange = inner.measure(t, width)

    override fun render(t: Terminal, width: Int): Lines {
        val orig = inner.render(t, width)
        return Lines(orig.lines.map { Line(listOf(Span.space(it.lineWidth))) })
    }
}
