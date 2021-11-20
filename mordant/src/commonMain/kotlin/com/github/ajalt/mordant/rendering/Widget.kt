package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.Terminal

interface Widget {
    fun measure(t: Terminal, width: Int = t.info.width): WidthRange
    fun render(t: Terminal, width: Int = t.info.width): Lines
}

