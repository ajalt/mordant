package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.printStderr
import com.github.ajalt.mordant.internal.readLineOrNullMpp
import com.github.ajalt.mordant.rendering.AnsiLevel

internal class StdoutTerminalInterface private constructor(
    override val info: TerminalInfo,
) : TerminalInterface {
    constructor(
        ansiLevel: AnsiLevel?,
        width: Int?,
        height: Int?,
        hyperlinks: Boolean?,
        interactive: Boolean?,
    ) : this(
        TerminalDetection.detectTerminal(ansiLevel, width, height, hyperlinks, interactive)
    )

    override fun completePrintRequest(request: PrintRequest) {
        when {
            request.stderr -> printStderr(request.text, request.trailingLinebreak)
            request.trailingLinebreak -> {
                if (request.text.isEmpty()) {
                    println()
                } else {
                    println(request.text)
                }
            }
            else -> print(request.text)
        }
    }

    override fun readLineOrNull(hideInput: Boolean): String? = readLineOrNullMpp(hideInput)
}
