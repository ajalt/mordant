package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel

/**
 * A [TerminalInterface] that records all output and allows you to provide input.
 *
 * ### Exmaple
 *
 * ```
 * val recorder = TerminalRecorder()
 * val t = Terminal(terminalInterface = recorder)
 * t.println("Hello, world!")
 * assertEquals(recorder.output(), "Hello, world!\n")
 */
class TerminalRecorder private constructor(
    override val info: TerminalInfo,
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
