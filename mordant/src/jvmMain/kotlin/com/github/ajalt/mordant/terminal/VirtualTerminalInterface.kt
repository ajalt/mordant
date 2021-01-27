package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel


class VirtualTerminalInterface(
    ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    stdoutInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    stdinInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
) : TerminalInterface {
    override val info = TerminalInfo(
        width,
        height,
        ansiLevel,
        hyperlinks,
        stdoutInteractive = stdoutInteractive,
        stdinInteractive = stdinInteractive
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
}

