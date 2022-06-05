package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel

/**
 * @property inputLines Lines of input to return from [readLineOrNull].
 */
@ExperimentalTerminalApi
class VirtualTerminalInterface private constructor(
    override val info: TerminalInfo,
    var inputLines: MutableList<String>,
    private val stdout: StringBuilder,
    private val stderr: StringBuilder,
    private val output: StringBuilder,
    private val useStdErr: Boolean,
) : TerminalInterface {
    constructor(
        ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
        width: Int = 79,
        height: Int = 24,
        hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
        outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
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
            crClearsLine = crClearsLine,
        ),
        inputLines = mutableListOf(),
        stdout = StringBuilder(),
        stderr = StringBuilder(),
        output = StringBuilder(),
        useStdErr = false
    )

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
        val sb = if (useStdErr) stderr else stdout
        sb.append(request.text)
        output.append(request.text)
        if (request.trailingLinebreak) {
            sb.append("\n")
            output.append("\n")
        }
    }

    override fun forStdErr(): VirtualTerminalInterface = VirtualTerminalInterface(
        info, inputLines, stdout, stderr, output, true
    )
    override fun readLineOrNull(hideInput: Boolean): String? = inputLines.removeFirstOrNull()
}

