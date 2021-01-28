package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel

@ExperimentalTerminalApi
internal class StdoutTerminalInterface(
    ansiLevel: AnsiLevel?,
    width: Int?,
    height: Int?,
    hyperlinks: Boolean?,
    interactive: Boolean?,
) : TerminalInterface {
    override val info = TerminalDetection.detectTerminal(ansiLevel, width, height, hyperlinks, interactive)

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
