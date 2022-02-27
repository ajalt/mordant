package com.github.ajalt.mordant.terminal


@ExperimentalTerminalApi
interface TerminalInterface {
    val info: TerminalInfo
    fun completePrintRequest(request: PrintRequest)

    /**
     * Return an interface that should print to stderr if possible.
     *
     * If the interface doesn't support stdErr (for example, if it writes to an html field in the browser rather than a
     * desktop terminal), the same instance may be returned.
     */
    fun forStdErr(): TerminalInterface
}

@ExperimentalTerminalApi
data class PrintRequest(
    val text: String,
    val trailingLinebreak: Boolean,
)
