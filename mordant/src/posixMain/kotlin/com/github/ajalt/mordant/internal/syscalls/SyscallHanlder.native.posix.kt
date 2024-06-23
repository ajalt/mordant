package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.internal.Size
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerPosix.Termios
import kotlinx.cinterop.*
import platform.posix.read
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal expect fun getTerminalSizeNative(): Size?
internal expect fun getStdinTermiosNative(): Termios
internal expect fun setStdinTermiosNative(termios: Termios)

internal object SyscallHandlerNativePosix : SyscallHandlerPosix() {
    override fun isatty(fd: Int): Boolean {
        return platform.posix.isatty(fd) != 0
    }

    override fun getTerminalSize(): Size? = getTerminalSizeNative()

    override fun getStdinTermios(): Termios = getStdinTermiosNative()

    override fun setStdinTermios(termios: Termios): Unit = setStdinTermiosNative(termios)

    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char = memScoped {
        do {
            val c = alloc<ByteVar>()
            val read = read(platform.posix.STDIN_FILENO, c.ptr, 1u)
            if (read < 0) throw RuntimeException("Error reading from stdin")
            if (read > 0) return c.value.toInt().toChar()
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
