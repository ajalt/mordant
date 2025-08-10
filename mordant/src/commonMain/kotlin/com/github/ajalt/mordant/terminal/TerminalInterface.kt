package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Size
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.TimeMark


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

    /**
     * Return the current size, in cells, of the terminal, or null if the size is unknown.
     */
    fun getTerminalSize(): Size? = null

    /**
     * Read a single input event from the terminal, or null if no event is available but this call
     * should be retried immediately.
     *
     * You would return `null` if you receive an event that should be ignored, like a mouse
     * event when mouse tracking is off.
     *
     * @param timeout The point in time that this call should block to while waiting for an event.
     *   If the timeout is in the past, this method should not block.
     * @param mouseTracking The current mouse tracking mode.
     * @return The event, or `null` if no event is available but this call should be retried
     */
    fun readInputEvent(timeout: TimeMark, mouseTracking: MouseTracking): InputEvent? {
        throw NotImplementedError("Reading input events is not supported on this terminal")
    }

    /**
     * Enter raw mode on the terminal, disabling line buffering and echoing to enable reading
     * individual character.
     *
     * @return A scope that will restore the terminal to its previous state when closed.
     */
    fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        throw NotImplementedError("Raw mode is not supported on this terminal")
    }

    /**
     * Return true if the [getTerminalSize] method should be called frequently to update the size.
     */
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

class TimeoutException : RuntimeException("Timeout waiting for input")
