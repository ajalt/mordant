package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal
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
    if (!info.inputInteractive) {
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
        while (true) {
            val event = readEvent(timeout)
            if (event is KeyboardEvent) return event
        }
    }

    /**
     * Read a single keyboard event from the terminal, ignoring any mouse events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readKeyOrNull(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
        return runCatching { readKey(timeout) }.getOrNull()
    }

    /**
     * Read a single mouse event from the terminal, ignoring any keyboard events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @throws RuntimeException if no event is received before the timeout, or input cannot be read.
     */
    fun readMouse(timeout: Duration = Duration.INFINITE): MouseEvent {
        while (true) {
            val event = readEvent(timeout)
            if (event is MouseEvent) return event
        }
    }

    /**
     * Read a single mouse event from the terminal, ignoring any keyboard events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readMouseOrNull(timeout: Duration = Duration.INFINITE): MouseEvent? {
        return runCatching { readMouse(timeout) }.getOrNull()
    }

    /**
     * Read a single input event from the terminal.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @throws RuntimeException if no event is received before the timeout, or input cannot be read.
     */
    fun readEvent(timeout: Duration = Duration.INFINITE): InputEvent {
        val t0 = TimeSource.Monotonic.markNow()
        do {
            val event = terminal.terminalInterface
                .readInputEvent(timeout - t0.elapsedNow(), mouseTracking)
            if (event == null || event is MouseEvent && mouseTracking == MouseTracking.Off) continue
            return event
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout while waiting for input")
    }

    /**
     * Read a single input event from the terminal.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readEventOrNull(timeout: Duration = Duration.INFINITE): InputEvent? {
        return runCatching { readEvent(timeout) }.getOrNull()
    }
}
