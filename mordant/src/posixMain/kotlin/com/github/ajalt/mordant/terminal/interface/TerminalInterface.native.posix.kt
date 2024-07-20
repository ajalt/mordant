package com.github.ajalt.mordant.terminal.`interface`

import kotlinx.cinterop.*
import platform.posix.read
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal abstract class TerminalInterfaceNativePosix : TerminalInterfacePosix() {
    override fun isatty(fd: Int): Boolean {
        return platform.posix.isatty(fd) != 0
    }

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
