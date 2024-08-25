package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Size
import kotlin.time.Duration

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
    private val info: TerminalInfo,
    private val size: Size,
) : TerminalInterface {
    constructor(
        ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
        width: Int = 79,
        height: Int = 24,
        hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
        outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
        inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
        crClearsLine: Boolean = false, // TODO(3.0): rename this to supportsAnsiCursor
    ) : this(
        TerminalInfo(
            ansiLevel = ansiLevel,
            ansiHyperLinks = hyperlinks,
            outputInteractive = outputInteractive,
            inputInteractive = inputInteractive,
            crClearsLine = crClearsLine,
        ),
        Size(width, height),
    )

    /**
     * Lines of input to return from [readLineOrNull].
     */
    var inputLines: MutableList<String> = mutableListOf()

    /**
     * Input events to return when reading in raw mode
     */
    var inputEvents: MutableList<InputEvent> = mutableListOf()

    /** Whether raw mode is currently active */
    var rawModeActive: Boolean = false

    private val stdout: StringBuilder = StringBuilder()
    private val stderr: StringBuilder = StringBuilder()
    private val output: StringBuilder = StringBuilder()

    /** Clear [stdout], [stderr], and [output] */
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
    override fun getTerminalSize(): Size = size
    override fun info(
        ansiLevel: AnsiLevel?,
        hyperlinks: Boolean?,
        outputInteractive: Boolean?,
        inputInteractive: Boolean?,
    ): TerminalInfo {
        return TerminalInfo(
            ansiLevel = ansiLevel ?: info.ansiLevel,
            ansiHyperLinks = hyperlinks ?: info.ansiHyperLinks,
            outputInteractive = outputInteractive ?: info.outputInteractive,
            inputInteractive = inputInteractive ?: info.inputInteractive,
            crClearsLine = info.crClearsLine,
        )
    }

    override fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): InputEvent? {
        return inputEvents.removeFirstOrNull()
    }

    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        rawModeActive = true
        return AutoCloseable { rawModeActive = false }
    }
}
