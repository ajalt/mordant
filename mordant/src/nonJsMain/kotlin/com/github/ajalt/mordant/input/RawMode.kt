package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.internal.SYSCALL_HANDLER
import com.github.ajalt.mordant.internal.syscalls.SysInputEvent
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
    return RawModeScope(SYSCALL_HANDLER.enterRawMode(mouseTracking), mouseTracking)
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
        RawModeScope(SYSCALL_HANDLER.enterRawMode(mouseTracking), mouseTracking)
    }.getOrNull()
}

class RawModeScope internal constructor(
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
        return readEventsUntilTimeout(timeout) { inputEvent ->
            if (inputEvent is KeyboardEvent) {
                return inputEvent
            }
        }
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
        return readEventsUntilTimeout(timeout) { inputEvent ->
            if (inputEvent is MouseEvent && mouseTracking != MouseTracking.Off) {
                return inputEvent
            }
        }
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
        return readEventsUntilTimeout(timeout) { inputEvent ->
            if (inputEvent !is MouseEvent || mouseTracking != MouseTracking.Off) {
                return inputEvent
            }
        }
    }

    private inline fun readEventsUntilTimeout(timeout: Duration, handler: (InputEvent) -> Unit): Nothing? {
        val t0 = TimeSource.Monotonic.markNow()
        do {
            val event = SYSCALL_HANDLER.readInputEvent(timeout - t0.elapsedNow(), mouseTracking)
            if (event is SysInputEvent.Success) {
                handler(event.event)
            }
        } while (t0.elapsedNow() < timeout)
        return null
    }

    private fun throwTimeout(): Nothing = throw RuntimeException("Timeout while waiting for input")
}
