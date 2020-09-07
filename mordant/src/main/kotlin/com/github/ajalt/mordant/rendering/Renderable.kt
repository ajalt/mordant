package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

interface Renderable {
    fun measure(t: Terminal, width: Int = t.width): WidthRange
    fun render(t: Terminal, width: Int = t.width): Lines
}

internal object EMPTY_RENDERABLE: Renderable {
    override fun measure(t: Terminal, width: Int) = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int) = EMPTY_LINES
}
