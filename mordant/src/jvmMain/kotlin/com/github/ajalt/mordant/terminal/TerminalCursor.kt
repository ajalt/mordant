package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.widgets.RawWidget

interface TerminalCursor {
    fun show()
    fun hide(showOnExit: Boolean = true)
    fun move(movements: CursorMovements.() -> Unit)
    fun getMoves(movements: CursorMovements.() -> Unit): String
}

interface CursorMovements {
    /**
     * Print an ANSI code to move the cursor up [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, nothing is printed.
     * If [count] is negative, the cursor will be moved down instead.
     */
    fun up(count: Int)

    /**
     * Print an ANSI code to move the cursor down [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, nothing is printed.
     * If [count] is negative, the cursor will be moved up instead.
     */
    fun down(count: Int)

    /**
     * Print an ANSI code to move the cursor right [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, nothing is printed.
     * If [count] is negative, the cursor will be moved left instead.
     */
    fun right(count: Int)

    /**
     * Print an ANSI code to move the cursor left [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, nothing is printed.
     * If [count] is negative, the cursor will be moved right instead.
     */
    fun left(count: Int)

    fun startOfLine()

    // TODO: docs
    /**
     * [x] and [y] are 0-indexed
     */
    fun setPosition(x: Int, y: Int)

    fun clearScreen()
    fun clearScreenBeforeCursor()
    fun clearScreenAfterCursor()
    fun clearLine()
    fun clearLineBeforeCursor()
    fun clearLineAfterCursor()
    fun savePosition()
    fun restorePosition()
}

private class AnsiMovements : CursorMovements {
    private val builder = StringBuilder()

    val output get() = builder.toString()

    override fun up(count: Int) {
        when {
            count < 0 -> down(-count)
            count > 0 -> csi("${count}A")
        }
    }


    override fun down(count: Int) {
        when {
            count < 0 -> up(-count)
            count > 0 -> csi("${count}B")
        }
    }

    override fun right(count: Int) {
        when {
            count < 0 -> left(-count)
            count > 0 -> csi("${count}C")
        }
    }

    override fun left(count: Int) {
        when {
            count < 0 -> right(-count)
            count > 0 -> csi("${count}D")
        }
    }

    override fun startOfLine() {
        print("\r")
    }

    override fun setPosition(x: Int, y: Int) {
        require(x >= 0) { "Invalid cursor column $x; value cannot be negative" }
        require(y >= 0) { "Invalid cursor column $y; value cannot be negative" }

        // CUP command is 1-indexed, args are ROW;COLUMN
        csi("${y + 1};${x + 1}H")
    }

    override fun clearScreen() {
        csi("2J")
    }

    override fun clearScreenAfterCursor() {
        csi("0J")
    }

    override fun clearScreenBeforeCursor() {
        csi("1J")
    }

    override fun clearLineAfterCursor() {
        csi("0K")
    }

    override fun clearLineBeforeCursor() {
        csi("1K")
    }

    override fun clearLine() {
        csi("2K")
    }

    override fun savePosition() {
        csi("s")
    }

    override fun restorePosition() {
        csi("u")
    }

    private fun csi(command: String) {
        print(CSI + command)
    }

    private fun print(text: String) = builder.append(text)
}

internal object DisabledTerminalCursor : TerminalCursor {
    override fun show() {}
    override fun hide(showOnExit: Boolean) {}
    override fun move(movements: CursorMovements.() -> Unit) {}
    override fun getMoves(movements: CursorMovements.() -> Unit): String = ""
}

internal class PrintTerminalCursor(private val terminal: Terminal) : TerminalCursor {
    private var shutdownHook: Thread? = null
    private val lock = Any()

    override fun show() {
        synchronized(lock) {
            shutdownHook?.let { hook ->
                Runtime.getRuntime().removeShutdownHook(hook)
            }
        }
        terminal.print(RawWidget("$CSI?25h"))
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit) {
            synchronized(lock) {
                if (shutdownHook == null) {
                    shutdownHook = Thread { show() }
                    Runtime.getRuntime().addShutdownHook(shutdownHook)
                }
            }
        }
        terminal.print(RawWidget("$CSI?25l"))
    }

    override fun move(movements: CursorMovements.() -> Unit) {
        terminal.print(RawWidget(getMoves(movements)))
    }

    override fun getMoves(movements: CursorMovements.() -> Unit): String {
        return AnsiMovements().apply(movements).output
    }
}
