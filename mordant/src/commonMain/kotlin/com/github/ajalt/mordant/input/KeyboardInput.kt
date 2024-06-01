package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.internal.enterRawModeMpp
import com.github.ajalt.mordant.internal.readKeyMpp
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration

// TODO docs
fun Terminal.readKey(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
    if (!info.inputInteractive) return null
    return readKeyMpp(timeout)
}

fun Terminal.enterRawMode(): AutoCloseable {
    if (!info.inputInteractive) return AutoCloseable { }
    return enterRawModeMpp()
}
