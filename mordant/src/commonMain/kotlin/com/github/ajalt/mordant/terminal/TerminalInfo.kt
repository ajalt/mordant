package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel


/**
 * Information about the current terminal
 *
 * [width] and [height] don't automatically change if the terminal is resized. Call
 * [updateTerminalSize] to get the latest values.
 */
data class TerminalInfo(
    /** The terminal width, in cells */
    var width: Int,
    /** The terminal height, in cells */
    var height: Int,
    /** The level of ANSI codes to use when printing to the terminal */
    var ansiLevel: AnsiLevel,
    /** If true, ANSI hyperlink codes can be used */
    var ansiHyperLinks: Boolean,
    /**
     * If false the output stream is not an interactive terminal, such as when it's redirected to a
     * file.
     */
    val outputInteractive: Boolean,
    /**
     * If false the intput stream is not an interactive terminal, such as when it's redirected from
     * a file
     */
    val inputInteractive: Boolean,
    /**
     * If true, `\r` will clear the entire line it's printed on in the current terminal, if false,
     * `\r` will only move the cursor
     */
    val crClearsLine: Boolean,
) {
    /** Return true if both input and output are interactive */
    val interactive: Boolean get() = inputInteractive && outputInteractive

    /**
     * Query the terminal for its current size, updating [width] and [height] if successful.
     *
     * @return `true` if the terminal size was queried successfully (even if the size hasn't
     *  changed), or false if the size could not be determined.
     */
    fun updateTerminalSize(): Boolean {
        val (w, h) = TerminalDetection.detectSize() ?: return false
        width = w
        height = h
        return true
    }

    @Deprecated(
        "The timeoutMs parameter is no longer used; this function does not open a subprocess",
        ReplaceWith("updateTerminalSize()")
    )
    @Suppress("UNUSED_PARAMETER")
    fun updateTerminalSize(timeoutMs: Long): Boolean = updateTerminalSize()
}
