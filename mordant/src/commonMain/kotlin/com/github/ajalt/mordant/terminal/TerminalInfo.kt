package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel


/**
 * Information about the current terminal
 */
data class TerminalInfo(
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
    val supportsAnsiCursor: Boolean,
) {
    /** Return true if both input and output are interactive */
    val interactive: Boolean get() = inputInteractive && outputInteractive
}
