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

class RenderableBuilder {
    private val renderables = mutableListOf<Renderable>()

    fun append(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        renderables += Text(message.toString(), style, whitespace, align, overflowWrap, width)
    }

    fun append(renderable: Renderable) {
        renderables += renderable
    }

    fun build(): Renderable = Concatenate(renderables)
}

inline fun buildRenderable(action: RenderableBuilder.() -> Unit): Renderable {
    return RenderableBuilder().apply { action() }.build()
}
