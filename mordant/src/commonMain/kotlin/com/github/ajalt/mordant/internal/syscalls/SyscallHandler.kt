package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.internal.Size
import kotlin.time.Duration

internal interface SyscallHandler {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun fastIsTty(): Boolean = true
    fun readKeyEvent(timeout: Duration): KeyboardEvent?
    fun enterRawMode(): AutoCloseable
}

internal object DumbSyscallHandler : SyscallHandler {
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun readKeyEvent(timeout: Duration): KeyboardEvent? = null
    override fun enterRawMode(): AutoCloseable = AutoCloseable { }
}
