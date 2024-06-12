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
 * @return A scope that will restore the terminal to its previous state when closed, or `null` if
 * the terminal is not interactive.
 */
fun Terminal.enterRawMode(mouseTracking: MouseTracking = MouseTracking.Off): RawModeScope? {
    if (!info.inputInteractive) return null
    return SYSCALL_HANDLER.enterRawMode(mouseTracking)?.let { RawModeScope(it, mouseTracking) }
}

class RawModeScope internal constructor(
    closeable: AutoCloseable,
    private val mouseTracking: MouseTracking,
) : AutoCloseable by closeable {
    /**
     * Read a single keyboard event from the terminal, ignoring any mouse events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
        while (true) {
            return when (val event = readEvent(timeout)) {
                is KeyboardEvent -> event
                is MouseEvent -> continue
                null -> null
            }
        }
    }

    /**
     * Read a single mouse event from the terminal, ignoring any keyboard events that arrive first.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readMouse(timeout: Duration = Duration.INFINITE): MouseEvent? {
        while (true) {
            return when (val event = readEvent(timeout)) {
                is MouseEvent -> event
                is KeyboardEvent -> continue
                null -> null
            }
        }
    }

    /**
     * Read a single input event from the terminal.
     *
     * @param timeout The maximum amount of time to wait for an event.
     * @return The event, or `null` if no event was received before the timeout.
     */
    fun readEvent(timeout: Duration = Duration.INFINITE): InputEvent? {
        val t0 = TimeSource.Monotonic.markNow()
        do {
            val event = SYSCALL_HANDLER.readInputEvent(timeout - t0.elapsedNow(), mouseTracking)
            return when (event) {
                is SysInputEvent.Success -> {
                    if (event.event is MouseEvent && mouseTracking == MouseTracking.Off) continue
                    event.event
                }

                SysInputEvent.Fail -> null
                SysInputEvent.Retry -> continue
            }
        } while (t0.elapsedNow() < timeout)
        return null
    }
}
