package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInterceptor
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.RawWidget
import com.github.ajalt.mordant.widgets.Text

/**
 * A base class for objects that can turn arbitrary objects into rendered widgets.
 *
 * [Animation] by itself doesn't handle actually triggering frames - if you use this that's up to you. Instead, it handles clearing the
 * areas of the screen needed as the widgets change, and ensuring that anything rendered via [Terminal.println] appears above the animated
 * widgets. Override [renderData] to provide a [Widget] for display.
 */
@OptIn(ExperimentalTerminalApi::class)
abstract class Animation<T>(private val terminal: Terminal) {
    private var size: Pair<Int, Int>? = null
    private var text: String? = null
    private var needsClear = false

    // Don't move the cursor the first time the animation is drawn
    private var firstDraw = true

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        text?.let { t ->
            PrintRequest(text = buildString {
                if (req.text.isNotEmpty())
                    needsClear = true

                if (!firstDraw) {
                    getClear()?.let { append(it) }
                }
                firstDraw = false
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
        needsClear = true
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
            if (needsClear) clearScreenAfterCursor()
        }
    }

    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        val height = rendered.height
        val width = rendered.width
        // To avoid flickering don't clear the screen if the render will completely cover the last frame
        needsClear = size?.let { (h, w) -> height < h || width < w } ?: false
        size = height to width
        text = terminal.render(rendered)
        // Print an empty renderable to trigger our interceptor, which will add the rendered text
        terminal.print(EmptyWidget)
    }
}

/**
 * Returns an anonymous subclass of [Animation] that uses the [draw] function to render objects of type [T].
 *
 * @see Animation
 */
inline fun <T> Terminal.animation(crossinline draw: (T) -> Widget): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Widget = draw(data)
    }
}

/**
 * Returns an anonymous subclass of [Animation] that wraps the result of the [draw] function into a [Text] and renders that.
 */
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
