package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.EMPTY_LINES
import com.github.ajalt.mordant.terminal.Terminal

interface Widget {
    fun measure(t: Terminal, width: Int = t.info.width): WidthRange
    fun render(t: Terminal, width: Int = t.info.width): Lines
}

internal object EmptyWidget : Widget {
    override fun measure(t: Terminal, width: Int) = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int) = EMPTY_LINES
}
