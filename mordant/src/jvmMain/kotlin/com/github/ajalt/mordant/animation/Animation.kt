package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.RawRenderable
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInterceptor

abstract class Animation<T>(private val terminal: Terminal) {
    private var size: Pair<Int, Int>? = null
    private var text: String? = null
    // Don't clear the screen the first time the animation is drawn
    private var needsClear = false

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        text?.let { t ->
            PrintRequest(text = buildString {
                if (needsClear) {
                    getClear(req.text.isNotEmpty())?.let { append(it) }
                }
                needsClear = true
                if (req.text.isNotEmpty()) {
                    appendLine(req.text)
                }
                append(t)
            }, trailingLinebreak = true)
        } ?: req
    }

    init {
        if (terminal.info.stdoutInteractive) {
            terminal.addInterceptor(interceptor)
        }
    }

    protected abstract fun renderData(data: T): Renderable

    fun clear() {
        getClear(true)?.let {
            text = null
            terminal.removeInterceptor(interceptor)
            terminal.print(RawRenderable(it))
        }
    }

    private fun getClear(clearScreen: Boolean): String? {
        val (height, _) = size ?: return null
        return terminal.cursor.getMoves {
            startOfLine()
            up(height)

            if (clearScreen) {
                clearScreenAfterCursor()
            }
        }
    }

    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        // Only clear the screen if we've previously drawn our animation
        size = rendered.height to rendered.width
        text = terminal.render(rendered)
        // Print an empty renderable to trigger our interceptor, which will add the rendered text
        terminal.print(EmptyRenderable)
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
        override fun renderData(data: T): Renderable =
            Text(draw(data), style, whitespace, align, overflowWrap, width, tabWidth)
    }
}
