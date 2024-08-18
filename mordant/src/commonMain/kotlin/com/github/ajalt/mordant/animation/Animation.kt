package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.CR_IMPLIES_LF
import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.rendering.*
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
    @Deprecated("This parameter is ignored; animations never print a trailing linebreak.")
    private val trailingLinebreak: Boolean = true,
    val terminal: Terminal,
): StoppableAnimation {
    private data class State(
        /** The length of each line of the last rendered widget */
        val size: List<Int>? = null,
        /** The length of each line of the previous rendered widget */
        val lastSize: List<Int>? = null,
        val lastTerminalSize: Size? = null,
        val text: String? = null,
        val interceptorInstalled: Boolean = false,
        val firstDraw: Boolean = true,
    )

    private val state = MppAtomicRef(State())

    private val interceptor: TerminalInterceptor = TerminalInterceptor { req ->
        val terminalSize = Size(terminal.size.width, terminal.size.height)
        val (st, _) = state.update {
            copy(
                firstDraw = false,
                lastSize = size,
                lastTerminalSize = terminalSize,
            )
        }
        val animationText = st.text ?: return@TerminalInterceptor req
        val newText = buildString {
            // move the cursor to the start of the widget, then append the request (which might
            // start with moves if it's an animation), then our text
            getCursorMoves(
                firstDraw = st.firstDraw,
                clearScreen = req.text.isNotEmpty(),
                lastSize = st.lastSize,
                size = st.size,
                terminalSize = terminalSize,
                lastTerminalSize = st.lastTerminalSize,
                extraUp = if (req.text.startsWith("\r")) 1 else 0, // it's another animation
            )?.let { append(it) }
            when {
                req.text.endsWith("\n") -> append(req.text)
                req.text.isNotEmpty() -> appendLine(req.text)
            }
            append(animationText)
        }

        PrintRequest(
            text = newText,
            trailingLinebreak = false,
            stderr = req.stderr
        )
    }

    protected abstract fun renderData(data: T): Widget

    /**
     * Stop this animation and remove it from the screen.
     *
     * Future calls to [update] will cause the animation to resume.
     */
    final override fun clear() {
        val (old, _) = doStop(clearSize = true, newline = false)
        getCursorMoves(
            firstDraw = false,
            clearScreen = true,
            lastSize = old.size,
            size = null,
            terminalSize = Size(terminal.size.width, terminal.size.height),
            lastTerminalSize = old.lastTerminalSize,
            // if we previously stopped, we need to move up past the final newline we added
            extraUp = if (old.firstDraw && old.size != null) 1 else 0,
        )?.let { terminal.rawPrint(it) }
    }

    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     *
     * Future calls to [update] will cause the animation to start again.
     */
    final override fun stop() {
        doStop(clearSize = false, newline = true)
    }

    private fun doStop(clearSize: Boolean, newline: Boolean): Pair<State, State> {
        val (old, new) = state.update {
            copy(
                interceptorInstalled = false,
                firstDraw = true,
                text = null,
                size = if (clearSize) null else size,
            )
        }
        if (old.interceptorInstalled) {
            terminal.removeInterceptor(interceptor)
            if (newline) terminal.println()
        }
        return old to new
    }

    /**
     * Update the data for this animation.
     *
     * This will cause the existing animation frame to be cleared and the new frame rendered in its
     * place.
     */
    fun update(data: T) {
        if (terminal.terminalInterface.shouldAutoUpdateSize()) terminal.updateSize()

        val rendered = renderData(data).render(terminal)
        val (old, _) = state.update {
            copy(
                size = rendered.lines.map { it.lineWidth },
                lastSize = size,
                interceptorInstalled = true,
                text = terminal.render(rendered)
            )
        }
        if (!old.interceptorInstalled && terminal.info.outputInteractive) {
            terminal.addInterceptor(interceptor)
        }
        // Print an empty widget to trigger our interceptor, which will add the rendered text
        terminal.print(EmptyWidget)
    }

    private fun getCursorMoves(
        firstDraw: Boolean,
        clearScreen: Boolean,
        lastSize: List<Int>?,
        size: List<Int>?,
        terminalSize: Size,
        lastTerminalSize: Size?,
        extraUp: Int = 0,
    ): String? {
        if (firstDraw || lastSize == null) return null
        return terminal.cursor.getMoves {
            startOfLine()
            if (CR_IMPLIES_LF) up(1)

            if (terminal.info.crClearsLine) {
                // IntelliJ doesn't support cursor moves, so this is all we can do
                return@getMoves
            }

            val lastWidth = lastSize.maxOrNull() ?: 0
            val lastHeight = lastSize.size
            val terminalShrank = lastTerminalSize != null
                    && terminalSize.width < lastTerminalSize.width
                    && terminalSize.width < lastWidth
            val widgetShrank = size != null && (
                    size.size < lastHeight
                            || size.zip(lastSize).any { (a, b) -> a < b }
                    )
            val up = if (terminalShrank) {
                // The terminal shrank and caused the text to wrap, we need to move back to the
                // start of the text
                lastHeight * (lastWidth.toDouble() / terminalSize.width).toInt()
            } else {
                (lastHeight - 1).coerceAtLeast(0)
            }

            up(up + extraUp)

            // To avoid flickering don't clear the screen if the render will completely cover
            // the last frame
            if (terminalShrank || widgetShrank || clearScreen) clearScreenAfterCursor()
        }
    }
}

/**
 * Create an [Animation] that uses the [draw] function to render objects of type [T].
 *
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
