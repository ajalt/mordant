package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.*
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.RawWidget
import com.github.ajalt.mordant.widgets.Text

/**
 * An Animation renders a widget to the screen each time [update] is called, clearing the render
 * from the previous call.
 *
 * If you [print][Terminal.print] anything in between calls to [update], the rendered widget will be
 * cleared, before the print, then rendered again below it.
 *
 * Note that [Animation] doesn't render the widget automatically: you need to call [update] yourself
 * when your data changes. If your terminal is not [interactive][TerminalInfo.interactive], the
 * animation will not render anything.
 *
 * You can create instances of Animations with [animation], [textAnimation], and `progressAnimation`
 * (on JVM), or by creating a subclass.
 */
@OptIn(ExperimentalTerminalApi::class)
abstract class Animation<T>(private val terminal: Terminal) {
    private var size: Pair<Int, Int>? = null
    private var text: String? = null
    private var needsClear = false
    private var interceptorInstalled = false

    // Don't move the cursor the first time the animation is drawn
    private var firstDraw = true

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        text?.let { t ->
            PrintRequest(text = buildString {
                if (!firstDraw) {
                    getCursorMoves(needsClear || req.text.isNotEmpty())?.let { append(it) }
                }
                firstDraw = false
                if (req.text.isNotEmpty()) {
                    appendLine(req.text)
                }
                append(t)
            }, trailingLinebreak = false)
        } ?: req
    }

    protected abstract fun renderData(data: T): Widget

    /**
     * Stop this animation and remove it from the screen.
     *
     * Future calls to [update] will cause the animation to resume.
     */
    fun clear() {
        stop()
        getCursorMoves(clearScreen = true)?.let { terminal.rawPrint(it) }
        size = null
    }

    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     *
     * Future calls to [update] will cause the animation to start again.
     */
    fun stop() {
        if (interceptorInstalled) terminal.removeInterceptor(interceptor)
        if (!firstDraw) terminal.println()
        interceptorInstalled = false
        firstDraw = true
        text = null
    }

    /**
     * Update the data for this animation.
     *
     * This will cause the existing animation frame to be cleared and the new frame rendered in its
     * place.
     */
    fun update(data: T) {
        if (!interceptorInstalled && terminal.info.outputInteractive) {
            terminal.addInterceptor(interceptor)
        }
        interceptorInstalled = true
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

    private fun getCursorMoves(clearScreen: Boolean): String? {
        val (height, _) = size ?: return null
        return terminal.cursor.getMoves {
            startOfLine()
            up(height - 1)
            if (clearScreen) clearScreenAfterCursor()
        }
    }
}

/**
 * Create an [Animation] that uses the [draw] function to render objects of type [T].
 *
 * @see Animation
 */
inline fun <T> Terminal.animation(crossinline draw: (T) -> Widget): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Widget = draw(data)
    }
}

/**
 * Create an [Animation] that wraps the result of the [draw] function into a [Text] widget and renders it.
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
        override fun renderData(data: T): Widget {
            return Text(draw(data), whitespace, align, overflowWrap, width, tabWidth)
        }
    }
}
