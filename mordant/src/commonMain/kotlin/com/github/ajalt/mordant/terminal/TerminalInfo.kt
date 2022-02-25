package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel


/**
 * Information about the current terminal
 *
 * [width] and [height] don't automatically change if the terminal is resized. Call [updateTerminalSize] to get the
 * latest values.
 *
 * @property width The terminal width, in cells
 * @property height The terminal height, in cells
 * @property ansiLevel The level of ANSI codes to use when printing to the terminal
 * @property ansiHyperLinks If true, ANSI hyperlink codes can be used
 * @property outputInteractive If false the output stream is not an interactive terminal, such as when it's redirected to a file
 * @property inputInteractive If false the output stream is not an interactive terminal, such as when it's redirected from a file
 * @property crClearsLine If true, `\r` will clear the entire line it's printed on in the current terminal, if false,
 *   `\r` will only move the cursor
 */
class TerminalInfo(
    width: Int,
    height: Int,
    var ansiLevel: AnsiLevel,
    var ansiHyperLinks: Boolean,
    val outputInteractive: Boolean,
    val inputInteractive: Boolean,
    val crClearsLine: Boolean,
) {
    var width: Int = width
        private set
    var height: Int = height
        private set


    /** Return true if both input and output are interactive */
    val interactive: Boolean get() = inputInteractive && inputInteractive

    /**
     * Query the terminal for its current size, updating [width] and [height] if successful.
     *
     * On JVM, this call will create a subprocess and block for up to [timeoutMs] waiting for it to complete.
     * On other platforms, this call doesn't require a subprocess, and [timeoutMs] is ignored.
     *
     * @return `true` if the size was updated, of `false` if it was not
     */
    fun updateTerminalSize(timeoutMs: Long = 5000): Boolean {
        val (w, h) = TerminalDetection.detectSize(timeoutMs) ?: return false
        width = w
        height = h
        return true
    }
}
