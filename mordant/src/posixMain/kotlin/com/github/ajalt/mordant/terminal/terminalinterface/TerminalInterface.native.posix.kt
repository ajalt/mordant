package com.github.ajalt.mordant.terminal.terminalinterface

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal abstract class TerminalInterfaceNativePosix : TerminalInterfacePosix() {
    override fun isatty(fd: Int): Boolean {
        return platform.posix.isatty(fd) != 0
    }

    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char = memScoped {
        do {
            val c = alloc<ByteVar>()
            val read = readIntoBuffer(c)
            if (read < 0) throw RuntimeException("Error reading from stdin")
            if (read > 0) return c.value.toInt().toChar()
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }

    // `read` has different byte widths on linux and apple
    protected abstract fun readIntoBuffer(c: ByteVar): Long
}
