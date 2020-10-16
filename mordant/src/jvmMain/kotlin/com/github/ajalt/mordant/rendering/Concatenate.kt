package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Concatenate(val renderables: List<Renderable>) : Renderable {
    constructor(vararg renderables: Renderable) : this(renderables.asList())

    init {
        require(renderables.isNotEmpty()) { "renderables must not be empty" }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        return renderables.maxWidthRange { it.measure(t, width) }
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(renderables.flatMap { it.render(t, width).lines })
    }
}
