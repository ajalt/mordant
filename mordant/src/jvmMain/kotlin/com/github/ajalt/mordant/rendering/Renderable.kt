package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.Terminal

interface Renderable {
    fun measure(t: Terminal, width: Int = t.info.width): WidthRange
    fun render(t: Terminal, width: Int = t.info.width): Lines
}

internal object EmptyRenderable : Renderable {
    override fun measure(t: Terminal, width: Int) = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int) = EMPTY_LINES
}
