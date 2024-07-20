package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.printStderr
import com.github.ajalt.mordant.internal.readLineOrNullMpp
import com.github.ajalt.mordant.rendering.AnsiLevel

/**
 * A base class for terminal interfaces that print using standard `kotlin.io` functions.
 */
abstract class StandardTerminalInterface : TerminalInterface {
    override fun info(
        ansiLevel: AnsiLevel?,
        hyperlinks: Boolean?,
        outputInteractive: Boolean?,
        inputInteractive: Boolean?,
    ): TerminalInfo {
        return TerminalDetection.detectTerminal(
            ansiLevel = ansiLevel,
            hyperlinks = hyperlinks,
            forceInputInteractive = inputInteractive,
            forceOutputInteractive = outputInteractive,
            detectedStdinInteractive = stdinInteractive(),
            detectedStdoutInteractive = stdoutInteractive(),
        )
    }

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

    /** Whether the output stream is detected as interactive. */
    open fun stdoutInteractive(): Boolean = true

    /** Whether the input stream is detected as interactive. */
    open fun stdinInteractive(): Boolean = true
}
