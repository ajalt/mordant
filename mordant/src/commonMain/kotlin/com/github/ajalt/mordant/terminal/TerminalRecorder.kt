package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel

class TerminalRecorder private constructor(
    override val info: TerminalInfo,
) : TerminalInterface {
    constructor(
        ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
        width: Int = 79,
        height: Int = 24,
        hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
        outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
        stderrInteractive: Boolean = outputInteractive,
        inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
        crClearsLine: Boolean = false,
    ) : this(
        info = TerminalInfo(
            width,
            height,
            ansiLevel,
            hyperlinks,
            outputInteractive = outputInteractive,
            inputInteractive = inputInteractive,
            stderrInteractive = stderrInteractive,
            crClearsLine = crClearsLine,
        ),
    )

    /**
     * Lines of input to return from [readLineOrNull].
     */
    var inputLines: MutableList<String> = mutableListOf()
    private val stdout: StringBuilder = StringBuilder()
    private val stderr: StringBuilder = StringBuilder()
    private val output: StringBuilder = StringBuilder()

    fun clearOutput() {
        stdout.clear()
        stderr.clear()
        output.clear()
    }

    /** The content written to stdout */
    fun stdout(): String = stdout.toString()

    /** The content written to stderr */
    fun stderr(): String = stderr.toString()

    /** The combined content of [stdout] and [stderr] */
    fun output(): String = output.toString()

    override fun completePrintRequest(request: PrintRequest) {
        val sb = if (request.stderr) stderr else stdout
        sb.append(request.text)
        output.append(request.text)
        if (request.trailingLinebreak) {
            sb.append("\n")
            output.append("\n")
        }
    }

    override fun readLineOrNull(hideInput: Boolean): String? = inputLines.removeFirstOrNull()
}
