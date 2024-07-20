package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Size
import kotlin.time.Duration


interface TerminalInterface {
    /**
     * Get information about the current terminal.
     *
     * @param ansiLevel The level of ANSI codes to use, or `null` to autodetect.
     * @param hyperlinks Whether to enable hyperlink support, or `null` to autodetect.
     * @param outputInteractive Whether the output stream is interactive. If `null`, it will be autodetected.
     * @param inputInteractive Whether the input stream is interactive. If `null`, it will be autodetected.
     */
    fun info(
        ansiLevel: AnsiLevel?,
        hyperlinks: Boolean?,
        outputInteractive: Boolean?,
        inputInteractive: Boolean?,
    ): TerminalInfo

    /**
     * Display a PrintRequest on this terminal.
     */
    fun completePrintRequest(request: PrintRequest)

    /**
     * Read a single line of input from stdin, returning null if no input is available
     *
     * @param hideInput If true, treat the input like a password that should not be echoed to the screen as it's typed.
     * @throws UnsupportedOperationException if the current interface doesn't support reading input. Currently, all
     *   targets are supported by default except Browser JS.
     */
    fun readLineOrNull(hideInput: Boolean): String?

    // TODO: docs
    fun getTerminalSize(): Size?
    fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): InputEvent? // null means retry
    fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable
    fun shouldAutoUpdateSize(): Boolean = true
}

data class PrintRequest(
    /** The Text to print */
    val text: String,
    /** If True, a trailing linebreak should be written after the text. */
    val trailingLinebreak: Boolean,
    /**
     * If True, the text should be written to stderr instead of stdout.
     *
     * If this terminal doesn't have separate output streams, this can be ignored.
     */
    val stderr: Boolean,
)
