package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.printStderr
import com.github.ajalt.mordant.rendering.AnsiLevel

@ExperimentalTerminalApi
internal class StdoutTerminalInterface(
    ansiLevel: AnsiLevel?,
    width: Int?,
    height: Int?,
    hyperlinks: Boolean?,
    interactive: Boolean?,
) : TerminalInterface {
    override val info = TerminalDetection.detectTerminal(false, ansiLevel, width, height, hyperlinks, interactive)

    override fun completePrintRequest(request: PrintRequest) {
        if (request.trailingLinebreak) {
            if (request.text.isEmpty()) {
                println()
            } else {
                println(request.text)
            }
        } else {
            print(request.text)
        }
    }
}

@ExperimentalTerminalApi
internal class StderrTerminalInterface(
    ansiLevel: AnsiLevel?,
    width: Int?,
    height: Int?,
    hyperlinks: Boolean?,
) : TerminalInterface {
    override val info = TerminalDetection.detectTerminal(true, ansiLevel, width, height, hyperlinks, null)

    override fun completePrintRequest(request: PrintRequest) {
        printStderr(request.text, request.trailingLinebreak)
    }
}
