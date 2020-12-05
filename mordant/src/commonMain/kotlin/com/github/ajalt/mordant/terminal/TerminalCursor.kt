package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI

interface TerminalCursor {
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

    fun show()
    fun hide(showOnExit: Boolean = true)

    fun clearScreen()
    fun clearScreenAfterCursor()
}

internal object DisabledTerminalCursor : TerminalCursor {
    override fun up(count: Int) {}
    override fun down(count: Int) {}
    override fun right(count: Int) {}
    override fun left(count: Int) {}
    override fun startOfLine() {}
    override fun setPosition(x: Int, y: Int) {}
    override fun show() {}
    override fun hide(showOnExit: Boolean) {}
    override fun clearScreen() {}
    override fun clearScreenAfterCursor() {}
}

internal class PrintTerminalCursor(private val terminal: Terminal) : TerminalCursor {
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

    /**
     * [x] and [y] are 0-indexed
     */
    override fun setPosition(x: Int, y: Int) {
        require(x >= 0) { "Invalid cursor column $x; value cannot be negative" }
        require(y >= 0) { "Invalid cursor column $y; value cannot be negative" }

        // CUP command is 1-indexed, args are ROW;COLUMN
        csi("${y + 1};${x + 1}H")
    }

    override fun show() {
        csi("?25h")
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit) {
            // TODO: Runtime.getRuntime().addShutdownHook(Thread { show() })
        }
        csi("?25l")
    }

    override fun clearScreen() {
        csi("2J")
    }

    override fun clearScreenAfterCursor() {
        csi("0J")
    }

    private fun csi(command: String) {
        print(CSI + command)
    }

    private fun print(text: String) = terminal.rawPrint(text)
}
