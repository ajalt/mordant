package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.rendering.OverflowWrap
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterceptor
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.progress.progressBarLayout

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
 * You can create instances of Animations with [animation], [textAnimation], animate a
 * [progressBarLayout], or by creating a subclass.
 *
 * Note that although this class's state is thread safe, calling [update] concurrently will likely
 * cause garbled output, so usage of this class should be serialized.
 */
abstract class Animation<T>(
    /**
     * By default, the animation will include a trailing linebreak. If you set this to false, you
     * won't be able to use multiple animations simultaneously.
     */
    private val trailingLinebreak: Boolean = true,
    private val terminal: Terminal,
) {
    private data class State(
        var size: Pair<Int, Int>? = null,
        var text: String? = null,
        var needsClear: Boolean = false,
        var interceptorInstalled: Boolean = false,
        // Don't move the cursor the first time the animation is drawn
        var firstDraw: Boolean = true,
    )

    private val state = MppAtomicRef(State())

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        val (old, new) = state.update { copy(firstDraw = false) }
        val t = old.text ?: return@TerminalInterceptor req
        val newText = buildString {
            if (!old.firstDraw) {
                val moves = getCursorMoves(old.needsClear || req.text.isNotEmpty(), new.size)
                moves?.let { append(it) }
            }
            if (req.text.isNotEmpty()) {
                appendLine(req.text)
            }
            append(t)
        }
        PrintRequest(
            text = newText,
            trailingLinebreak = trailingLinebreak && !terminal.info.crClearsLine,
            stderr = req.stderr
        )
    }

    protected abstract fun renderData(data: T): Widget

    /**
     * Stop this animation and remove it from the screen.
     *
     * Future calls to [update] will cause the animation to resume.
     */
    fun clear() {
        val (old, _) = doStop(true)
        getCursorMoves(clearScreen = true, old.size)?.let { terminal.rawPrint(it) }
    }

    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     *
     * Future calls to [update] will cause the animation to start again.
     *
     * ### Note
     *
     * If running on JVM when [TerminalInfo.crClearsLine] is true (such as on the IntelliJ built-in
     * console), this will not print a trailing newline, leaving the cursor on the same line as the
     * animation.
     */
    fun stop() {
        doStop(false)
    }

    private fun doStop(clearSize: Boolean): Pair<State, State> {
        val (old, new) = state.update {
            copy(
                interceptorInstalled = false,
                firstDraw = true,
                text = null,
                size = if (clearSize) null else size,
            )
        }
        if (old.interceptorInstalled) terminal.removeInterceptor(interceptor)
        return old to new
    }

    /**
     * Update the data for this animation.
     *
     * This will cause the existing animation frame to be cleared and the new frame rendered in its
     * place.
     */
    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        val height = rendered.height
        val width = rendered.width
        val (old, _) = state.update {
            copy(
                // To avoid flickering don't clear the screen if the render will completely cover
                // the last frame
                needsClear = size?.let { (h, w) -> height < h || width < w } ?: false,
                interceptorInstalled = true,
                text = terminal.render(rendered)
            )
        }
        if (!old.interceptorInstalled && terminal.info.outputInteractive) {
            terminal.addInterceptor(interceptor)
        }
        // Print an empty renderable to trigger our interceptor, which will add the rendered text
        terminal.print(EmptyWidget)
        // Update the size now that the old frame has been cleared
        state.update { copy(size = height to width) }
    }

    private fun getCursorMoves(clearScreen: Boolean, size: Pair<Int, Int>?): String? {
        val (height, _) = size ?: return null
        return terminal.cursor.getMoves {
            startOfLine()
            up(if (trailingLinebreak && !terminal.info.crClearsLine) height else height - 1)
            if (clearScreen && (height > 1 || !terminal.info.crClearsLine)) {
                clearScreenAfterCursor()
            }
        }
    }
}

/**
 * Create an [Animation] that uses the [draw] function to render objects of type [T].
 *
 * @param trailingLinebreak By default, the animation will include a trailing linebreak. If you set
 *   this to false, you won't be able to use multiple animations simultaneously.
 * @see Animation
 */
inline fun <T> Terminal.animation(
    trailingLinebreak: Boolean = true,
    crossinline draw: (T) -> Widget,
): Animation<T> {
    return object : Animation<T>(trailingLinebreak, this) {
        override fun renderData(data: T): Widget = draw(data)
    }
}

/**
 * Create an [Animation] that wraps the result of the [draw] function into a [Text] widget and
 * renders it.
 *
 * @param trailingLinebreak By default, the animation will include a trailing linebreak. If you set
 *   this to false, you won't be able to use multiple animations simultaneously.
 */
inline fun <T> Terminal.textAnimation(
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    tabWidth: Int? = null,
    trailingLinebreak: Boolean = true,
    crossinline draw: (T) -> String,
): Animation<T> {
    return object : Animation<T>(trailingLinebreak, this) {
        override fun renderData(data: T): Widget {
            return Text(draw(data), whitespace, align, overflowWrap, width, tabWidth)
        }
    }
}
