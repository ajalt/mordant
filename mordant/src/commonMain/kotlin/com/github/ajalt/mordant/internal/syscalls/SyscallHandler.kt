package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.internal.Size
import kotlin.time.Duration

internal sealed class SysInputEvent {
    data class Success(val event: InputEvent) : SysInputEvent()
    data object Retry : SysInputEvent()
}

internal interface SyscallHandler {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun fastIsTty(): Boolean = true
    fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): SysInputEvent
    fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable
}

internal object DumbSyscallHandler : SyscallHandler {
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        throw UnsupportedOperationException("Cannot enter raw mode on this system")
    }
    override fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): SysInputEvent {
        throw UnsupportedOperationException("Cannot read input on this system")
    }
}
