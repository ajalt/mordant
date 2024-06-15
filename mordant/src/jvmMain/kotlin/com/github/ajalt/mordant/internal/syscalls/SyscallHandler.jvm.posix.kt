package com.github.ajalt.mordant.internal.syscalls

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal abstract class SyscallHandlerJvmPosix : SyscallHandlerPosix() {
    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char {
        do {
            val c = System.`in`.read()
            if (c >= 0) return c.toChar()
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
