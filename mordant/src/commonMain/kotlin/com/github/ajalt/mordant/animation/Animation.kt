package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

abstract class Animation<T>(private val terminal: Terminal) {
    private var lastRender: Lines? = null

    protected abstract fun renderData(data: T): Renderable

    fun log(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        clear()
        terminal.println(message, style, whitespace, align, overflowWrap, width)
        lastRender?.let { terminal.rawPrint(terminal.render(it)) }
    }

    fun clear() = clear(0, 0)

    private fun clear(renderedHeight: Int, renderedWidth: Int) {
        val lastRender = lastRender ?: return
        terminal.cursor.startOfLine()
        terminal.cursor.up(lastRender.height - 1)

        if (lastRender.height > renderedHeight || lastRender.width > renderedWidth) {
            terminal.cursor.clearScreenAfterCursor()
        }
    }

    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        clear(rendered.height, rendered.width)
        lastRender = rendered
        terminal.rawPrint(terminal.render(rendered))
    }
}

inline fun <T> Terminal.animation(crossinline draw: (T) -> Renderable): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Renderable = draw(data)
    }
}

inline fun <T> Terminal.textAnimation(
        style: TextStyle = TextStyle(),
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
        tabWidth: Int? = null,
        crossinline draw: (T) -> String,
): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Renderable = Text(draw(data), style, whitespace, align, overflowWrap, width, tabWidth)
    }
}
