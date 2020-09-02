package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Panel(private val content: Renderable): Renderable {
    override fun render(t: Terminal, width: Int): List<Span> {
        TODO()
    }

    override fun measure(t: Terminal, width: Int): IntRange {
        TODO()
    }
}
