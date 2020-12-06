package com.github.ajalt.mordant.terminal

internal class StdoutTerminalInterface(
    ansiLevel: AnsiLevel? = null,
    width: Int? = null,
    height: Int? = null,
    hyperlinks: Boolean? = null,
) : TerminalInterface {
    override val info = TerminalDetection.detectTerminal(ansiLevel, width, height, hyperlinks)

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
