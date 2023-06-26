package com.github.ajalt.mordant.terminal


interface TerminalInterface {
    val info: TerminalInfo
    fun completePrintRequest(request: PrintRequest)

    /**
     * Read a single line of input from stdin, returning null if no input is available
     *
     * @param hideInput If true, treat the input like a password that should not be echoed to the screen as it's typed.
     * @throws UnsupportedOperationException if the current interface doesn't support reading input. Currently, all
     *   targets are supported by default except Browser JS.
     */
    fun readLineOrNull(hideInput: Boolean): String?

    /**
     * Return an interface that should print to stderr if possible.
     *
     * If the interface doesn't support stdErr (for example, if it writes to an html field in the browser rather than a
     * desktop terminal), the same instance may be returned.
     */
    fun forStdErr(): TerminalInterface
}

data class PrintRequest(
    val text: String,
    val trailingLinebreak: Boolean,
)
