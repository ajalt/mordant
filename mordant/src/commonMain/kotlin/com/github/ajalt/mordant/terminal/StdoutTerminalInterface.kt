package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.printStderr
import com.github.ajalt.mordant.internal.readLineOrNullMpp
import com.github.ajalt.mordant.rendering.AnsiLevel

@ExperimentalTerminalApi
internal class StdoutTerminalInterface private constructor(
    override val info: TerminalInfo,
    private val errInfo: TerminalInfo,
    private val useStdErr: Boolean,
) : TerminalInterface {
    constructor(
        ansiLevel: AnsiLevel?,
        width: Int?,
        height: Int?,
        hyperlinks: Boolean?,
        interactive: Boolean?,
    ) : this(TerminalDetection.detectTerminal(false, ansiLevel, width, height, hyperlinks, interactive),
        TerminalDetection.detectTerminal(true, ansiLevel, width, height, hyperlinks, interactive), false)

    constructor(): this(null, null, null, null, null)

    override fun completePrintRequest(request: PrintRequest) {
        if (useStdErr) {
            printStderr(request.text, request.trailingLinebreak)
            return
        }

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

    override fun forStdErr(): TerminalInterface {
        return StdoutTerminalInterface(info, errInfo, true)
    }

    override fun readLineOrNull(hideInput: Boolean): String? = readLineOrNullMpp(hideInput)
}
