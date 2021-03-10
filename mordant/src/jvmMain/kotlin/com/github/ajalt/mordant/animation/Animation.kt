package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInterceptor
import com.github.ajalt.mordant.widgets.RawWidget
import com.github.ajalt.mordant.widgets.Text

@OptIn(ExperimentalTerminalApi::class)
abstract class Animation<T>(private val terminal: Terminal) {
    private var size: Pair<Int, Int>? = null
    private var text: String? = null

    // Don't clear the screen the first time the animation is drawn
    private var needsClear = false

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        text?.let { t ->
            PrintRequest(text = buildString {
                if (needsClear) {
                    getClear()?.let { append(it) }
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

    protected abstract fun renderData(data: T): Widget

    fun clear() {
        getClear()?.let {
            text = null
            terminal.removeInterceptor(interceptor)
            terminal.print(RawWidget(it))
        }
    }

    private fun getClear(): String? {
        val (height, _) = size ?: return null
        return terminal.cursor.getMoves {
            startOfLine()
            up(height)
            clearScreenAfterCursor()
        }
    }

    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        // Only clear the screen if we've previously drawn our animation
        size = rendered.height to rendered.width
        text = terminal.render(rendered)
        // Print an empty renderable to trigger our interceptor, which will add the rendered text
        terminal.print(EmptyWidget)
    }
}

inline fun <T> Terminal.animation(crossinline draw: (T) -> Widget): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Widget = draw(data)
    }
}

inline fun <T> Terminal.textAnimation(
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    tabWidth: Int? = null,
    crossinline draw: (T) -> String,
): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Widget =
            Text(draw(data), whitespace, align, overflowWrap, width, tabWidth)
    }
}
