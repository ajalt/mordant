package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

interface Renderable {
    fun measure(t: Terminal, width: Int = t.width): WidthRange
    fun render(t: Terminal, width: Int = t.width): Lines
}
