package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TimeoutException
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Enter raw mode on the terminal, disabling line buffering and echoing to enable reading individual
 * input events.
 *
 * @param mouseTracking What type of mouse events to listen for.
 * @return A scope that will restore the terminal to its previous state when closed
 * @throws RuntimeException if the terminal is not interactive or raw mode cannot be entered.
 */
fun Terminal.enterRawMode(mouseTracking: MouseTracking = MouseTracking.Off): RawModeScope {
    if (!terminalInfo.inputInteractive) {
        throw IllegalStateException("Cannot enter raw mode on a non-interactive terminal")
    }
    return RawModeScope(this, terminalInterface.enterRawMode(mouseTracking), mouseTracking)
}

/**
 * Enter raw mode on the terminal, disabling line buffering and echoing to enable reading individual
 * input events.
 *
 * @param mouseTracking What type of mouse events to listen for.
 * @return A scope that will restore the terminal to its previous state when closed, or `null` if
 * the terminal is not interactive.
 */
fun Terminal.enterRawModeOrNull(mouseTracking: MouseTracking = MouseTracking.Off): RawModeScope? {
    return runCatching {
        RawModeScope(this, terminalInterface.enterRawMode(mouseTracking), mouseTracking)
    }.getOrNull()
}

class RawModeScope internal constructor(
    private val terminal: Terminal,
    closeable: AutoCloseable,
    private val mouseTracking: MouseTracking,
) : AutoCloseable by closeable {
    /**
     * Read a single keyboard event from the terminal, ignoring any mouse events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @throws RuntimeException if no event is received before the timeout, or input cannot be read.
     */
    fun readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent {
        return readKeyOrNull(timeout) ?: throwTimeout()
    }

    /**
     * Read a single keyboard event from the terminal, ignoring any mouse events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readKeyOrNull(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
        return readEventWithTimeout(timeout) { it !is KeyboardEvent } as KeyboardEvent?
    }

    /**
     * Read a single mouse event from the terminal, ignoring any keyboard events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @throws RuntimeException if no event is received before the timeout, or input cannot be read.
     */
    fun readMouse(timeout: Duration = Duration.INFINITE): MouseEvent {
        return readMouseOrNull(timeout) ?: throwTimeout()
    }

    /**
     * Read a single mouse event from the terminal, ignoring any keyboard events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readMouseOrNull(timeout: Duration = Duration.INFINITE): MouseEvent? {
        return readEventWithTimeout(timeout) { it !is MouseEvent } as MouseEvent?
    }

    /**
     * Read a single input event from the terminal.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @throws RuntimeException if no event is received before the timeout, or input cannot be read.
     */
    fun readEvent(timeout: Duration = Duration.INFINITE): InputEvent {
        return readEventOrNull(timeout) ?: throwTimeout()
    }

    /**
     * Read a single input event from the terminal.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readEventOrNull(timeout: Duration = Duration.INFINITE): InputEvent? {
        return readEventWithTimeout(timeout) { false }
    }

    private inline fun readEventWithTimeout(
        timeout: Duration, skip: (InputEvent) -> Boolean,
    ): InputEvent? {
        val t = TimeSource.Monotonic.markNow() + timeout
        try {
            do {
                val event = terminal.terminalInterface.readInputEvent(t, mouseTracking) ?: continue
                if (event is MouseEvent && mouseTracking == MouseTracking.Off || skip(event)) continue
                return event
            } while (t.hasNotPassedNow())
        } catch (_: TimeoutException) {}
        return null
    }

    private fun throwTimeout(): Nothing = throw RuntimeException("Timeout while waiting for input")
}
