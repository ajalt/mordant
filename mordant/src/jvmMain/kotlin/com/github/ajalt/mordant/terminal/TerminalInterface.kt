package com.github.ajalt.mordant.terminal


@ExperimentalTerminalApi
interface TerminalInterface {
    val info: TerminalInfo
    fun completePrintRequest(request: PrintRequest)
}

@ExperimentalTerminalApi
data class PrintRequest(
    val text: String,
    val trailingLinebreak: Boolean,
)
