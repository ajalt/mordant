package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.internal.SYSCALL_HANDLER
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration
import kotlin.time.TimeSource

// TODO: docs, tests
fun Terminal.enterRawMode(mouseTracking: MouseTracking = MouseTracking.Off): RawModeScope? {
    if (!info.inputInteractive) return null
    return SYSCALL_HANDLER.enterRawMode(mouseTracking)?.let { RawModeScope(it, mouseTracking) }
}

class RawModeScope internal constructor(
    closeable: AutoCloseable,
    private val mouseTracking: MouseTracking,
) : AutoCloseable by closeable {
    // TODO: docs, tests
    fun readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
        while (true) {
            return when (val event = readEvent(timeout)) {
                is KeyboardEvent -> event
                is MouseEvent -> continue
                null -> null
            }
        }
    }

    fun readMouse(timeout: Duration = Duration.INFINITE): MouseEvent? {
        while (true) {
            return when (val event = readEvent(timeout)) {
                is MouseEvent -> event
                is KeyboardEvent -> continue
                null -> null
            }
        }
    }

    fun readEvent(timeout: Duration = Duration.INFINITE): InputEvent? {
        val t0 = TimeSource.Monotonic.markNow()
        do {
            val event = SYSCALL_HANDLER.readInputEvent(timeout - t0.elapsedNow(), mouseTracking)
            return when (event) {
                is KeyboardEvent -> event
                is MouseEvent -> {
                    if (mouseTracking != MouseTracking.Off) event
                    else continue
                }

                null -> null
            }
        } while (t0.elapsedNow() < timeout)
        return null
    }
}
