package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Renderable
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.terminal.Terminal

internal class RawRenderable(private val content: String): Renderable {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(content.length, content.length)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(listOf(listOf(Span.raw(content))))
    }
}
