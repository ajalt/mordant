package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

interface Renderable {
    // TODO: use a Measurement class instead of IntRange?
    fun measure(t: Terminal, width: Int = t.width): IntRange
    fun render(t: Terminal, width: Int = t.width): Lines
}
