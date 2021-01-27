package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.terminal.Terminal

private class Concatenate(private val widgets: List<Widget>) : Widget {
    init {
        require(widgets.isNotEmpty()) { "renderables must not be empty" }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        return widgets.maxWidthRange { it.measure(t, width) }
    }

    override fun render(t: Terminal, width: Int): Lines {
        return Lines(widgets.flatMap { it.render(t, width).lines })
    }
}

class WidgetBuilder {
    private val widgets = mutableListOf<Widget>()

    fun appendAll(widgets: Iterable<Widget>): WidgetBuilder = apply {
        widgets.forEach { appendln(it) }
    }

    fun appendln(
        message: Any?,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null
    ): WidgetBuilder = apply {
        if (message is Widget) widgets += message
        else widgets += Text(message.toString(), style, whitespace, align, overflowWrap, width)
    }

    fun appendln(widget: Widget): WidgetBuilder = apply {
        widgets += widget
    }

    /** Append a blank line */
    fun appendln(): WidgetBuilder = apply {
        widgets += LinebreakWidget
    }

    fun build(): Widget = Concatenate(widgets)
}

inline fun buildWidget(action: WidgetBuilder.() -> Unit): Widget {
    return WidgetBuilder().apply(action).build()
}

private object LinebreakWidget : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int): Lines = Lines(listOf(emptyList()))
}
