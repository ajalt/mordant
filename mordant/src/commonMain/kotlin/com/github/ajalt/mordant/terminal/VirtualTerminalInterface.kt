package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel

@ExperimentalTerminalApi
class VirtualTerminalInterface(
    ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    crClearsLine: Boolean = false,
) : TerminalInterface {
    override val info = TerminalInfo(
        width,
        height,
        ansiLevel,
        hyperlinks,
        outputInteractive = outputInteractive,
        inputInteractive = inputInteractive,
        crClearsLine = crClearsLine,
    )

    private val sb = StringBuilder()

    fun clearBuffer() {
        sb.clear()
    }

    fun buffer(): String = sb.toString()

    override fun completePrintRequest(request: PrintRequest) {
        sb.append(request.text)
        if (request.trailingLinebreak) {
            sb.append("\n")
        }
    }

    override fun forStdErr(): TerminalInterface = this
}

