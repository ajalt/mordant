package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.internal.CSI

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

    // TODO: docs
    /**
     * [x] and [y] are 0-indexed
     */
    fun setPosition(x: Int, y: Int)
    fun clearScreen()
    /**
     * Print an ANSI command to show or hide the terminal's cursor.
     *
     * If ANSI codes are not supported, nothing is printed.
     */
    fun setVisible(visible: Boolean)
}

internal object DisabledTerminalCursor : TerminalCursor {
    override fun up(count: Int) {}
    override fun down(count: Int) {}
    override fun right(count: Int) {}
    override fun left(count: Int) {}
    override fun setPosition(x: Int, y: Int) {}
    override fun clearScreen() {}
    override fun setVisible(visible: Boolean) {}
}

internal open class PrintTerminalCursor : TerminalCursor {
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

    /**
     * [x] and [y] are 0-indexed
     */
    override fun setPosition(x: Int, y: Int) {
        require(x >= 0) { "Invalid cursor column $x; value cannot be negative" }
        require(y >= 0) { "Invalid cursor column $y; value cannot be negative" }

        // CUP command is 1-indexed, args are ROW;COLUMN
        csi("${y + 1};${x + 1}H")
    }

    override fun clearScreen() {
        csi("2J")
    }

    override fun setVisible(visible: Boolean) {
        csi("?25${if (visible) "h" else "l"}")
    }

    private fun csi(command: String) {
        print(CSI + command)
    }

    open fun print(text: String) = kotlin.io.print(text)
}
