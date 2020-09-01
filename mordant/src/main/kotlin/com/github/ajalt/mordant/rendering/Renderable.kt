package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

interface Renderable {
    fun render(t: Terminal, width: Int = t.width): List<Span>
    fun measure(t: Terminal, width: Int = t.width): IntRange
}
