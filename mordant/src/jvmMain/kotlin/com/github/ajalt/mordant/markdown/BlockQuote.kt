package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Renderable
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.WidthRange

internal class BlockQuote(private val content: Renderable) : Renderable {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return content.measure(t, width - 2) + 2
    }

    override fun render(t: Terminal, width: Int): Lines {
        val bar = Span.word("▎", t.theme.blockQuote)
        val justBar = listOf(bar)
        val paddedBar = listOf(bar, Span.space(style = t.theme.blockQuote))
        val lines = content.render(t, width).withStyle(t.theme.blockQuote).lines
        return Lines(lines.map { if (it.isEmpty()) justBar else paddedBar + it })
    }
}
