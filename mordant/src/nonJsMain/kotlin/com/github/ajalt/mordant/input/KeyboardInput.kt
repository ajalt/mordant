package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.internal.SYSCALL_HANDLER
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration

// TODO: docs, tests
fun Terminal.enterRawMode(): RawModeScope? {
    if (!info.inputInteractive) return null
    return SYSCALL_HANDLER.enterRawMode()?.let(::RawModeScope)
}

class RawModeScope internal constructor(
    closeable: AutoCloseable,
) : AutoCloseable by closeable {
    // TODO: docs, tests
    fun readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
        return SYSCALL_HANDLER.readKeyEvent(timeout)
    }
}
