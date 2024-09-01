package com.github.ajalt.mordant.terminal.terminalinterface

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value

internal abstract class TerminalInterfaceNativePosix : TerminalInterfacePosix() {
    override fun isatty(fd: Int): Boolean {
        return platform.posix.isatty(fd) != 0
    }

    override fun readRawByte(): Int? = memScoped {
        val c = alloc<ByteVar>()
        val read = readIntoBuffer(c)
        if (read < 0) throw RuntimeException("Error reading from stdin")
        if (read > 0) return c.value.toInt()
        return null
    }

    // `read` has different byte widths on linux and apple
    protected abstract fun readIntoBuffer(c: ByteVar): Long
}
