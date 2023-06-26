package com.github.ajalt.mordant.terminal


interface TerminalInterface {
    /**
     * Information about the current terminal.
     */
    val info: TerminalInfo

    /**
     * Display a PrintRequest on this terminal.
     */
    fun completePrintRequest(request: PrintRequest)

    /**
     * Read a single line of input from stdin, returning null if no input is available
     *
     * @param hideInput If true, treat the input like a password that should not be echoed to the screen as it's typed.
     * @throws UnsupportedOperationException if the current interface doesn't support reading input. Currently, all
     *   targets are supported by default except Browser JS.
     */
    fun readLineOrNull(hideInput: Boolean): String?
}

data class PrintRequest(
    /** The Text to print */
    val text: String,
    /** If True, a trailing linebreak should be written after the text. */
    val trailingLinebreak: Boolean,
    /**
     * If True, the text should be written to stderr instead of stdout.
     *
     * If this terminal doesn't have separate output streams, this can be ignored.
     */
    val stderr: Boolean,
)
