package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

internal class BlockQuote(private val content: Widget) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return content.measure(t, width - 2) + 2
    }

    override fun render(t: Terminal, width: Int): Lines {
        val bar = Span.word(t.theme.string("markdown.blockquote.bar"), t.theme.style("markdown.blockquote"))
        val justBar = Line(listOf(bar))
        val paddedBar = listOf(bar, Span.space(style = t.theme.style("markdown.blockquote")))
        val lines = content.render(t, width).withStyle(t.theme.style("markdown.blockquote")).lines
        return Lines(lines.map { if (it.isEmpty()) justBar else Line(paddedBar + it) })
    }
}
