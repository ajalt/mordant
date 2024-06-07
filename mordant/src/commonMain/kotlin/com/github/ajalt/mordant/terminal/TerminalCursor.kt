package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI

interface TerminalCursor {
    /**
     * Show the cursor
     */
    fun show()

    /**
     * Hide the cursor
     *
     * @param showOnExit By default, the cursor will be shown again when the program terminates. If
     *   false, the cursor will remain hidden
     */
    fun hide(showOnExit: Boolean = true)

    /**
     * Print the movement commands called in [movements].
     *
     * If the terminal is not interactive, this call does nothing.
     */
    fun move(movements: CursorMovements.() -> Unit)

    /**
     * Return a string contianing the ANSI codes for the movements commands called in [movements].
     *
     * If the terminal is not interactive, this will always return an empty string.
     */
    fun getMoves(movements: CursorMovements.() -> Unit): String
}

interface CursorMovements {
    /**
     * Move the cursor up [count] cells.
     *
     * If [count] is 0, this call does nothing.
     * If [count] is negative, the cursor will be moved down instead.
     */
    fun up(count: Int)

    /**
     * Move the cursor down [count] cells.
     *
     * If [count] is 0, this call does nothing.
     * If [count] is negative, the cursor will be moved up instead.
     */
    fun down(count: Int)

    /**
     * Move the cursor right [count] cells.
     *
     * If [count] is 0, this call does nothing.
     * If [count] is negative, the cursor will be moved left instead.
     */
    fun right(count: Int)

    /**
     * Move the cursor left [count] cells.
     *
     * If [count] is 0, this call does nothing.
     * If [count] is negative, the cursor will be moved right instead.
     */
    fun left(count: Int)

    /** Move the cursor to the start of the current line */
    fun startOfLine()

    /**
     * Set the cursor to an absolute position.
     *
     * [x] and [y] are 0-indexed, with (0, 0) being the top-left corner.
     */
    fun setPosition(x: Int, y: Int)

    /**
     * Clear the entire terminal screen.
     */
    fun clearScreen()

    /**
     * Clear the screen before the cursor, leaving anything after the cursor unchanged.
     */
    fun clearScreenBeforeCursor()

    /**
     * Clear the screen after the cursor, leaving anything before the cursor unchanged.
     */
    fun clearScreenAfterCursor()


    /**
     * Clear the current line.
     */
    fun clearLine()

    /**
     * Clear the line before the cursor, leaving anything after the cursor unchanged.
     */
    fun clearLineBeforeCursor()

    /**
     * Clear the line after the cursor, leaving anything after the cursor unchanged.
     */
    fun clearLineAfterCursor()

    /**
     * Save the current cursor position. Restore it with [restorePosition]
     */
    fun savePosition()

    /**
     * Move the cursor to the position saved with a prior [savePosition] call.
     */
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

    override fun setPosition(x: Int, y: Int) {
        require(x >= 0) { "Invalid cursor column $x; value cannot be negative" }
        require(y >= 0) { "Invalid cursor column $y; value cannot be negative" }

        // CUP command is 1-indexed, args are ROW;COLUMN
        csi("${y + 1};${x + 1}H")
    }

    override fun startOfLine() = print("\r")
    override fun clearScreen() = csi("2J")
    override fun clearScreenAfterCursor() = csi("0J")
    override fun clearScreenBeforeCursor() = csi("1J")
    override fun clearLineAfterCursor() = csi("0K")
    override fun clearLineBeforeCursor() = csi("1K")
    override fun clearLine() = csi("2K")
    override fun savePosition() = csi("s")
    override fun restorePosition() = csi("u")

    private fun csi(command: String) = print(CSI + command)
    private fun print(text: String) {
        builder.append(text)
    }
}

internal object DisabledTerminalCursor : TerminalCursor {
    override fun show() {}
    override fun hide(showOnExit: Boolean) {}
    override fun move(movements: CursorMovements.() -> Unit) {}
    override fun getMoves(movements: CursorMovements.() -> Unit): String = ""
}

internal abstract class PrintTerminalCursor(protected val terminal: Terminal) : TerminalCursor {
    override fun show() {
        terminal.rawPrint("$CSI?25h")
    }

    override fun hide(showOnExit: Boolean) {
        terminal.rawPrint("$CSI?25l")
    }

    override fun move(movements: CursorMovements.() -> Unit) {
        terminal.rawPrint(getMoves(movements))
    }

    override fun getMoves(movements: CursorMovements.() -> Unit): String {
        return AnsiMovements().apply(movements).output
    }
}
