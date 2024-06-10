package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.internal.Size
import kotlin.time.Duration

internal interface SyscallHandler {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun fastIsTty(): Boolean = true
    fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): InputEvent?
    fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable?
}

internal object DumbSyscallHandler : SyscallHandler {
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): InputEvent? = null
    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable? = null
}
