package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

interface Renderable {
    fun render(t: Terminal): List<Span>
    fun measure(t: Terminal): IntRange
}
