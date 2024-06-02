package com.github.ajalt.mordant.internal.syscalls

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal abstract class SyscallHandlerJnaPosix : SyscallHandlerPosix() {
    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char? {
        while (t0.elapsedNow() < timeout) {
            val c = System.`in`.read().takeIf { it >= 0 }?.toChar()
            if (c != null) return c
        }
        return null
    }
}
