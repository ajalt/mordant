package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.terminal.Terminal

internal class Concatenate(val renderables: List<Renderable>) : Renderable {
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

    fun appendAll(renderables: Iterable<Renderable>): RenderableBuilder = apply {
        renderables.forEach { appendln(it) }
    }

    fun appendln(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): RenderableBuilder = apply {
        if (message is Renderable) renderables += message
        else renderables += Text(message.toString(), style, whitespace, align, overflowWrap, width)
    }

    fun appendln(renderable: Renderable): RenderableBuilder = apply {
        renderables += renderable
    }

    /** Append a blank line */
    fun appendln(): RenderableBuilder = apply {
        renderables += LinebreakRenderable
    }

    fun build(): Renderable = Concatenate(renderables)
}

inline fun buildRenderable(action: RenderableBuilder.() -> Unit): Renderable {
    return RenderableBuilder().apply { action() }.build()
}

private object LinebreakRenderable : Renderable {
    override fun measure(t: Terminal, width: Int): WidthRange = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int): Lines = Lines(listOf(emptyList()))
}
