package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.internal.SYSCALL_HANDLER
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration

// TODO docs, tests
fun Terminal.readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
    if (!info.inputInteractive) return null
    return SYSCALL_HANDLER.readKeyEvent(timeout)
}

fun Terminal.enterRawMode(): AutoCloseable {
    if (!info.inputInteractive) return AutoCloseable { }
    return SYSCALL_HANDLER.enterRawMode()
}
