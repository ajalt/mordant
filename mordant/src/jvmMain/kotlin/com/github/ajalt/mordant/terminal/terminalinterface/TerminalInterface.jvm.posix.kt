package com.github.ajalt.mordant.terminal.terminalinterface

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

/**
 * A base class for terminal interfaces for JVM POSIX systems that uses `System.in` for input.
 */
abstract class TerminalInterfaceJvmPosix : TerminalInterfacePosix() {
    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Int {
        Character.toString(1)
        do {
            val c = System.`in`.read()
            if (c >= 0) return c
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
