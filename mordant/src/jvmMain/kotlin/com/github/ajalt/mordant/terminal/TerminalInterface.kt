package com.github.ajalt.mordant.terminal


interface TerminalInterface {
    val info: TerminalInfo
    fun completePrintRequest(request: PrintRequest)
}

data class PrintRequest(
    val text: String,
    val trailingLinebreak: Boolean,
)
